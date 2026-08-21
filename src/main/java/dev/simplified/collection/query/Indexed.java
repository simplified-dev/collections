package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field worth spending memory on, so a query naming it answers in constant time instead of
 * scanning.
 *
 * <p>An index is opt-in per field because it costs memory proportional to the collection. Nothing is
 * indexed by accident: a field gains an index because someone asked for one, and a query over a
 * field that carries no {@code @Indexed} scans exactly as it does today.
 *
 * <p>The field is named rather than the accessor, and a query naming either resolves to it -
 * {@code Stat::getId}, {@code stat -> stat.getId()} and a fluent {@code Stat::id} all read the
 * property {@code id}.
 *
 * <pre>{@code
 * public class Stat {
 *
 *     @Indexed(unique = true)
 *     private String id = "";
 *
 *     @Indexed
 *     @Indexed(group = "modeAndTier", order = 0)
 *     private String mode = "";
 *
 *     @Indexed(group = "modeAndTier", order = 1)
 *     private int tier;
 *
 * }
 * }</pre>
 *
 * <p>Repeating the annotation puts one field in more than one index, which is what lets {@code mode}
 * above answer both a query about it alone and a query about it together with {@code tier}.
 *
 * <p>{@link #unique} is a promise about the elements rather than a hint about the schema: an index
 * declared unique whose collection holds two elements sharing a value fails to build rather than
 * answering one of them.
 *
 * <h2>Reaching a property of a property</h2>
 *
 * <p>{@link #follow} indexes what the field's own type declares, so a query reading through one
 * object to a property of another answers from an index too:
 *
 * <pre>{@code
 * public class Department {
 *
 *     @Indexed(unique = true)
 *     private String name = "";
 *
 * }
 *
 * public class Person {
 *
 *     @Indexed                 // by the Department itself
 *     @Indexed(follow = true)  // and by everything Department declares
 *     private Department department;
 *
 * }
 * }</pre>
 *
 * <p>{@code findFirst(person -> person.getDepartment().getName(), "eng")} is then a hash probe
 * rather than a scan. No path is written down anywhere: the first step is the field this annotation
 * sits on and the rest is whatever the target class declares about itself, so renaming a field on
 * either side moves the index with it and a misspelling is not expressible.
 *
 * @see Indexable
 * @see PropertyReference
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Indexed.Declarations.class)
public @interface Indexed {

    /**
     * Whether to index what this field's own type declares, reached through this field, rather than
     * the field's value itself.
     *
     * <p>A derived index is never unique however the target declared it, because a promise that no
     * two departments share a name says nothing about how many people share a department.
     *
     * <p>Ignored on a field whose type declares nothing. Refused on a collection-typed field, where
     * one element reaches many values and the answer is a join rather than a path.
     *
     * <p>An index is rebuilt when the collection holding the elements is written, and a followed
     * value is not part of that collection - a department renaming itself leaves an index over
     * {@code department.name} describing the name it used to have. Follow a reference whose indexed
     * properties are effectively final, which is the same requirement a hash key already carries,
     * held over a wider surface.
     */
    boolean follow() default false;

    /**
     * Name joining this field to the other fields of one composite index, empty when the field is
     * an index on its own.
     */
    @NotNull String group() default "";

    /**
     * Position of this field within its group's key, ignored outside a group. Every member of a
     * group carries a distinct position.
     */
    int order() default 0;

    /**
     * Whether at most one element may carry any one value of this index.
     */
    boolean unique() default false;

    /**
     * Holder for the repeated form, so one field can join more than one index.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Declarations {

        /**
         * The declarations made on one field.
         */
        @NotNull Indexed[] value();

    }

}
