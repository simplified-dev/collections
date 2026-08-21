package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
     * How many {@link Indexed#follow} steps a chain may take before the schema stops resolving it.
     *
     * <p>A bound is needed because references form a graph rather than a tree - a department holding
     * its head and a person holding their department is an ordinary shape and an unbounded walk
     * would never finish. Three hops is past anything a query in this codebase writes.
     */
    private static final int MAX_DEPTH = 3;

    /**
     * The answer for a class declaring nothing, which every query falls through to a scan against.
     */
    static final IndexSchema EMPTY = new IndexSchema(Map.of());

    /**
     * What each class declares about itself, with nothing followed.
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

    private IndexSchema(@NotNull Map<List<PropertyReference>, Declaration> declarations) {
        this.declarations = declarations;
        Map<Set<PropertyReference>, Declaration> byComponents = new LinkedHashMap<>();
        declarations.forEach((key, declaration) -> byComponents.putIfAbsent(Set.copyOf(key), declaration));
        this.byComponents = Map.copyOf(byComponents);
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
     * Collects every {@link Indexed} annotation on a class and its supertypes, without following
     * any of them.
     *
     * @param type the class to read
     * @return one entry per annotation, most derived first
     */
    private static @NotNull List<Local> readLocal(@NotNull Class<?> type) {
        List<Local> locals = new ArrayList<>();

        // Most derived first, so a shadowing field wins the way a field read would.
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                for (Indexed declared : field.getAnnotationsByType(Indexed.class))
                    locals.add(new Local(field.getName(), field.getType(), declared));
            }
        }

        return List.copyOf(locals);
    }

    /**
     * Builds one class's full schema, following the fields that ask to be followed.
     *
     * @param type the class to resolve
     * @return its declarations, or {@link #EMPTY}
     * @throws IllegalArgumentException if the declarations disagree with each other, or a
     *         collection-typed field asks to be followed
     */
    private static @NotNull IndexSchema resolve(@NotNull Class<?> type) {
        List<Local> locals = LOCAL.get(type);

        if (locals.isEmpty())
            return EMPTY;

        Map<List<PropertyReference>, Declaration> declarations = new LinkedHashMap<>();
        Map<String, List<Local>> groups = new LinkedHashMap<>();

        for (Local local : locals) {
            if (local.declared().follow())
                follow(declarations, type, local);
            else if (local.declared().group().isEmpty())
                declare(declarations, type, List.of(reference(type, local.property())), local.declared().unique());
            else
                groups.computeIfAbsent(local.declared().group(), name -> new ArrayList<>()).add(local);
        }

        groups.forEach((name, grouped) -> declare(declarations, type, name, grouped));
        return declarations.isEmpty() ? EMPTY : new IndexSchema(Map.copyOf(declarations));
    }

    /**
     * Adds one declaration per index the followed field's own type declares, each reached through
     * that field.
     *
     * @throws IllegalArgumentException if the field holds many values rather than one
     */
    private static void follow(@NotNull Map<List<PropertyReference>, Declaration> declarations, @NotNull Class<?> type, @NotNull Local local) {
        if (Iterable.class.isAssignableFrom(local.type()) || Map.class.isAssignableFrom(local.type()) || local.type().isArray())
            throw new IllegalArgumentException(String.format(
                "Field '%s' on '%s' holds many values, so following it reaches many rows for one element - that is a join rather than a property path, and an index over it is not built",
                local.property(),
                type.getSimpleName()
            ));

        for (List<String> path : pathsUnder(local.type(), 1))
            declare(declarations, type, List.of(reference(type, prefixed(local.property(), path))), false);
    }

    /**
     * Walks the paths one class exposes, and the paths reachable through the fields it follows.
     *
     * <p>Only single-component declarations are exported. A composite is a key over several values
     * of one element, and prefixing it would claim the holder can probe several values of a value
     * it does not own.
     *
     * @param type the class being reached into
     * @param depth how many steps have already been taken
     * @return one property path per index reachable from here
     */
    private static @NotNull List<List<String>> pathsUnder(@NotNull Class<?> type, int depth) {
        if (depth > MAX_DEPTH)
            return List.of();

        List<List<String>> paths = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Local local : LOCAL.get(type)) {
            if (local.declared().follow()) {
                // A cycle is bounded by the depth rather than refused, because a field reaching back
                // to its holder is an ordinary shape and the paths through it are still real.
                if (Iterable.class.isAssignableFrom(local.type()) || Map.class.isAssignableFrom(local.type()) || local.type().isArray())
                    continue;

                for (List<String> path : pathsUnder(local.type(), depth + 1)) {
                    List<String> prefixed = prefixed(local.property(), path);

                    if (seen.add(String.join(".", prefixed)))
                        paths.add(prefixed);
                }
            } else if (local.declared().group().isEmpty() && seen.add(local.property()))
                paths.add(List.of(local.property()));
        }

        return paths;
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
     * Reduces one group's members to a single composite declaration.
     *
     * @throws IllegalArgumentException if the members disagree on uniqueness or share a position
     */
    private static void declare(@NotNull Map<List<PropertyReference>, Declaration> declarations, @NotNull Class<?> type, @NotNull String name, @NotNull List<Local> grouped) {
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
     * One {@link Indexed} annotation together with the field carrying it.
     *
     * @param property the field's name
     * @param type the field's declared type
     * @param declared the annotation read off it
     */
    private record Local(@NotNull String property, @NotNull Class<?> type, @NotNull Indexed declared) {}

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
