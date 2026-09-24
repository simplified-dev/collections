package dev.simplified.collection.query;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Holds {@link LambdaBodyReader} against ASM over every class this module compiles.
 *
 * <p>The reader owns its class file parsing rather than depending on a library for it, and the risk
 * that carries is not refusing too often - a refusal only costs a scan - but naming the wrong
 * property, which would index the wrong values and answer wrongly. ASM is kept as a test dependency
 * for exactly this: the two read the same bytes and must reach the same chain, method for method.
 */
class LambdaBodyReaderDifferentialTest {

    /**
     * The chain ASM reads, in the same shape {@link LambdaBodyReader.Chain} carries.
     */
    private record Reference(String ownerInternalName, List<String> accessors) {}

    @Test
    void everyCompiledMethod_readsTheSameChainAsAsm() throws Exception {
        int methods = 0;
        int chains = 0;
        List<String> disagreements = new ArrayList<>();

        for (Path file : compiledClasses()) {
            byte[] bytecode = Files.readAllBytes(file);

            for (String[] member : membersOf(bytecode)) {
                methods++;
                Reference expected = withAsm(bytecode, member[0], member[1]);
                LambdaBodyReader.Chain found = LambdaBodyReader.read(bytecode, member[0], member[1]);

                if (expected != null)
                    chains++;

                if (!described(expected).equals(described(found)))
                    disagreements.add(String.format("%s#%s%s%n  asm=%s%n  own=%s",
                        file.getFileName(), member[0], member[1], described(expected), described(found)));
            }
        }

        assertEquals(List.of(), disagreements, "readers disagree");

        // a sweep that found nothing would agree with anything, so it has to have read real chains
        assertTrue(methods > 1_000, "expected the whole module, read " + methods + " methods");
        assertTrue(chains > 50, "expected real accessor chains, resolved " + chains);
    }

    @Test
    void classFileVersion_isNotRead() throws Exception {
        for (Path file : compiledClasses()) {
            byte[] bytecode = Files.readAllBytes(file);

            for (String[] member : membersOf(bytecode)) {
                LambdaBodyReader.Chain chain = LambdaBodyReader.read(bytecode, member[0], member[1]);

                if (chain == null)
                    continue;

                // a compiler newer than this one writes the constant pool and the code the same way,
                // so nothing here has an opinion about the version that precedes them
                byte[] future = bytecode.clone();
                future[6] = 0;
                future[7] = (byte) 99;

                assertEquals(chain, LambdaBodyReader.read(future, member[0], member[1]));
                return;
            }
        }

        fail("no compiled method held a chain to read");
    }

    /**
     * Renders a chain from either reader, so the two can be compared as text.
     */
    private static String described(Object chain) {
        if (chain instanceof LambdaBodyReader.Chain own)
            return own.ownerInternalName() + own.accessors();

        if (chain instanceof Reference reference)
            return reference.ownerInternalName() + reference.accessors();

        return "refused";
    }

    /**
     * Every class file this module compiled, main and test alike.
     */
    private static List<Path> compiledClasses() throws Exception {
        Set<Path> roots = new LinkedHashSet<>();

        for (Class<?> anchor : List.of(LambdaBodyReader.class, LambdaBodyReaderDifferentialTest.class)) {
            URL location = anchor.getProtectionDomain().getCodeSource().getLocation();
            roots.add(Path.of(location.toURI()));
        }

        List<Path> files = new ArrayList<>();

        for (Path root : roots) {
            if (!Files.isDirectory(root))
                continue;

            try (Stream<Path> tree = Files.walk(root)) {
                tree.filter(path -> path.toString().endsWith(".class")).forEach(files::add);
            }
        }

        return files;
    }

    /**
     * Names every method a class file declares.
     */
    private static List<String[]> membersOf(byte[] bytecode) {
        List<String[]> members = new ArrayList<>();

        new ClassReader(bytecode).accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                members.add(new String[] { name, descriptor });
                return null;
            }

        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

        return members;
    }

    /**
     * Reads one method's chain with ASM, applying the same rules the reader applies.
     */
    private static Reference withAsm(byte[] bytecode, String name, String descriptor) {
        Collector collector = new Collector(name, descriptor);

        try {
            new ClassReader(bytecode).accept(collector, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        } catch (RuntimeException unreadable) {
            return null;
        }

        return collector.matcher == null ? null : collector.matcher.chain();
    }

    /**
     * Finds the target method and hands its instructions to a {@link BodyMatcher}.
     */
    private static final class Collector extends ClassVisitor {

        private final String name;
        private final String descriptor;
        private BodyMatcher matcher;

        private Collector(String name, String descriptor) {
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

    }

    /**
     * Accepts only a straight-line chain of no-argument accessors rooted at the parameter.
     */
    private static final class BodyMatcher extends MethodVisitor {

        private final List<String> accessors = new ArrayList<>();
        private String ownerInternalName;
        private boolean rooted;
        private boolean refused;

        private BodyMatcher() {
            super(Opcodes.ASM9);
        }

        /** {@inheritDoc} */
        @Override
        public void visitVarInsn(int opcode, int index) {
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

            if (opcode == Opcodes.INVOKESTATIC) {
                if (!"valueOf".equals(name) || !owner.startsWith("java/lang/"))
                    this.refused = true;

                return;
            }

            if (opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE) {
                this.refused = true;
                return;
            }

            if (!descriptor.startsWith("()")) {
                this.refused = true;
                return;
            }

            if (this.ownerInternalName == null)
                this.ownerInternalName = owner;

            this.accessors.add(name);
        }

        /** {@inheritDoc} */
        @Override public void visitTypeInsn(int opcode, String type) { if (opcode != Opcodes.CHECKCAST) this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitInsn(int opcode) { if (opcode < Opcodes.IRETURN || opcode > Opcodes.RETURN) this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitFieldInsn(int opcode, String owner, String name, String descriptor) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitLdcInsn(Object value) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitIntInsn(int opcode, int operand) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitJumpInsn(int opcode, Label label) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitIincInsn(int index, int increment) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitInvokeDynamicInsn(String name, String descriptor, Handle handle, Object... arguments) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) { this.refused = true; }
        /** {@inheritDoc} */
        @Override public void visitMultiANewArrayInsn(String descriptor, int dimensions) { this.refused = true; }

        private Reference chain() {
            if (this.refused || this.ownerInternalName == null || this.accessors.isEmpty())
                return null;

            return new Reference(this.ownerInternalName, List.copyOf(this.accessors));
        }

    }

}
