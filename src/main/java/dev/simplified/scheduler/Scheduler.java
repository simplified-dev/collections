package dev.sbs.api.scheduler;

import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentList;

/**
 * Minecraft Scheduler for Minecraft Clients.
 */
public class Scheduler {

	private static final Scheduler INSTANCE = new Scheduler();
	private final ConcurrentList<ScheduledTask> tasks = Concurrent.newList();
	private final Object anchor = new Object();
	private volatile long currentTicks = 0;
	private volatile long totalTicks = 0;

	private Scheduler() { }

	/*@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			if (this.getCurrentTicks() == 20)
				this.currentTicks = -1;

			synchronized (this.anchor) {
				this.totalTicks++;
				this.currentTicks++;
			}

			if (MinecraftReflection.isClientInstantiated()) {
				this.tasks.removeIf(ScheduledTask::isCompleted);

				for (ScheduledTask scheduledTask : this.tasks) {
					if (this.getTotalTicks() >= (scheduledTask.getAddedTicks() + scheduledTask.getDelay())) {
						if (!scheduledTask.isCompleted() && !scheduledTask.isRunning()) {
							scheduledTask.start();

							if (scheduledTask.getPeriod() > 0)
								scheduledTask.setDelay(scheduledTask.getPeriod());
						}
					}
				}
			}
		}
	}*/

	public synchronized void cancel(int id) {
		this.tasks.forEach(scheduledTask -> {
			if (scheduledTask.getId() == id)
				scheduledTask.cancel();
		});

		this.tasks.forEach(scheduledTask -> {
			if (scheduledTask.getId() == id)
				scheduledTask.cancel();
		});
	}

	public void cancel(ScheduledTask task) {
		task.cancel();
	}

	public synchronized long getCurrentTicks() {
		return this.currentTicks;
	}

	public static Scheduler getInstance() {
		return INSTANCE;
	}

	public synchronized long getTotalTicks() {
		return this.totalTicks;
	}

	/**
	 * Repeats a task (synchronously) every tick.<br><br>
	 *
	 * Warning: This method is ran on the main thread, don't do anything heavy.
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask repeat(Runnable task) {
		return this.schedule(task, 0, 1);
	}

	/**
	 * Repeats a task (asynchronously) every tick.
	 *
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask repeatAsync(Runnable task) {
		return this.runAsync(task, 0, 1);
	}

	/**
	 * Runs a task (asynchronously) on the next tick.
	 *
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask runAsync(Runnable task) {
		return this.runAsync(task, 0);
	}

	/**
	 * Runs a task (asynchronously) on the next tick.
	 *
	 * @param task The task to run.
	 * @param delay The delay (in ticks) to wait before the task is ran.
	 * @return The scheduled task.
	 */
	public ScheduledTask runAsync(Runnable task, long delay) {
		return this.runAsync(task, delay, 0);
	}

	/**
	 * Runs a task (asynchronously) on the next tick.
	 *
	 * @param task The task to run.
	 * @param delay The delay (in ticks) to wait before the task is ran.
	 * @param period The delay (in ticks) to wait before calling the task again.
	 * @return The scheduled task.
	 */
	public ScheduledTask runAsync(Runnable task, long delay, long period) {
		ScheduledTask scheduledTask = new ScheduledTask(task, delay, period, true);
		this.tasks.add(scheduledTask);
		return scheduledTask;
	}

	/**
	 * Runs a task (synchronously) on the next tick.
	 *
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask schedule(Runnable task) {
		return this.schedule(task, 0);
	}

	/**
	 * Runs a task (synchronously) on the next tick.
	 *
	 * @param task The task to run.
	 * @param delay The delay (in ticks) to wait before the task is ran.
	 * @return The scheduled task.
	 */
	public ScheduledTask schedule(Runnable task, long delay) {
		return this.schedule(task, delay, 0);
	}

	/**
	 * Runs a task (synchronously) on the next tick.
	 *
	 * @param task The task to run.
	 * @param delay The delay (in ticks) to wait before the task is ran.
	 * @param period The delay (in ticks) to wait before calling the task again.
	 * @return The scheduled task.
	 */
	public ScheduledTask schedule(Runnable task, long delay, long period) {
		ScheduledTask scheduledTask = new ScheduledTask(task, delay, period, false);
		this.tasks.add(scheduledTask);
		return scheduledTask;
	}

	/**
	 * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors.
	 *
	 * @param millis the length of time to sleep in milliseconds
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignore) { }
	}

}
