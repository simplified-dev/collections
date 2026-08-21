package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What one class declares about how its instances can be found, read once off its {@link Indexed}
 * fields and held for the life of the class.
 *
 * <p>Reflection runs once per class rather than once per collection or once per query, because a
 * class's declarations cannot change while it is loaded.
 */
final class IndexSchema {

    /**
     * The answer for a class declaring nothing, which every query falls through to a scan against.
     */
    static final IndexSchema EMPTY = new IndexSchema(Map.of());

    private static final ClassValue<IndexSchema> SCHEMAS = new ClassValue<>() {

        @Override
        protected IndexSchema computeValue(@NotNull Class<?> type) {
            return read(type);
        }

    };

    private final @NotNull Map<List<String>, Declaration> declarations;

    /**
     * The same declarations keyed by their property set, so a query naming a composite's fields in
     * any argument order still finds it. Two composites over one set of fields in different orders
     * are legal, and the first one read answers here.
     */
    private final @NotNull Map<Set<String>, Declaration> byProperties;

    private IndexSchema(@NotNull Map<List<String>, Declaration> declarations) {
        this.declarations = declarations;
        Map<Set<String>, Declaration> byProperties = new LinkedHashMap<>();
        declarations.forEach((path, declaration) -> byProperties.putIfAbsent(Set.copyOf(path), declaration));
        this.byProperties = Map.copyOf(byProperties);
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
     * Finds the declaration covering one property path.
     *
     * @param path the property path to probe by
     * @return the declaration, or {@code null} when the path carries no index
     */
    @Nullable Declaration declaring(@NotNull List<String> path) {
        return this.declarations.get(path);
    }

    /**
     * Finds the declaration over exactly one set of properties, whatever order a query named them
     * in.
     *
     * @param properties the properties a query names
     * @return the declaration, or {@code null} when nothing is declared over exactly those
     */
    @Nullable Declaration covering(@NotNull List<String> properties) {
        Set<String> distinct = Set.copyOf(properties);

        // Two predicates over one property are not a composite key, they are a contradiction or a
        // redundancy, and either way the scan is the honest answer.
        if (distinct.size() != properties.size())
            return null;

        return this.byProperties.get(distinct);
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
     * Walks a class and its supertypes collecting every {@link Indexed} field.
     *
     * @param type the class to read
     * @return its declarations
     * @throws IllegalArgumentException if a group's members disagree on uniqueness or share a
     *         position, or if two declarations name one path and disagree on uniqueness
     */
    private static @NotNull IndexSchema read(@NotNull Class<?> type) {
        List<Member> members = new ArrayList<>();

        // Most derived first, so a shadowing field wins the way a field read would.
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                for (Indexed declared : field.getAnnotationsByType(Indexed.class))
                    members.add(new Member(field.getName(), declared));
            }
        }

        if (members.isEmpty())
            return EMPTY;

        Map<List<String>, Declaration> declarations = new LinkedHashMap<>();
        Map<String, List<Member>> groups = new LinkedHashMap<>();

        for (Member member : members) {
            if (member.declared().group().isEmpty())
                declare(declarations, type, List.of(member.property()), member.declared().unique());
            else
                groups.computeIfAbsent(member.declared().group(), name -> new ArrayList<>()).add(member);
        }

        groups.forEach((name, grouped) -> declare(declarations, type, name, grouped));
        return new IndexSchema(Map.copyOf(declarations));
    }

    /**
     * Reduces one group's members to a single composite declaration.
     *
     * @throws IllegalArgumentException if the members disagree on uniqueness or share a position
     */
    private static void declare(@NotNull Map<List<String>, Declaration> declarations, @NotNull Class<?> type, @NotNull String name, @NotNull List<Member> grouped) {
        boolean unique = grouped.getFirst().declared().unique();

        for (Member member : grouped) {
            if (member.declared().unique() != unique)
                throw new IllegalArgumentException(String.format(
                    "Index group '%s' on '%s' is declared unique by some of its fields and not by others - uniqueness is a promise about the whole key",
                    name,
                    type.getSimpleName()
                ));
        }

        List<Member> ordered = new ArrayList<>(grouped);
        ordered.sort(Comparator.comparingInt(member -> member.declared().order()));
        List<String> path = new ArrayList<>(ordered.size());

        for (int position = 0; position < ordered.size(); position++) {
            Member member = ordered.get(position);

            if (position > 0 && member.declared().order() == ordered.get(position - 1).declared().order())
                throw new IllegalArgumentException(String.format(
                    "Index group '%s' on '%s' gives position %d to both '%s' and '%s' - a composite key is probed in a fixed order, so each field needs its own",
                    name,
                    type.getSimpleName(),
                    member.declared().order(),
                    ordered.get(position - 1).property(),
                    member.property()
                ));

            path.add(member.property());
        }

        declare(declarations, type, path, unique);
    }

    /**
     * Records one declaration, refusing a second one that names the same path and disagrees.
     *
     * @throws IllegalArgumentException if the path is already declared with a different promise
     */
    private static void declare(@NotNull Map<List<String>, Declaration> declarations, @NotNull Class<?> type, @NotNull List<String> path, boolean unique) {
        Declaration existing = declarations.get(path);

        if (existing == null) {
            declarations.put(List.copyOf(path), new Declaration(PropertyReference.of(type, path.toArray(String[]::new)), unique));
            return;
        }

        if (existing.unique() != unique)
            throw new IllegalArgumentException(String.format(
                "Index '%s' on '%s' is declared both unique and not unique",
                String.join(", ", path),
                type.getSimpleName()
            ));
    }

    /**
     * One {@link Indexed} annotation together with the field carrying it.
     *
     * @param property the field's name
     * @param declared the annotation read off it
     */
    private record Member(@NotNull String property, @NotNull Indexed declared) {}

    /**
     * One index a class declares.
     *
     * @param reference the property tuple the index is built and probed over
     * @param unique whether at most one element may carry any one value
     */
    record Declaration(@NotNull PropertyReference reference, boolean unique) {

        /**
         * The property path this index is probed by.
         *
         * @return the path, one entry for a single-field index and several for a composite
         */
        @NotNull List<String> path() {
            return this.reference().properties();
        }

    }

}
