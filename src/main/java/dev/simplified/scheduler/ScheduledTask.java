package dev.sbs.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled Task for the {@link Scheduler}.
 */
@Getter
public final class ScheduledTask implements Runnable {

    private static volatile int currentId = 1;

    /**
     * Get the time the task was created.
     */
    private final long addedTime = System.currentTimeMillis();
    /**
     * Get the id of the task.
     */
    private final int id;
    /**
     * Get the delay (in milliseconds) before the task will run.
     */
    private final long initialDelay;
    /**
     * Get the delay (in milliseconds) before the task will repeat.
     */
    private final long repeatDelay;
    /**
     * Is this an asynchronous task?
     */
    private final boolean async;
    /**
     * The TimeUnit used for {@link #getInitialDelay()} and {@link #getRepeatDelay()}.
     */
    private final @NotNull TimeUnit timeUnit;
    /**
     * Is this task currently running?
     */
    private boolean running;
    /**
     * Will this task run repeatedly?
     */
    private boolean repeating;
    /**
     * Get the number of consecutive errors.
     */
    private int consecutiveErrors = 0;

    @Getter(AccessLevel.NONE)
    private final @NotNull Runnable runnableTask;
    @Getter(AccessLevel.NONE)
    private final @NotNull ScheduledFuture<?> scheduledFuture;
    @Getter(AccessLevel.NONE)
    private final @NotNull Object lock = new Object();

    /**
     * Creates a new Scheduled Task.
     *
     * @param task         The task to run.
     * @param initialDelay The initialDelay (in ticks) to wait before the task is ran.
     * @param repeatDelay  The initialDelay (in ticks) to wait before calling the task again.
     * @param async        If the task should be run asynchronously.
     */
    ScheduledTask(@NotNull ScheduledExecutorService executorService, @NotNull  final Runnable task, long initialDelay, long repeatDelay, boolean async, @NotNull TimeUnit timeUnit) {
        synchronized (this.lock) {
            this.id = currentId++;
        }

        this.runnableTask = task;
        this.initialDelay = initialDelay;
        this.repeatDelay = repeatDelay;
        this.async = async;
        this.timeUnit = timeUnit;
        this.repeating = this.repeatDelay > 0;

        // Schedule Task
        if (this.isRepeating())
            this.scheduledFuture = executorService.scheduleWithFixedDelay(this, initialDelay, repeatDelay, timeUnit);
        else
            this.scheduledFuture = executorService.schedule(this, initialDelay, timeUnit);
    }

    /**
     * Will attempt to cancel this task.
     */
    public void cancel() {
        this.cancel(false);
    }

    /**
     * Will attempt to cancel this task, even if running.
     */
    public void cancel(boolean mayInterruptIfRunning) {
        // Attempt Cancellation
        this.scheduledFuture.cancel(mayInterruptIfRunning);

        if (this.scheduledFuture.isDone()) {
            this.repeating = false;
            this.running = false;
        }
    }

    /**
     * Gets if the current task is done.
     *
     * @return True if the task has completed normally, encountered an exception or cancelled.
     */
    public boolean isDone() {
        return this.scheduledFuture.isDone();
    }

    /**
     * Gets if the current task is canceled.
     *
     * @return True if the task is canceled.
     */
    public boolean isCanceled() {
        return this.scheduledFuture.isCancelled();
    }

    @Override
    public void run() {
        try {
            // Run Task
            this.running = true;

            if (this.isAsync())
                this.runnableTask.run();
            else {
                synchronized (this.lock) {
                    this.runnableTask.run();
                }
            }

            this.consecutiveErrors = 0;
        } catch (Exception ignore) {
            this.consecutiveErrors++;
        } finally {
            this.running = false;
        }
    }

}
