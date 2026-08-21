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
 * @see Indexable
 * @see PropertyReference
 */
@Target(ElementType.FIELD)
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
