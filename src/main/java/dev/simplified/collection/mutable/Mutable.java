package dev.sbs.api.collection.mutable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Provides mutable access to a value.
 * <p>
 * {@code Mutable} is used as a generic interface to the implementations in this package.
 * <p>
 * A typical use case would be to enable a primitive or string to be passed to a method and allow that method to
 * effectively change the value of the primitive/string. Another use case is to store a frequently changing primitive in
 * a collection (for example a total in a map) without needing to create new Integer/Long wrapper objects.
 *
 * @param <T> the type to set and get
 */
public interface Mutable<T> {

    /**
     * Gets the value of this mutable.
     *
     * @return the stored value
     */
    T get();

    /**
     * Sets the value of this mutable.
     *
     * @param value the value to store
     * @throws NullPointerException if the object is null and null is invalid
     * @throws ClassCastException if the type is invalid
     */
    void set(T value);

    /**
     * Creates a new instance of {@code Mutable}.
     *
     * @param <T> the type of the value to be stored in the mutable instance
     * @return a new {@code Mutable} instance containing the specified value
     */
    static <T> @NotNull Mutable<T> of() {
        return new Impl<>();
    }

    /**
     * Creates a new instance of {@code Mutable} with an initial value.
     *
     * @param <T> the type of the value to be stored in the mutable instance
     * @param value the initial value may be null
     * @return a new {@code Mutable} instance containing the specified value
     */
    static <T> @NotNull Mutable<T> of(@Nullable T value) {
        return new Impl<>(value);
    }

    /**
     * A mutable {@code Object} wrapper.
     *
     * @param <T> the type to set and get
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(fluent = true)
    class Impl<T> implements Mutable<T>, Serializable {

        private @Nullable T value;

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public void set(final T value) {
            this.value = value;
        }

        /**
         * <p>
         * Compares this object against the specified object. The result is {@code true} if and only if the argument
         * is not {@code null} and is a {@code MutableObject} object that contains the same {@code T}
         * value as this object.
         * </p>
         *
         * @param obj  the object to compare with, {@code null} returns {@code false}
         * @return  {@code true} if the objects are the same;
         *          {@code true} if the objects have equivalent {@code value} fields;
         *          {@code false} otherwise.
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == null)
                return false;

            if (this == obj)
                return true;

            if (this.getClass() == obj.getClass()) {
                final Impl<?> that = (Impl<?>) obj;
                return Objects.equals(this.value, that.value);
            }

            return false;
        }

        /**
         * Returns the value's hash code or {@code 0} if the value is {@code null}.
         */
        @Override
        public int hashCode() {
            return this.value == null ? 0 : this.value.hashCode();
        }

        /**
         * Returns the String value of this mutable.
         */
        @Override
        public String toString() {
            return this.value == null ? "null" : this.value.toString();
        }

    }

}
