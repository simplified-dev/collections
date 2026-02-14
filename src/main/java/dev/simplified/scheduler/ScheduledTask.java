package dev.sbs.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a scheduled task that is executed by a {@link Scheduler}.
 * <p>
 * The task can be configured with an initial delay and a repeating period.
 * It provides mechanisms for canceling, tracking its state (e.g., running, done, or canceled),
 * and handling execution errors.
 * <p>
 * This class is immutable except for its internal state, such as
 * {@code running}, {@code repeating}, and {@code consecutiveErrors}, which are updated
 * during the lifecycle of the task.
 */
@Getter
@Log4j2
public final class ScheduledTask implements Runnable {

    private static AtomicLong currentId = new AtomicLong(1);

    /**
     * The time the task was created.
     */
    private final long addedTime = System.currentTimeMillis();

    /**
     * The id of the task.
     */
    private final long id;

    /**
     * The time (in milliseconds) before the task will run.
     */
    private final long initialDelay;

    /**
     * The time (in milliseconds) before the task will repeat.
     */
    private final long period;

    /**
     * The TimeUnit used for {@link #getInitialDelay()} and {@link #getPeriod()}.
     */
    private final @NotNull TimeUnit timeUnit;

    /**
     * Is this task currently running?
     */
    private volatile boolean running;

    /**
     * Will this task run repeatedly?
     */
    private volatile boolean repeating;

    /**
     * The number of consecutive errors.
     */
    private AtomicInteger consecutiveErrors = new AtomicInteger(0);

    @Getter(AccessLevel.NONE)
    private final @NotNull Runnable runnableTask;

    @Getter(AccessLevel.NONE)
    private final @NotNull ScheduledFuture<?> scheduledFuture;

    /**
     * Creates a new Scheduled Task.
     *
     * @param task         The task to run.
     * @param initialDelay The initialDelay (in ticks) to wait before the task is run.
     * @param period  The initialDelay (in ticks) to wait before calling the task again.
     */
    ScheduledTask(
        @NotNull ScheduledExecutorService executorService,
        @NotNull final Runnable task,
        @Range(from = 0, to = Long.MAX_VALUE) long initialDelay,
        @Range(from = 0, to = Long.MAX_VALUE) long period,
        @NotNull TimeUnit timeUnit
    ) {
        this.id = currentId.getAndIncrement();
        this.runnableTask = task;
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
        this.repeating = this.period > 0;

        // Schedule Task
        if (this.isRepeating())
            this.scheduledFuture = executorService.scheduleAtFixedRate(this, initialDelay, period, timeUnit);
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
            this.runnableTask.run();
            this.consecutiveErrors.set(0);
        } catch (Exception ex) {
            this.consecutiveErrors.incrementAndGet();
            log.error("Task {} failed: {}", this.id, ex.getMessage(), ex);
        } finally {
            this.running = false;
        }
    }

}
