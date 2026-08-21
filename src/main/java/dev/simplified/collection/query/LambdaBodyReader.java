package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
 * <p>Refusing on the first unexpected opcode is also what keeps this small enough to own. An
 * instruction that is never accepted never has to have its length known, so what would otherwise be
 * a bytecode library is a constant pool and six opcodes. Nothing here reads the class file's version
 * either, so a class compiled by a newer compiler than this one reads the same as any other.
 */
final class LambdaBodyReader {

    private static final int UTF8 = 1, INTEGER = 3, FLOAT = 4, LONG = 5, DOUBLE = 6, CLASS = 7,
        STRING = 8, FIELD_REFERENCE = 9, METHOD_REFERENCE = 10, INTERFACE_METHOD_REFERENCE = 11,
        NAME_AND_TYPE = 12, METHOD_HANDLE = 15, METHOD_TYPE = 16, DYNAMIC = 17, INVOKE_DYNAMIC = 18,
        MODULE = 19, PACKAGE = 20;

    private static final int ALOAD = 25, ALOAD_0 = 42, INVOKE_VIRTUAL = 182, INVOKE_STATIC = 184,
        INVOKE_INTERFACE = 185, CHECKCAST = 192, IRETURN = 172, RETURN = 177;

    /**
     * The constant pool, split by what each entry holds - a string, or the one or two pool indexes a
     * reference is built from. One entry per slot, so a long or a double leaves the slot after it
     * empty, the way the class file counts them.
     */
    private final @Nullable String @NotNull [] strings;
    private final int @NotNull [] first;
    private final int @NotNull [] second;

    private LambdaBodyReader(@Nullable String @NotNull [] strings, int @NotNull [] first, int @NotNull [] second) {
        this.strings = strings;
        this.first = first;
        this.second = second;
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

        return read(bytecode, lambda.getImplMethodName(), lambda.getImplMethodSignature());
    }

    /**
     * Reads the accessor chain the named method applies to its parameter.
     *
     * @param bytecode the class file holding the method
     * @param name the method's name
     * @param descriptor the method's descriptor
     * @return the chain, or {@code null} when the method is absent, unreadable or not a plain chain
     */
    static @Nullable Chain read(byte @NotNull [] bytecode, @NotNull String name, @NotNull String descriptor) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytecode))) {
            if (in.readInt() != 0xCAFEBABE)
                return null;

            // the minor and major version, which nothing here needs to agree with
            in.skipBytes(4);

            LambdaBodyReader reader = constantPool(in);

            if (reader == null)
                return null;

            // the access flags, this class and super class, then the interface list
            in.skipBytes(6);
            in.skipBytes(in.readUnsignedShort() * 2);
            reader.skipFields(in);

            return reader.walkMethod(in, name, descriptor);
        } catch (IOException | RuntimeException unreadable) {
            return null;
        }
    }

    /**
     * Reads the constant pool.
     *
     * @param in the class file, positioned at the pool's count
     * @return a reader over that pool, or {@code null} when it holds a kind this cannot measure
     */
    private static @Nullable LambdaBodyReader constantPool(@NotNull DataInputStream in) throws IOException {
        int slots = in.readUnsignedShort();
        String[] strings = new String[slots];
        int[] first = new int[slots];
        int[] second = new int[slots];

        for (int slot = 1; slot < slots; slot++) {
            int tag = in.readUnsignedByte();

            switch (tag) {
                case UTF8 -> strings[slot] = in.readUTF();
                case CLASS, STRING, METHOD_TYPE, MODULE, PACKAGE -> first[slot] = in.readUnsignedShort();
                case FIELD_REFERENCE, METHOD_REFERENCE, INTERFACE_METHOD_REFERENCE, NAME_AND_TYPE, DYNAMIC, INVOKE_DYNAMIC -> {
                    first[slot] = in.readUnsignedShort();
                    second[slot] = in.readUnsignedShort();
                }
                case INTEGER, FLOAT -> in.skipBytes(4);
                case METHOD_HANDLE -> in.skipBytes(3);
                // a long or a double is written once and counted twice
                case LONG, DOUBLE -> {
                    in.skipBytes(8);
                    slot++;
                }
                // a kind added after this was written has no width here, so nothing past it can be
                // found - which is a refusal rather than a guess
                default -> {
                    return null;
                }
            }
        }

        return new LambdaBodyReader(strings, first, second);
    }

    /**
     * Skips the field table, whose members carry attributes of their own.
     */
    private void skipFields(@NotNull DataInputStream in) throws IOException {
        int fields = in.readUnsignedShort();

        for (int at = 0; at < fields; at++) {
            in.skipBytes(6);
            this.codeOf(in, false);
        }
    }

    /**
     * Finds the named method in the method table and walks its body.
     *
     * @return the chain it applies, or {@code null} when it is absent or is not a plain chain
     */
    private @Nullable Chain walkMethod(@NotNull DataInputStream in, @NotNull String name, @NotNull String descriptor) throws IOException {
        int methods = in.readUnsignedShort();

        for (int at = 0; at < methods; at++) {
            in.skipBytes(2);
            String found = this.strings[in.readUnsignedShort()];
            String signature = this.strings[in.readUnsignedShort()];
            byte[] code = this.codeOf(in, name.equals(found) && descriptor.equals(signature));

            if (code != null)
                return this.walk(code);
        }

        return null;
    }

    /**
     * Reads one member's attributes, answering the bytes of its {@code Code} when it is wanted.
     *
     * @param wanted whether this member is the one being looked for
     * @return the code, or {@code null} when it was not wanted or the member carries none
     */
    private byte @Nullable [] codeOf(@NotNull DataInputStream in, boolean wanted) throws IOException {
        int attributes = in.readUnsignedShort();
        byte[] code = null;

        for (int at = 0; at < attributes; at++) {
            String name = this.strings[in.readUnsignedShort()];
            int length = in.readInt();

            if (!wanted || code != null || !"Code".equals(name)) {
                in.skipBytes(length);
                continue;
            }

            // the operand stack and local counts, then the instructions
            in.skipBytes(4);
            code = new byte[in.readInt()];
            in.readFully(code);
            in.skipBytes(length - 8 - code.length);
        }

        return code;
    }

    /**
     * Walks the instructions, refusing anything that is not a link in a plain accessor chain.
     *
     * @param code the method's instructions
     * @return the chain, or {@code null} when the body reads anything else
     */
    private @Nullable Chain walk(byte @NotNull [] code) {
        List<String> accessors = new ArrayList<>();
        String owner = null;
        boolean rooted = false;

        for (int at = 0; at < code.length; ) {
            int opcode = code[at] & 0xFF;

            if (opcode == ALOAD_0 || opcode == ALOAD) {
                // the parameter is loaded exactly once, first, and nothing else is
                if (rooted || (opcode == ALOAD && (code[at + 1] & 0xFF) != 0))
                    return null;

                rooted = true;
                at += opcode == ALOAD ? 2 : 1;
            } else if (opcode == INVOKE_VIRTUAL || opcode == INVOKE_INTERFACE || opcode == INVOKE_STATIC) {
                if (!rooted)
                    return null;

                int reference = ((code[at + 1] & 0xFF) << 8) | (code[at + 2] & 0xFF);
                int signature = this.second[reference];
                String declaredOn = this.strings[this.first[this.first[reference]]];
                String name = this.strings[this.first[signature]];
                String descriptor = this.strings[this.second[signature]];

                if (declaredOn == null || name == null || descriptor == null)
                    return null;

                if (opcode == INVOKE_STATIC) {
                    // boxing the result is the compiler's doing, not the caller's, so it is
                    // transparent
                    if (!"valueOf".equals(name) || !declaredOn.startsWith("java/lang/"))
                        return null;
                } else {
                    // an accessor takes nothing; anything with arguments is a computation, not a
                    // property
                    if (!descriptor.startsWith("()"))
                        return null;

                    if (owner == null)
                        owner = declaredOn;

                    accessors.add(name);
                }

                at += opcode == INVOKE_INTERFACE ? 5 : 3;
            } else if (opcode == CHECKCAST) {
                at += 3;
            } else if (opcode >= IRETURN && opcode <= RETURN) {
                at += 1;
            } else {
                return null;
            }
        }

        return owner == null || accessors.isEmpty() ? null : new Chain(owner, List.copyOf(accessors));
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

}
