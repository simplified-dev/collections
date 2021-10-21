package dev.sbs.api.scheduler;

import dev.sbs.api.SimplifiedApi;

public abstract class ScheduledRunnable implements Runnable {

	private ScheduledTask task;

	public void cancel() {
		SimplifiedApi.getScheduler().cancel(task);
	}

}