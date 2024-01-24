package org.twohead.zadanie.consumer;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.twohead.zadanie.queue.DBBlockingQueue;

public abstract class AbstractConsumer<E> implements Runnable {

	private static final long TIME_OUT_NOT_SET = -1;

	private final DBBlockingQueue<E> sharedQueue;
	private long timeout = -1;
	private TimeUnit unit;

	public AbstractConsumer(DBBlockingQueue<E> sharedQueue) {
		this.sharedQueue = sharedQueue;
	}

	public AbstractConsumer(DBBlockingQueue<E> sharedQueue, long timeout, TimeUnit unit) {
		this.sharedQueue = sharedQueue;
		this.timeout = timeout;
		this.unit = unit;
	}

	protected abstract void process(E e);
	
	protected DBBlockingQueue<E> getSharedQueue() {
		return sharedQueue;
	}

	@Override
	public void run() {

		E e = null;
		do {
			try {
				if (timeout == TIME_OUT_NOT_SET) {
					e = sharedQueue.take();
				} else {
					e = sharedQueue.take(timeout, unit);
				}
				if (e != null) {
					process(e);
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} catch (SQLException ex1) {
				ex1.printStackTrace();
			}
		} while (e != null);
	}
}
