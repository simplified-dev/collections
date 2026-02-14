package dev.sbs.api.scheduler;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Scheduler implements Executor {

    private final @NotNull ScheduledExecutorService syncExecutor;
    private final @NotNull ScheduledExecutorService asyncExecutor;
    private final @NotNull ConcurrentList<ScheduledTask> tasks = Concurrent.newList();

    public Scheduler() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public Scheduler(int corePoolSize) {
        this.syncExecutor = Executors.newSingleThreadScheduledExecutor();
        this.asyncExecutor = Executors.newScheduledThreadPool(corePoolSize);

        // Schedule Permanent Cleaner
        new ScheduledTask(
            this.syncExecutor,
            () -> this.tasks.removeIf(ScheduledTask::isDone),
            1_000,
            30_000,
            TimeUnit.MILLISECONDS
        );
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
        this.tasks.stream()
            .filter(scheduledTask -> scheduledTask.getId() == id)
            .findFirst()
            .ifPresent(scheduledTask -> scheduledTask.cancel(mayInterruptIfRunning));
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
     * Repeats a task (synchronously) every 50 milliseconds.
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
        ScheduledTask scheduledTask = new ScheduledTask(
            async ? this.asyncExecutor : this.syncExecutor,
            task,
            initialDelay,
            repeatDelay,
            timeUnit
        );

        this.tasks.add(scheduledTask);
        return scheduledTask;
    }

    public void shutdown() {
        this.syncExecutor.shutdown();
        this.asyncExecutor.shutdown();
    }

    public boolean isShutdown() {
        return this.syncExecutor.isShutdown() && this.asyncExecutor.isShutdown();
    }

    public boolean isTerminated() {
        return this.syncExecutor.isTerminated() && this.asyncExecutor.isTerminated();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.asyncExecutor.execute(command);
    }

}
