package dev.sbs.api.scheduler;

import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentQueue;

public class QueuedThread {

	private final Object anchor = new Object();
	private final Thread thread;
	private final ConcurrentQueue<Runnable> queuedActions = Concurrent.newQueue();
	private boolean isRunning = true;

	public QueuedThread() {
		this.thread = new Thread(() -> {
			while (true) {
				Scheduler.sleep(1);

				if (this.isRunning()) {
					if (!this.queuedActions.isEmpty()) {
						try {
							Runnable next = this.queuedActions.poll();
							next.run();
						} catch (Exception ex) {
							synchronized (this.anchor) {
								// TODO: Log
								//SkyblockRecords.getLog().error("Error running queued action: {{0}}", ex.getMessage());
								ex.printStackTrace();
							}
						}
					}
				}
			}
		});
		this.thread.start();
	}

	public final boolean isRunning() {
		return this.isRunning;
	}

	public final void setRunning(boolean value) {
		this.isRunning = value;

		if (!this.isRunning())
			this.queuedActions.clear();
	}

	public final void queueAction(Runnable runnable) {
		if (this.isRunning())
			this.queuedActions.add(runnable);
	}

}
