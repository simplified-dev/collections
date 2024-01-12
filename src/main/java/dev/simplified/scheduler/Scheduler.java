package dev.sbs.api.scheduler;

import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Scheduler implements ScheduledExecutorService {

    private final @NotNull ScheduledExecutorService internalExecutor;
    private final @NotNull ConcurrentList<ScheduledTask> tasks = Concurrent.newList();
    private final @NotNull Object lock = new Object();

    public Scheduler() {
        this(1);
    }

    public Scheduler(int corePoolSize) {
        this.internalExecutor = Executors.newScheduledThreadPool(corePoolSize);

        // Schedule Permanent Cleaner
        new ScheduledTask(this, () -> this.tasks.forEach(scheduledTask -> {
            if (scheduledTask.isDone())
                this.tasks.remove(scheduledTask);
        }), 1_000, 30_000, false, TimeUnit.MILLISECONDS);
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors.
     *
     * @param millis the length of time to sleep in milliseconds
     * @throws IllegalArgumentException if the value of millis is negative
     */
    public static void sleep(@Range(from = 0, to = Long.MAX_VALUE) long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) { }
    }

    public void cancel(@Range(from = 1, to = Long.MAX_VALUE) long id) {
        this.cancel(id, false);
    }

    public void cancel(@Range(from = 1, to = Long.MAX_VALUE) long id, boolean mayInterruptIfRunning) {
        this.tasks.forEach(scheduledTask -> {
            if (scheduledTask.getId() == id)
                this.cancel(scheduledTask, mayInterruptIfRunning);
        });
    }

    public void cancel(@NotNull ScheduledTask task) {
        this.cancel(task, false);
    }

    public void cancel(@NotNull ScheduledTask task, boolean mayInterruptIfRunning) {
        task.cancel(mayInterruptIfRunning);
    }

    public @NotNull ConcurrentList<ScheduledTask> getTasks() {
        return Concurrent.newUnmodifiableList(this.tasks);
    }

    /**
     * Repeats a task (synchronously) every 50 milliseconds.<br><br>
     * <p>
     * Warning: This method is run on the main thread, don't do anything heavy.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask repeat(@NotNull Runnable task) {
        return this.schedule(task, 0, 50);
    }

    /**
     * Repeats a task (asynchronously) every 50 milliseconds.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask repeatAsync(@NotNull Runnable task) {
        return this.scheduleAsync(task, 0, 50);
    }

    /**
     * Runs a task (synchronously).
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask schedule(@NotNull Runnable task) {
        return this.schedule(task, 0);
    }

    /**
     * Runs a task (synchronously).
     *
     * @param task  The task to run.
     * @param delay The delay (in milliseconds) to wait before the task runs.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask schedule(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long delay) {
        return this.schedule(task, delay, 0);
    }

    /**
     * Runs a task (synchronously).
     *
     * @param task         The task to run.
     * @param initialDelay The initial delay (in milliseconds) to wait before the task runs.
     * @param repeatDelay  The repeat delay (in milliseconds) to wait before running the task again.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask schedule(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long repeatDelay) {
        return this.schedule(task, initialDelay, repeatDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Runs a task (synchronously).
     *
     * @param task         The task to run.
     * @param initialDelay The initial delay to wait before the task runs.
     * @param repeatDelay  The repeat delay to wait before running the task again.
     * @param timeUnit     The unit of time for initialDelay and repeatDelay.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask schedule(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long repeatDelay, @NotNull TimeUnit timeUnit) {
        return this.scheduleTask(task, initialDelay, repeatDelay, false, timeUnit);
    }

    /**
     * Runs a task (asynchronously).
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask scheduleAsync(@NotNull Runnable task) {
        return this.scheduleAsync(task, 0);
    }

    /**
     * Runs a task (asynchronously).
     *
     * @param task         The task to run.
     * @param initialDelay The initial delay (in milliseconds) to wait before the task runs.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask scheduleAsync(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay) {
        return this.scheduleAsync(task, initialDelay, 0);
    }

    /**
     * Runs a task (asynchronously).
     *
     * @param task         The task to run.
     * @param initialDelay The initial delay (in milliseconds) to wait before the task runs.
     * @param repeatDelay  The repeat delay (in milliseconds) to wait before running the task again.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask scheduleAsync(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long repeatDelay) {
        return this.scheduleAsync(task, initialDelay, repeatDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Runs a task (asynchronously).
     *
     * @param task         The task to run.
     * @param initialDelay The initial delay to wait before the task runs.
     * @param repeatDelay  The repeat delay to wait before running the task again.
     * @param timeUnit     The unit of time for initialDelay and repeatDelay.
     * @return The scheduled task.
     */
    public @NotNull ScheduledTask scheduleAsync(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long repeatDelay, @NotNull TimeUnit timeUnit) {
        return this.scheduleTask(task, initialDelay, repeatDelay, true, timeUnit);
    }

    private @NotNull ScheduledTask scheduleTask(@NotNull Runnable task, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long repeatDelay, boolean async, @NotNull TimeUnit timeUnit) {
        synchronized (this.lock) {
            ScheduledTask scheduledTask = new ScheduledTask(this, task, initialDelay, repeatDelay, async, timeUnit);
            this.tasks.add(scheduledTask);
            return scheduledTask;
        }
    }

    @Override
    public @NotNull ScheduledFuture<?> schedule(@NotNull Runnable command, @Range(from = 0, to = Long.MAX_VALUE) long delay, @NotNull TimeUnit unit) {
        return this.internalExecutor.schedule(command, delay, unit);
    }

    @Override
    public <V> @NotNull ScheduledFuture<V> schedule(@NotNull Callable<V> callable, @Range(from = 0, to = Long.MAX_VALUE) long delay, @NotNull TimeUnit unit) {
        return this.internalExecutor.schedule(callable, delay, unit);
    }

    @Override
    public @NotNull ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long period, @NotNull TimeUnit unit) {
        return this.internalExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public @NotNull ScheduledFuture<?> scheduleWithFixedDelay(@NotNull Runnable command, @Range(from = 0, to = Long.MAX_VALUE) long initialDelay, @Range(from = 0, to = Long.MAX_VALUE) long delay, @NotNull TimeUnit unit) {
        return this.internalExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        this.internalExecutor.shutdown();
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        return this.internalExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.internalExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.internalExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(@Range(from = 0, to = Long.MAX_VALUE) long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return this.internalExecutor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> @NotNull Future<T> submit(@NotNull Callable<T> task) {
        return this.internalExecutor.submit(task);
    }

    @Override
    public <T> @NotNull Future<T> submit(@NotNull Runnable task, T result) {
        return this.internalExecutor.submit(task, result);
    }

    @Override
    public @NotNull Future<?> submit(@NotNull Runnable task) {
        return this.internalExecutor.submit(task);
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.internalExecutor.invokeAll(tasks);
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return this.internalExecutor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> @NotNull T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.internalExecutor.invokeAny(tasks);
    }

    @Override
    public <T> @NotNull T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.internalExecutor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.internalExecutor.execute(command);
    }

}
