package dev.sbs.api.scheduler;

/**
 * Scheduled Task for the Minecraft Scheduler.
 */
public final class ScheduledTask {

	private static volatile int currentId = 1;
	private static final Object anchor = new Object();
	private final long addedTime = System.currentTimeMillis();
	private long addedTicks = Scheduler.getInstance().getTotalTicks();
	private final int id;
	private long delay;
	private final long period;
	private final boolean async;
	private boolean running;
	private boolean canceled;
	private boolean repeating;
	private final Runnable task;
	private boolean calledOnce;

	/**
	 * Creates a new Scheduled Task.
	 *
	 * @param task The task to run.
	 * @param delay The delay (in ticks) to wait before the task is ran.
	 * @param period The delay (in ticks) to wait before calling the task again.
	 * @param async If the task should be run asynchronously.
	 */
	ScheduledTask(final Runnable task, long delay, long period, boolean async) {
		synchronized (anchor) {
			this.id = currentId++;
		}

		this.delay = delay;
		this.period = period;
		this.async = async;
		this.repeating = this.period > 0;

		this.task = () -> {
			this.calledOnce = true;
			this.running = true;
			task.run();
			this.running = false;
		};
	}

	/**
	 * Will attempt to cancel this task if running.
	 */
	public void cancel() {
		this.repeating = false;
		this.running = false;
		this.canceled = true;
	}

	/**
	 * Returns the added time for the task.
	 *
	 * @return When the task was added.
	 */
	public long getAddedTime() {
		return this.addedTime;
	}

	/**
	 * Returns the added ticks for the task.
	 *
	 * @return Ticks when the task was added.
	 */
	public long getAddedTicks() {
		return this.addedTicks;
	}

	/**
	 * Returns the id for the task.
	 *
	 * @return Task id number.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Returns the delay (in ticks) for the task.
	 *
	 * @return How long the task will wait to run.
	 */
	public long getDelay() {
		return this.delay;
	}

	/**
	 * Returns the delay (in ticks) for the task to repeat itself.
	 *
	 * @return How long until the task repeats itself.
	 */
	public long getPeriod() {
		return this.period;
	}

	/**
	 * Gets if the current task is an asynchronous task.
	 *
	 * @return True if the task is not run by main thread.
	 */
	public boolean isAsync() {
		return this.async;
	}

	/**
	 * Gets if the current task is canceled.
	 *
	 * @return True if the task is canceled.
	 */
	public boolean isCanceled() {
		return this.canceled;
	}

	/**
	 * Gets if the current task is completed.
	 *
	 * @return True if the task is completed.
	 */
	public boolean isCompleted() {
		return this.canceled || (this.calledOnce && !this.running && !this.repeating);
	}

	/**
	 * Gets if the current task is running.
	 *
	 * @return True if the task is running.
	 */
	public boolean isRunning() {
		return this.running;
	}

	void setDelay(int delay) {
		this.addedTicks = Scheduler.getInstance().getTotalTicks();
		this.delay = delay;
	}

	/**
	 * Starts the thread.
	 */
	void start() {
		if (this.isAsync())
			new Thread(this.task).start();
		else
			this.task.run();
	}

}