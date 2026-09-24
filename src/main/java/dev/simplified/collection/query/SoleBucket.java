package dev.simplified.collection.query;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * The sealed bucket a value only one element carries is answered from.
 *
 * <p>That is the shape of a reference table and of every unique index, so an index holds one of
 * these per row and what one costs is most of what holding an index costs at all. A single field is
 * sixteen bytes, where the narrowest immutable list the platform offers is twenty-four - that one
 * carries a second element slot it will never fill.
 *
 * <p>Every mutator refuses, so a bucket is handed to a caller as it stands rather than copied or
 * wrapped on the way out.
 *
 * @param <E> the element type
 */
final class SoleBucket<E> implements List<E>, RandomAccess, Serializable {

    private final E element;

    SoleBucket(@NotNull E element) {
        this.element = element;
    }

    @Override public boolean add(E element) { throw new UnsupportedOperationException(); }
    @Override public void add(int index, E element) { throw new UnsupportedOperationException(); }
    @Override public boolean addAll(@NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
    @Override public boolean addAll(int index, @NotNull Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
    @Override public void clear() { throw new UnsupportedOperationException(); }
    @Override public boolean remove(Object element) { throw new UnsupportedOperationException(); }
    @Override public E remove(int index) { throw new UnsupportedOperationException(); }
    @Override public boolean removeAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
    @Override public boolean removeIf(@NotNull Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
    @Override public void replaceAll(@NotNull UnaryOperator<E> operator) { throw new UnsupportedOperationException(); }
    @Override public boolean retainAll(@NotNull Collection<?> collection) { throw new UnsupportedOperationException(); }
    @Override public E set(int index, E element) { throw new UnsupportedOperationException(); }
    @Override public void sort(Comparator<? super E> comparator) { throw new UnsupportedOperationException(); }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object item) {
        // The item asks, the element answers, which is the side List names and the side every list
        // in the platform compares from.
        return Objects.equals(item, this.element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> items) {
        for (Object item : items) {
            if (!this.contains(item))
                return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int index) {
        if (index != 0)
            throw new IndexOutOfBoundsException(String.format("Index %d out of bounds for length 1", index));

        return this.element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object item) {
        return this.contains(item) ? 0 : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object item) {
        return this.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Iterator<E> iterator() {
        return this.listIterator(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ListIterator<E> listIterator(int index) {
        if (index < 0 || index > 1)
            throw new IndexOutOfBoundsException(String.format("Index %d out of bounds for length 1", index));

        return new Cursor(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<E> subList(int from, int to) {
        if (from < 0 || to > 1 || from > to)
            throw new IndexOutOfBoundsException(String.format("Range [%d, %d) out of bounds for length 1", from, to));

        return from == to ? List.of() : this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object @NotNull [] toArray() {
        return new Object[] { this.element };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] array) {
        T[] filled = array.length < 1
            ? (T[]) Array.newInstance(array.getClass().getComponentType(), 1)
            : array;

        filled[0] = (T) this.element;

        if (filled.length > 1)
            filled[1] = null;

        return filled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        return other instanceof List<?> list && list.size() == 1 && Objects.equals(this.element, list.getFirst());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 + Objects.hashCode(this.element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + this.element + "]";
    }

    /**
     * The cursor a walk over one element runs on.
     */
    private final class Cursor implements ListIterator<E> {

        private int at;

        private Cursor(int at) {
            this.at = at;
        }

        @Override public void add(E element) { throw new UnsupportedOperationException(); }
        @Override public void remove() { throw new UnsupportedOperationException(); }
        @Override public void set(E element) { throw new UnsupportedOperationException(); }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return this.at == 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E next() {
            if (this.at != 0)
                throw new NoSuchElementException();

            this.at = 1;
            return SoleBucket.this.element;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            return this.at == 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E previous() {
            if (this.at != 1)
                throw new NoSuchElementException();

            this.at = 0;
            return SoleBucket.this.element;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nextIndex() {
            return this.at;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int previousIndex() {
            return this.at - 1;
        }

    }

}
