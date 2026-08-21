package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * The property an extractor reads, recovered from the extractor itself.
 *
 * <p>An index cannot be keyed by the extractor: a non-capturing method reference such as
 * {@code Person::name} is a distinct singleton per call site, so two lines asking one question would
 * mint two keys and a lookup would be constant time or linear depending on which line asked. Keying
 * by the property both lines name removes that, which is the whole reason this type exists.
 *
 * <p>Recovery is best-effort and refusal is always safe. {@link Kind#UNRESOLVED} is the honest answer
 * for a bound method reference, a capturing lambda, a body that reads more than one property, or a
 * runtime without ASM on the classpath - and a caller holding one simply scans, exactly as it does
 * without an index.
 *
 * @param owner the class the first accessor is declared on, {@code null} only when unresolved
 * @param properties the accessor chain in the order it is applied, empty only when unresolved
 * @param kind how the reference was recovered
 */
public record PropertyReference(@Nullable Class<?> owner, @NotNull List<String> properties, @NotNull Kind kind) {

    /**
     * The answer for an extractor whose property cannot be named.
     */
    public static final PropertyReference UNRESOLVED = new PropertyReference(null, List.of(), Kind.UNRESOLVED);

    /**
     * One empty cell per extractor class. A lambda proxy class is minted once per call site, so the
     * class is a stable memo key and the decode below runs once for the life of that class. The cell
     * is filled by the caller rather than by {@link ClassValue#computeValue}, because the decode
     * needs an instance to ask for its serialized form and {@code computeValue} is handed only the
     * class.
     */
    private static final ClassValue<AtomicReference<PropertyReference>> DECODED = new ClassValue<>() {

        @Override
        protected AtomicReference<PropertyReference> computeValue(@NotNull Class<?> type) {
            return new AtomicReference<>();
        }

    };

    /**
     * Validates the component invariants and freezes the property chain.
     *
     * @param owner the class the first accessor is declared on
     * @param properties the accessor chain in the order it is applied
     * @param kind how the reference was recovered
     * @throws IllegalArgumentException if a resolved reference names no owner or no property, or an
     *         unresolved one names either
     */
    public PropertyReference {
        properties = List.copyOf(properties);

        if (kind == Kind.UNRESOLVED) {
            if (owner != null || !properties.isEmpty())
                throw new IllegalArgumentException("An unresolved reference names no owner and no property");
        } else if (owner == null || properties.isEmpty())
            throw new IllegalArgumentException(String.format("A '%s' reference names an owner and at least one property", kind));
    }

    /**
     * Recovers the property an extractor reads.
     *
     * <p>The answer is memoised against the extractor's class, so a call site pays the decode once
     * and every later call is a {@link ClassValue} read.
     *
     * @param extractor the extractor to decode
     * @return the property it reads, or {@link #UNRESOLVED} when it reads no single property
     */
    public static @NotNull PropertyReference of(@NotNull SearchFunction<?, ?> extractor) {
        Class<?> type = extractor.getClass();

        // A composition is one class holding many different chains, so it is decoded structurally
        // every time rather than memoised. Its halves are memoised, which is where the cost is.
        if (!type.isSynthetic())
            return decode(extractor);

        AtomicReference<PropertyReference> cell = DECODED.get(type);
        PropertyReference cached = cell.get();

        if (cached == null) {
            cached = decode(extractor);
            cell.set(cached);
        }

        return cached;
    }

    /**
     * Names a property tuple directly, for a caller that already knows what it wants to probe.
     *
     * @param owner the class carrying the properties
     * @param properties the property names, in probe order
     * @return the named reference
     * @throws IllegalArgumentException if no property is named
     */
    public static @NotNull PropertyReference of(@NotNull Class<?> owner, @NotNull String @NotNull ... properties) {
        return new PropertyReference(owner, List.of(properties), Kind.DECLARED);
    }

    /**
     * Whether this reference names a property.
     *
     * @return {@code true} unless the extractor was refused
     */
    public boolean isResolved() {
        return this.kind() != Kind.UNRESOLVED;
    }

    /**
     * Whether this reference names exactly one property directly on its owner, which is the only
     * shape an index over a single field can serve.
     *
     * @return {@code true} for a resolved single-hop reference
     */
    public boolean isDirect() {
        return this.isResolved() && this.properties().size() == 1;
    }

    /**
     * Restates this reference against the class actually holding the elements, so a reference
     * decoded off an accessor inherited from a supertype keys the same index as one declared on
     * the element class itself.
     *
     * @param elementType the class the elements are instances of
     * @return the reference in declared form, or {@link #UNRESOLVED} when the elements are not of
     *         the type this reference reads
     */
    public @NotNull PropertyReference against(@NotNull Class<?> elementType) {
        // An owner is present exactly when the reference is resolved, which the compact constructor
        // enforces - so naming it is also the resolved test.
        Class<?> owner = this.owner();

        if (owner == null || !owner.isAssignableFrom(elementType))
            return UNRESOLVED;

        return owner == elementType && this.kind() == Kind.DECLARED
            ? this
            : new PropertyReference(elementType, this.properties(), Kind.DECLARED);
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull String toString() {
        Class<?> owner = this.owner();

        if (owner == null)
            return "PropertyReference[unresolved]";

        return String.format("PropertyReference[%s.%s, %s]", owner.getSimpleName(), String.join(".", this.properties()), this.kind());
    }

    /**
     * Decodes one extractor without consulting the memo.
     *
     * @param extractor the extractor to decode
     * @return the property it reads, or {@link #UNRESOLVED}
     */
    private static @NotNull PropertyReference decode(@NotNull SearchFunction<?, ?> extractor) {
        if (extractor instanceof SearchFunction.Composed<?, ?, ?> composed)
            return compose(composed);

        SerializedLambda lambda = serializedForm(extractor);

        // A captured argument means the extractor reads something other than its own argument - a
        // bound reference such as person::name answers the same value whatever it is handed.
        if (lambda == null || lambda.getCapturedArgCount() != 0)
            return UNRESOLVED;

        int implKind = lambda.getImplMethodKind();

        if ((implKind == MethodHandleInfo.REF_invokeVirtual || implKind == MethodHandleInfo.REF_invokeInterface)
            && lambda.getImplMethodSignature().startsWith("()")) {
            Class<?> owner = loadOwner(lambda.getImplClass(), extractor.getClass().getClassLoader());

            if (owner == null)
                return UNRESOLVED;

            return new PropertyReference(owner, List.of(propertyOf(lambda.getImplMethodName())), Kind.METHOD_REFERENCE);
        }

        return readBody(lambda, extractor.getClass().getClassLoader());
    }

    /**
     * Joins the property paths of a composition's two halves, which needs no bytecode because both
     * halves are reachable as record components.
     *
     * <p>Held on the composition once it is worked out, because a composition is one class holding
     * every chain anyone writes and so cannot be remembered against its class the way every other
     * extractor is.
     *
     * @param composed the composition to decode
     * @return the joined path, or {@link #UNRESOLVED} when either half is refused
     */
    private static @NotNull PropertyReference compose(@NotNull SearchFunction.Composed<?, ?, ?> composed) {
        PropertyReference held = composed.decoded();

        if (held == null) {
            held = join(composed);
            composed.decoded(held);
        }

        return held;
    }

    /**
     * Joins the property paths the two halves of a composition read.
     *
     * @param composed the composition to read
     * @return the joined path, or {@link #UNRESOLVED} when either half is refused
     */
    private static @NotNull PropertyReference join(@NotNull SearchFunction.Composed<?, ?, ?> composed) {
        PropertyReference head = of(composed.from());
        Function<?, ?> second = composed.to();

        if (!head.isResolved() || !(second instanceof SearchFunction<?, ?> decodable))
            return UNRESOLVED;

        PropertyReference tail = of(decodable);

        if (!tail.isResolved())
            return UNRESOLVED;

        List<String> joined = new ArrayList<>(head.properties());
        joined.addAll(tail.properties());
        return new PropertyReference(head.owner(), joined, Kind.COMPOSED);
    }

    /**
     * Reads a lambda body back to the accessor chain it applies.
     *
     * @param lambda the cracked lambda naming the body to read
     * @param hint the classloader the extractor came from
     * @return the chain as a reference, or {@link #UNRESOLVED}
     */
    private static @NotNull PropertyReference readBody(@NotNull SerializedLambda lambda, @Nullable ClassLoader hint) {
        LambdaBodyReader.Chain chain = LambdaBodyReader.read(lambda);

        if (chain == null)
            return UNRESOLVED;

        Class<?> owner = loadOwner(chain.ownerInternalName(), hint);

        if (owner == null)
            return UNRESOLVED;

        List<String> path = new ArrayList<>(chain.accessors().size());
        chain.accessors().forEach(accessor -> path.add(propertyOf(accessor)));
        return new PropertyReference(owner, path, Kind.LAMBDA_BODY);
    }

    /**
     * Invokes the synthetic {@code writeReplace} the compiler emits on every lambda and method
     * reference whose target type is {@link Serializable}.
     *
     * @param extractor the extractor to crack
     * @return its serialized form, or {@code null} when it has none or cannot be read
     */
    private static @Nullable SerializedLambda serializedForm(@NotNull SearchFunction<?, ?> extractor) {
        try {
            Method writeReplace = extractor.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            return writeReplace.invoke(extractor) instanceof SerializedLambda lambda ? lambda : null;
        } catch (ReflectiveOperationException | RuntimeException unreadable) {
            return null;
        }
    }

    /**
     * Resolves a class named in internal form, trying the extractor's own loader first.
     *
     * @param internalName the class name in internal form
     * @param hint the loader the extractor came from
     * @return the class, or {@code null} when no loader can see it
     */
    private static @Nullable Class<?> loadOwner(@NotNull String internalName, @Nullable ClassLoader hint) {
        String binaryName = internalName.replace('/', '.');

        for (ClassLoader loader : new ClassLoader[] { hint, Thread.currentThread().getContextClassLoader(), PropertyReference.class.getClassLoader() }) {
            if (loader == null)
                continue;

            try {
                return Class.forName(binaryName, false, loader);
            } catch (ClassNotFoundException | LinkageError absent) {
                // Fall through to the next loader.
            }
        }

        return null;
    }

    /**
     * Reduces an accessor name to the property it reads, so a bean accessor and a fluent one that
     * name one field agree.
     *
     * @param accessor the accessor's method name
     * @return the property name
     */
    static @NotNull String propertyOf(@NotNull String accessor) {
        if (accessor.length() > 3 && accessor.startsWith("get") && Character.isUpperCase(accessor.charAt(3)))
            return decapitalise(accessor.substring(3));

        if (accessor.length() > 2 && accessor.startsWith("is") && Character.isUpperCase(accessor.charAt(2)))
            return decapitalise(accessor.substring(2));

        return accessor;
    }

    /**
     * Lowers a stem's first letter, leaving an acronym alone the way the bean convention does.
     *
     * @param stem the accessor name with its prefix removed
     * @return the property name
     */
    private static @NotNull String decapitalise(@NotNull String stem) {
        if (stem.length() > 1 && Character.isUpperCase(stem.charAt(1)))
            return stem;

        return Character.toLowerCase(stem.charAt(0)) + stem.substring(1);
    }

    /**
     * How a {@link PropertyReference} was recovered.
     */
    public enum Kind {

        /**
         * An unbound method reference, whose property is named in the constant pool.
         */
        METHOD_REFERENCE,
        /**
         * A lambda body, whose accessor chain was read back out of its bytecode.
         */
        LAMBDA_BODY,
        /**
         * Two extractors joined by {@link SearchFunction#combine}, whose halves both resolved.
         */
        COMPOSED,
        /**
         * A property tuple named directly by a caller rather than recovered from an extractor.
         */
        DECLARED,
        /**
         * An extractor that reads no single property, or one this runtime cannot read.
         */
        UNRESOLVED

    }

}
