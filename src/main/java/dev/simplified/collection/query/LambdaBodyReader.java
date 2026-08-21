package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;

/**
 * Recovers the accessor chain a lambda body reads, for the shapes where that is unambiguous.
 *
 * <p>A method reference names its property in the constant pool, but a lambda body compiles to a
 * synthetic method whose name is a slot ordinal. The body itself is still real bytecode, and
 * {@link SerializedLambda} gives its exact address, so the remaining step is to read that one
 * method.
 *
 * <p>Only a straight-line chain of no-argument accessors rooted at the parameter is accepted:
 * {@code row -> row.getA()} and {@code row -> row.getA().getB()} resolve, while a branch, an
 * arithmetic operation, a second root, a constant or a field read all refuse. Refusing is the
 * safe answer, because a body that reads more than one property has no single property to name,
 * and guessing one would answer wrongly rather than slowly.
 *
 * <p>{@link PropertyReference} reaches this class only after confirming ASM is on the runtime
 * classpath, so it may reference ASM freely.
 */
final class LambdaBodyReader {

    private LambdaBodyReader() {
        throw new UnsupportedOperationException("LambdaBodyReader is a static holder");
    }

    /**
     * Reads the accessor chain a lambda body applies to its parameter.
     *
     * @param lambda the cracked lambda naming the synthetic method to read
     * @return the chain, or {@code null} when the body is absent, unreadable or not a plain chain
     */
    static @Nullable Chain read(@NotNull SerializedLambda lambda) {
        byte[] bytecode = bytecodeOf(lambda.getImplClass());

        if (bytecode == null)
            return null;

        Collector collector = new Collector(lambda.getImplMethodName(), lambda.getImplMethodSignature());

        try {
            new ClassReader(bytecode).accept(collector, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        } catch (RuntimeException exception) {
            return null;
        }

        return collector.chain();
    }

    /**
     * Loads the class file holding the synthetic method.
     *
     * @param internalName the declaring class in internal form
     * @return the class bytes, or {@code null} when they cannot be read
     */
    private static byte @Nullable [] bytecodeOf(@NotNull String internalName) {
        String resource = internalName + ".class";

        for (ClassLoader loader : new ClassLoader[] { Thread.currentThread().getContextClassLoader(), LambdaBodyReader.class.getClassLoader() }) {
            if (loader == null)
                continue;

            try (InputStream stream = loader.getResourceAsStream(resource)) {
                if (stream != null)
                    return stream.readAllBytes();
            } catch (IOException | RuntimeException exception) {
                return null;
            }
        }

        return null;
    }

    /**
     * The accessor chain one body applies, rooted at its parameter.
     *
     * @param ownerInternalName the class the first accessor is declared on, in internal form
     * @param accessors the accessor names, in the order the body invokes them
     */
    record Chain(@NotNull String ownerInternalName, @NotNull List<String> accessors) {}

    /**
     * Finds the target method and delegates its instructions to a {@link BodyMatcher}.
     */
    private static final class Collector extends ClassVisitor {

        private final @NotNull String name;
        private final @NotNull String descriptor;
        private @Nullable BodyMatcher matcher;

        private Collector(@NotNull String name, @NotNull String descriptor) {
            super(Opcodes.ASM9);
            this.name = name;
            this.descriptor = descriptor;
        }

        /** {@inheritDoc} */
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (!this.name.equals(name) || !this.descriptor.equals(descriptor))
                return null;

            this.matcher = new BodyMatcher();
            return this.matcher;
        }

        private @Nullable Chain chain() {
            return this.matcher == null ? null : this.matcher.chain();
        }

    }

    /**
     * Accepts only a straight-line chain of no-argument accessors rooted at the parameter, and
     * refuses on the first instruction that is anything else.
     */
    private static final class BodyMatcher extends MethodVisitor {

        private final @NotNull List<String> accessors = new ArrayList<>();
        private @Nullable String ownerInternalName;
        private boolean rooted;
        private boolean refused;

        private BodyMatcher() {
            super(Opcodes.ASM9);
        }

        /** {@inheritDoc} */
        @Override
        public void visitVarInsn(int opcode, int index) {
            // The parameter must be loaded exactly once, first, and nothing else may be.
            if (opcode != Opcodes.ALOAD || index != 0 || this.rooted)
                this.refused = true;

            this.rooted = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (!this.rooted) {
                this.refused = true;
                return;
            }

            // Boxing the result is the compiler's doing, not the caller's, so it is transparent.
            if (opcode == Opcodes.INVOKESTATIC) {
                if (!"valueOf".equals(name) || !owner.startsWith("java/lang/"))
                    this.refused = true;

                return;
            }

            if (opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE) {
                this.refused = true;
                return;
            }

            // An accessor takes nothing; anything with arguments is a computation, not a property.
            if (!descriptor.startsWith("()")) {
                this.refused = true;
                return;
            }

            if (this.ownerInternalName == null)
                this.ownerInternalName = owner;

            this.accessors.add(name);
        }

        /** {@inheritDoc} */
        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode != Opcodes.CHECKCAST)
                this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitInsn(int opcode) {
            if (opcode < Opcodes.IRETURN || opcode > Opcodes.RETURN)
                this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitLdcInsn(Object value) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitIntInsn(int opcode, int operand) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitIincInsn(int index, int increment) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle handle, Object... arguments) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            this.refused = true;
        }

        /** {@inheritDoc} */
        @Override
        public void visitMultiANewArrayInsn(String descriptor, int dimensions) {
            this.refused = true;
        }

        private @Nullable Chain chain() {
            if (this.refused || this.ownerInternalName == null || this.accessors.isEmpty())
                return null;

            return new Chain(this.ownerInternalName, List.copyOf(this.accessors));
        }

    }

}
