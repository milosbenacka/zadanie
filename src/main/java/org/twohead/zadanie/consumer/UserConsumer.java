package org.twohead.zadanie.consumer;


import java.util.concurrent.TimeUnit;

import org.twohead.zadanie.model.User;
import org.twohead.zadanie.queue.DBBlockingQueue;

public class UserConsumer extends AbstractConsumer<User> {

	public UserConsumer(DBBlockingQueue<User> sharedQueue) {
		super(sharedQueue);
	}

	public UserConsumer(DBBlockingQueue<User> sharedQueue, long timeout, TimeUnit unit) {
		super(sharedQueue, timeout, unit);
	}

	@Override
	protected void process(User e) {
		System.out.println(e);
	}
}
