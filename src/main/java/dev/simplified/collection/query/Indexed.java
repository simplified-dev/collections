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
 * collection holds two different elements sharing a value fails to build rather than answering one
 * of them, wherever both of those elements made the promise. One element the collection holds twice
 * is not two elements, and is answered as often as it is held.
 *
 * <p>Declarations are read from a class and its superclasses, never from the interfaces it
 * implements. An {@code @Indexed} on an interface's accessor declares nothing, and the implementing
 * class's override does not inherit it, so declare on the implementing class.
 *
 * <p>A persistence mapping declares nothing. {@code @Id}, {@code @Column(unique = true)} and the
 * association annotations of {@code jakarta.persistence} or {@code javax.persistence} say how rows
 * are stored rather than what code asks by, and a table's uniqueness constraint is no promise about
 * every collection its rows are held in. An entity queried by its id carries {@code @Indexed}
 * beside {@code @Id}.
 *
 * <p>The most derived declaration of a property is the one read: a shadowing field or an annotated
 * override restates the property, and nothing a supertype declares about it applies, {@link #unique}
 * included. An override carrying no {@code @Indexed} restates nothing, so the supertype's declaration
 * stands. Restating a member of a group outside that group breaks the group on the class restating
 * it, as {@link #group} describes.
 *
 * <p>A class contradicting itself fails fast rather than scanning in silence. One property declared
 * both {@link #unique} and not, two members of a group given one {@link #order}, or members of a
 * group sitting on one class that disagree on {@link #unique} throw an
 * {@link IllegalArgumentException} naming the class and the property or group at fault, from the
 * first query against a collection of that class and from every query after it. The throw depends
 * on the declarations alone, never on what the elements hold, so the first test touching the class
 * catches it.
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
 * <h2>Collections holding more than one class</h2>
 *
 * <p>A collection reads its declarations off the class of its first element. When that class
 * declares nothing of its own - a runtime proxy of an entity, or a subclass that only overrides
 * behaviour - the widest class some element has, up to the one carrying the declarations, is read
 * instead. An index serves when every element is an instance of the class read, and the collection
 * is scanned otherwise, with the same answers. A proxy and the plain instances of its entity index
 * together in any order, and so do subclasses declaring nothing held beside an instance of the class
 * they inherit from. Siblings with no instance of their shared class present are scanned, which is
 * every mix of siblings under an abstract base, and so is a subclass declaring something of its own
 * ahead of an instance of a class above it. Declare what a mixed collection is queried by on the
 * class it is held as, and leave the subclasses held beside it declaring nothing of their own.
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
     * an index on its own. A group needs two or more members on the class being read, and a class
     * seeing only one member of a group declares nothing for it.
     *
     * <p>A group may span a hierarchy, and a subclass may widen it with members of its own. A
     * shadowing field or an annotated override restating a member without this group takes the
     * member out, and the group declares nothing at all on the class it sits on, since the members
     * left behind would be a narrower key than anybody wrote. Restating a member in the same group
     * keeps the key, and the members on the most derived class holding any of them decide whether
     * it is {@link #unique}, so such an override can promise the whole key or stop promising it.
     * The members on that class must agree with one another.
     */
    @NotNull String group() default "";

    /**
     * Position of this field within its group's key, ignored outside a group. Every member of a
     * group carries a distinct position.
     */
    int order() default 0;

    /**
     * Whether at most one element may carry any one value of this index.
     *
     * <p>Each element makes this promise through its own class, and two elements sharing a value
     * break it only when both made it. An element of a subclass that restates the property without
     * {@code unique}, or widens a group into a larger key, promised nothing about this one, so where
     * it shares a value with an instance of the class above it the query answers from a scan rather
     * than failing.
     *
     * <p>Only two different objects can share a value. A collection holding one element twice holds
     * one row twice, which carries its value once, so the query answers that element as often as
     * the collection holds it.
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
