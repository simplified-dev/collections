package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a property worth spending memory on, so a query naming it answers in constant time instead
 * of scanning.
 *
 * <h2>Declaring one</h2>
 *
 * <p>Indexing is opt-in per property because it costs memory proportional to the collection, so a
 * property carrying no {@code @Indexed} is scanned exactly as it is today. It goes on the field or
 * on the accessor, whichever the class keeps its mapping on, and a query naming either resolves to
 * it - {@code Stat::getId}, {@code stat -> stat.getId()} and a fluent {@code Stat::id} all read the
 * property {@code id}. Declaring on both is one declaration, not two, which is also what a record
 * does on its own by propagating a component's annotation to its field and its accessor alike.
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
 * answer both a query about it alone and a query about it together with {@code tier}. {@link #unique}
 * is a promise about the elements rather than a hint about the schema: an index declared unique whose
 * collection holds two elements sharing a value fails to build rather than answering one of them.
 *
 * <h2>Reaching a property of a property</h2>
 *
 * <p>Indexing a field that holds another object also indexes what that object declares about itself,
 * so {@code findFirst(person -> person.getDepartment().getName(), "eng")} is a hash probe and so is a
 * query by the department itself. No path is written down anywhere - the first step is the field this
 * annotation sits on and the rest is whatever the target declares - so renaming a field on either
 * side moves the index with it and a misspelling is not expressible. Both ends have already opted in,
 * the target saying what it is worth finding by and the holder saying it cares about that field, so
 * nothing further is asked for.
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
 *     @Indexed
 *     private Department department;
 *
 * }
 * }</pre>
 *
 * <p>Three limits keep that honest. A path may be at most three accessors long, past which a query
 * scans, which is what stops a graph of references from declaring a set no reader can hold in their
 * head. A reached index is never unique however the target declared it, because a promise that no two
 * departments share a name says nothing about how many people share a department. A composite the
 * target declares is not reached at all, being a key over several values of one department that a
 * person cannot probe by naming several values of something it merely points at.
 *
 * <p>An index is rebuilt when the collection holding the elements is written, and a value reached
 * through a field is not part of that collection - a department renaming itself leaves an index over
 * {@code department.name} describing the name it used to have. Index a reference whose own indexed
 * properties are effectively final, which is the requirement a hash key already carries, held over a
 * wider surface.
 *
 * @see Indexable
 * @see PropertyReference
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Indexed.Declarations.class)
public @interface Indexed {

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
     * Holder for the repeated form, so one property can join more than one index.
     */
    @Target({ ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Declarations {

        /**
         * The declarations made on one property.
         */
        @NotNull Indexed[] value();

    }

}
