package dev.simplified.collection.unmodifiable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A no-op {@link ReadWriteLock} used by snapshot {@code ConcurrentUnmodifiable*} wrappers
 * to bypass synchronization on read paths.
 *
 * <p>Snapshot wrappers own a freshly cloned, never-mutated backing collection; the JMM
 * already guarantees safe publication via {@code final} field assignment, so acquiring a
 * real lock on every read would be pure overhead. Returning lock instances whose
 * {@code lock}/{@code unlock} are no-ops keeps the base class read paths unchanged while
 * making them effectively wait-free.</p>
 *
 * <p>The mutation paths in unmodifiable wrappers throw {@link UnsupportedOperationException}
 * before reaching {@link #writeLock()}, so the write lock is never actually exercised.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class NoOpReadWriteLock implements ReadWriteLock, Serializable {

    public static final @NotNull NoOpReadWriteLock INSTANCE = new NoOpReadWriteLock();

    private static final @NotNull Lock NO_OP_LOCK = new NoOpLock();

    @Override
    public @NotNull Lock readLock() {
        return NO_OP_LOCK;
    }

    @Override
    public @NotNull Lock writeLock() {
        return NO_OP_LOCK;
    }

    /**
     * Returns the canonical singleton instance after deserialization, preserving the
     * {@code INSTANCE == deserialized} invariant that consumers rely on.
     *
     * @return the canonical {@link #INSTANCE}
     */
    @Serial
    private Object readResolve() {
        return INSTANCE;
    }

    private static final class NoOpLock implements Lock {

        @Override
        public void lock() {}

        @Override
        public void lockInterruptibly() {}

        @Override
        public boolean tryLock() {
            return true;
        }

        @Override
        public boolean tryLock(long time, @NotNull TimeUnit unit) {
            return true;
        }

        @Override
        public void unlock() {}

        @Override
        public @NotNull Condition newCondition() {
            throw new UnsupportedOperationException("Conditions not supported on NoOpReadWriteLock");
        }

    }

}
