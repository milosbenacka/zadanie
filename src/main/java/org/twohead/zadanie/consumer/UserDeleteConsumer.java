package org.twohead.zadanie.consumer;


import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.twohead.zadanie.queue.DBBlockingQueue;
import org.twohead.zadanie.queue.model.User;

public class UserDeleteConsumer extends UserConsumer {

	public UserDeleteConsumer(DBBlockingQueue<User> sharedQueue) {
		super(sharedQueue);
	}

	public UserDeleteConsumer(DBBlockingQueue<User> sharedQueue, long timeout, TimeUnit unit) {
		super(sharedQueue, timeout, unit);
	}

	@Override
	public void run() {
		try {
			getSharedQueue().clear();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
