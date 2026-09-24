package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What one class declares about how its instances can be found, read once off its {@link Indexed}
 * fields and held for the life of the class.
 *
 * <p>Reflection runs once per class rather than once per collection or once per query, because a
 * class's declarations cannot change while it is loaded.
 *
 * <p>A declaration is a tuple of {@link PropertyReference}, one per value a query compares. That is
 * the unit rather than a flat list of names because a component may itself read through more than
 * one accessor - {@code (department.name)} is one component of two hops, and {@code (mode, tier)} is
 * two components of one hop each, and nothing may confuse the two.
 */
final class IndexSchema {

    /**
     * How many accessors long a declared path may be.
     *
     * <p>A bound is needed because references form a graph rather than a tree - a department holding
     * its head and a person holding their department is an ordinary shape, and an unbounded walk
     * would never finish. Three is past anything a query in this codebase writes, and it is what
     * stops a graph of references from declaring a set no reader can hold in their head.
     */
    private static final int MAX_HOPS = 3;

    /**
     * The answer for a class declaring nothing, which every query falls through to a scan against.
     */
    static final IndexSchema EMPTY = new IndexSchema(Map.of(), Object.class);

    /**
     * What each class declares about itself, with nothing reached through.
     *
     * <p>Held separately from the resolved schema because resolving one class reads the local
     * declarations of the classes it reaches, and a cycle would otherwise re-enter
     * {@link ClassValue#computeValue} for a class already being computed.
     */
    private static final ClassValue<List<Local>> LOCAL = new ClassValue<>() {

        @Override
        protected List<Local> computeValue(@NotNull Class<?> type) {
            return readLocal(type);
        }

    };

    private static final ClassValue<IndexSchema> SCHEMAS = new ClassValue<>() {

        @Override
        protected IndexSchema computeValue(@NotNull Class<?> type) {
            return resolve(type);
        }

    };

    private final @NotNull Map<List<PropertyReference>, Declaration> declarations;

    /**
     * The same declarations keyed by their component set, so a query naming a composite's
     * components in any argument order still finds it. Two composites over one set of components in
     * different orders are legal, and the first one read answers here.
     */
    private final @NotNull Map<Set<PropertyReference>, Declaration> byComponents;

    /**
     * The single-component declarations keyed by their property path, so the one-predicate query
     * that is nearly every query is answered by a map read of a list the caller already holds.
     */
    private final @NotNull Map<List<String>, Declaration> bySinglePath;

    /**
     * The most derived class whose own fields or accessors carry a declaration. A class declaring
     * nothing of its own - a runtime proxy, or a subclass that only overrides behaviour - reads
     * exactly what this class reads, and so does every class between the two.
     */
    private final @NotNull Class<?> declaringClass;

    private IndexSchema(@NotNull Map<List<PropertyReference>, Declaration> declarations, @NotNull Class<?> declaringClass) {
        this.declarations = declarations;
        this.declaringClass = declaringClass;
        Map<Set<PropertyReference>, Declaration> byComponents = new LinkedHashMap<>();
        Map<List<String>, Declaration> bySinglePath = new LinkedHashMap<>();

        declarations.forEach((key, declaration) -> {
            byComponents.putIfAbsent(Set.copyOf(key), declaration);

            if (key.size() == 1)
                bySinglePath.putIfAbsent(key.getFirst().properties(), declaration);
        });

        this.byComponents = Map.copyOf(byComponents);
        this.bySinglePath = Map.copyOf(bySinglePath);
    }

    /**
     * Reads what a class declares, answering from the per-class cache after the first call.
     *
     * @param type the class to read
     * @return its declarations, or {@link #EMPTY}
     * @throws IllegalArgumentException if the declarations disagree with each other
     */
    static @NotNull IndexSchema of(@NotNull Class<?> type) {
        return SCHEMAS.get(type);
    }

    /**
     * Whether this class declares no index at all.
     *
     * @return {@code true} when nothing is declared
     */
    boolean isEmpty() {
        return this.declarations.isEmpty();
    }

    @NotNull Class<?> declaringClass() {
        return this.declaringClass;
    }

    /**
     * Finds the declaration over one component tuple, in the order it is probed.
     *
     * @param components the property tuple to probe by
     * @return the declaration, or {@code null} when the tuple carries no index
     */
    @Nullable Declaration declaring(@NotNull List<PropertyReference> components) {
        return this.declarations.get(components);
    }

    /**
     * Finds the declaration over one property path.
     *
     * <p>Probed by the path rather than by a {@link PropertyReference} restated against the element
     * type, because restating one allocates and this runs on every query.
     *
     * @param path the property path a query names
     * @return the declaration, or {@code null} when the path carries no single-property index
     */
    @Nullable Declaration coveringPath(@NotNull List<String> path) {
        return this.bySinglePath.get(path);
    }

    /**
     * Finds the declaration over exactly one set of components, whatever order a query named them
     * in.
     *
     * @param components the properties a query names
     * @return the declaration, or {@code null} when nothing is declared over exactly those
     */
    @Nullable Declaration covering(@NotNull List<PropertyReference> components) {
        Set<PropertyReference> distinct = Set.copyOf(components);

        // Two predicates over one property are not a composite key, they are a contradiction or a
        // redundancy, and either way the scan is the honest answer.
        if (distinct.size() != components.size())
            return null;

        return this.byComponents.get(distinct);
    }

    /**
     * Every declaration this class makes.
     *
     * @return the declarations, in the order they were read
     */
    @NotNull Collection<Declaration> declarations() {
        return this.declarations.values();
    }

    /**
     * Collects the {@link Indexed} annotations a class and its supertypes declare, without reaching
     * through any of them.
     *
     * @param type the class to read
     * @return one entry per annotation on the most derived declaration of each property, most
     *         derived first
     */
    private static @NotNull List<Local> readLocal(@NotNull Class<?> type) {
        List<Local> locals = new ArrayList<>();

        // What a more derived class already declares, which a supertype's declaration of the same
        // property yields to. Only an annotation claims, so an unannotated override hides nothing.
        Set<String> claimed = new HashSet<>();

        // Most derived first, so a shadowing field or an annotated override wins the way a field
        // read or a virtual call would.
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            Set<String> declared = new LinkedHashSet<>();

            for (Field field : current.getDeclaredFields()) {
                // A static field holds one value for every element, so an index over it sorts
                // nothing and would answer every query with the whole collection.
                if (Modifier.isStatic(field.getModifiers()))
                    continue;

                if (claimed.contains(field.getName()))
                    continue;

                for (Indexed found : field.getAnnotationsByType(Indexed.class)) {
                    declared.add(field.getName());
                    locals.add(new Local(field.getName(), field.getType(), found, current));
                }
            }

            for (Method accessor : current.getDeclaredMethods()) {
                if (!reads(accessor))
                    continue;

                // The property an accessor names is the one its extractor decodes to, so a query
                // written against the accessor and a declaration written on it agree by name.
                String property = PropertyReference.propertyOf(accessor.getName());

                // A record propagates a component's annotation to both its field and its accessor,
                // and a class may carry it on both by hand. Either way it is one declaration.
                if (claimed.contains(property) || !declared.add(property))
                    continue;

                for (Indexed found : accessor.getAnnotationsByType(Indexed.class))
                    locals.add(new Local(property, accessor.getReturnType(), found, current));
            }

            claimed.addAll(declared);
        }

        return List.copyOf(locals);
    }

    /**
     * Whether a method is an accessor a declaration can sit on.
     *
     * <p>Anything taking an argument or answering nothing reads no one property, and a bridge the
     * compiler wrote carries a copy of the annotation that would declare the same thing twice.
     *
     * @param accessor the method to judge
     * @return {@code true} when it reads one property of its instance
     */
    private static boolean reads(@NotNull Method accessor) {
        return !Modifier.isStatic(accessor.getModifiers())
            && !accessor.isSynthetic()
            && !accessor.isBridge()
            && accessor.getParameterCount() == 0
            && accessor.getReturnType() != void.class
            && (accessor.isAnnotationPresent(Indexed.class) || accessor.isAnnotationPresent(Indexed.Declarations.class));
    }

    /**
     * Builds one class's full schema, reaching through the fields that hold another indexed object.
     *
     * @param type the class to resolve
     * @return its declarations, or {@link #EMPTY}
     * @throws IllegalArgumentException if the declarations disagree with each other
     */
    private static @NotNull IndexSchema resolve(@NotNull Class<?> type) {
        List<Local> locals = LOCAL.get(type);

        if (locals.isEmpty())
            return EMPTY;

        Map<List<PropertyReference>, Declaration> declarations = new LinkedHashMap<>();
        Map<String, List<Local>> groups = new LinkedHashMap<>();

        for (Local local : locals) {
            if (!local.declared().group().isEmpty()) {
                groups.computeIfAbsent(local.declared().group(), name -> new ArrayList<>()).add(local);
                continue;
            }

            declare(declarations, type, List.of(reference(type, local.property())), local.declared().unique());

            // Whatever the field's own type declares is reachable through it, and never unique:
            // one department having one name says nothing about how many people hold it.
            for (List<String> path : reachableThrough(local, MAX_HOPS - 1))
                declare(declarations, type, List.of(reference(type, path)), false);
        }

        groups.forEach((name, grouped) -> declare(declarations, type, name, grouped));

        // Locals are read most derived first, so the first one sits on the most derived declaring
        // class.
        return declarations.isEmpty() ? EMPTY : new IndexSchema(Map.copyOf(declarations), locals.getFirst().declaredOn());
    }

    /**
     * Walks the paths reachable by reading one field and then reading on from what it holds.
     *
     * @param local the field being read through
     * @param remaining how many accessors may still be appended
     * @return one path per index reachable through this field, each already prefixed by it
     */
    private static @NotNull List<List<String>> reachableThrough(@NotNull Local local, int remaining) {
        // A field holding many values reaches many rows for one element, which is a join rather
        // than a property path. The field itself is still indexed, by containment.
        if (remaining <= 0 || holdsMany(local.type()))
            return List.of();

        List<List<String>> reached = new ArrayList<>();

        for (List<String> path : pathsUnder(local.type(), remaining))
            reached.add(prefixed(local.property(), path));

        return reached;
    }

    /**
     * Walks the paths one class exposes, and the paths reachable on through them.
     *
     * <p>Only single-component declarations are exported. A composite is a key over several values
     * of one element, and prefixing it would claim the holder can probe several values of a value
     * it does not own.
     *
     * @param type the class being reached into
     * @param remaining how many accessors may still be appended
     * @return one property path per index reachable from here
     */
    private static @NotNull List<List<String>> pathsUnder(@NotNull Class<?> type, int remaining) {
        if (remaining <= 0)
            return List.of();

        List<List<String>> paths = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Local local : LOCAL.get(type)) {
            if (!local.declared().group().isEmpty())
                continue;

            if (seen.add(local.property()))
                paths.add(List.of(local.property()));

            // A cycle is bounded by the hop count rather than refused, because a field reaching
            // back to its holder is an ordinary shape and the paths through it are still real.
            for (List<String> path : reachableThrough(local, remaining - 1)) {
                if (seen.add(String.join(".", path)))
                    paths.add(path);
            }
        }

        return paths;
    }

    /**
     * Whether a field holds many values rather than one.
     *
     * @param type the field's declared type
     * @return {@code true} for a collection, a map or an array
     */
    private static boolean holdsMany(@NotNull Class<?> type) {
        return Iterable.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || type.isArray();
    }

    /**
     * Prepends one step to a path.
     *
     * @return the path reached through {@code step}
     */
    private static @NotNull List<String> prefixed(@NotNull String step, @NotNull List<String> path) {
        List<String> prefixed = new ArrayList<>(path.size() + 1);
        prefixed.add(step);
        prefixed.addAll(path);
        return prefixed;
    }

    /**
     * Names one component of a declaration.
     *
     * @return the component as a reference off the declaring class
     */
    private static @NotNull PropertyReference reference(@NotNull Class<?> type, @NotNull String property) {
        return PropertyReference.of(type, property);
    }

    /**
     * Names one multi-hop component of a declaration.
     *
     * @return the component as a reference off the declaring class
     */
    private static @NotNull PropertyReference reference(@NotNull Class<?> type, @NotNull List<String> path) {
        return PropertyReference.of(type, path.toArray(String[]::new));
    }

    /**
     * Reduces one group's members to a single composite declaration, or to nothing when the class
     * sees only one of them.
     *
     * @throws IllegalArgumentException if the members disagree on uniqueness or share a position
     */
    private static void declare(@NotNull Map<List<PropertyReference>, Declaration> declarations, @NotNull Class<?> type, @NotNull String name, @NotNull List<Local> grouped) {
        // One value is no composite. It is how a group split across a hierarchy looks from the
        // class holding one member, or a group an override has taken a member out of, and a unique
        // there would promise a key nobody wrote.
        if (grouped.size() < 2)
            return;

        boolean unique = grouped.getFirst().declared().unique();

        for (Local local : grouped) {
            if (local.declared().unique() != unique)
                throw new IllegalArgumentException(String.format(
                    "Index group '%s' on '%s' is declared unique by some of its fields and not by others - uniqueness is a promise about the whole key",
                    name,
                    type.getSimpleName()
                ));
        }

        List<Local> ordered = new ArrayList<>(grouped);
        ordered.sort(Comparator.comparingInt(local -> local.declared().order()));
        List<PropertyReference> components = new ArrayList<>(ordered.size());

        for (int position = 0; position < ordered.size(); position++) {
            Local local = ordered.get(position);

            if (position > 0 && local.declared().order() == ordered.get(position - 1).declared().order())
                throw new IllegalArgumentException(String.format(
                    "Index group '%s' on '%s' gives position %d to both '%s' and '%s' - a composite key is probed in a fixed order, so each field needs its own",
                    name,
                    type.getSimpleName(),
                    local.declared().order(),
                    ordered.get(position - 1).property(),
                    local.property()
                ));

            components.add(reference(type, local.property()));
        }

        declare(declarations, type, components, unique);
    }

    /**
     * Records one declaration, refusing a second one that names the same tuple and disagrees.
     *
     * @throws IllegalArgumentException if the tuple is already declared with a different promise
     */
    private static void declare(@NotNull Map<List<PropertyReference>, Declaration> declarations, @NotNull Class<?> type, @NotNull List<PropertyReference> components, boolean unique) {
        Declaration existing = declarations.get(components);

        if (existing == null) {
            declarations.put(List.copyOf(components), new Declaration(List.copyOf(components), unique));
            return;
        }

        if (existing.unique() != unique)
            throw new IllegalArgumentException(String.format(
                "Index '%s' on '%s' is declared both unique and not unique",
                describe(components),
                type.getSimpleName()
            ));
    }

    /**
     * Names a component tuple the way a reader would write it.
     *
     * @return the tuple as a comma-separated list of dotted paths
     */
    private static @NotNull String describe(@NotNull List<PropertyReference> components) {
        List<String> named = new ArrayList<>(components.size());
        components.forEach(component -> named.add(String.join(".", component.properties())));
        return String.join(", ", named);
    }

    /**
     * One {@link Indexed} annotation together with the property carrying it.
     *
     * @param property the property's name, which is the field's or the one the accessor's name
     *        strips down to
     * @param type the type the property holds, declared by the field or answered by the accessor
     * @param declared the annotation read off it
     * @param declaredOn the class whose own field or accessor carries the annotation
     */
    private record Local(@NotNull String property, @NotNull Class<?> type, @NotNull Indexed declared, @NotNull Class<?> declaredOn) {}

    /**
     * One index a class declares.
     *
     * @param components the property tuple the index is built and probed over, one entry per value
     *        a query compares
     * @param unique whether at most one element may carry any one value
     */
    record Declaration(@NotNull List<PropertyReference> components, boolean unique) {

        /**
         * Returns the tuple named the way a reader would write it.
         *
         * @return the description
         */
        @NotNull String describe() {
            return IndexSchema.describe(this.components());
        }

    }

}
