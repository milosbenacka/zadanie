package org.twohead.zadanie.consumer;


import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.twohead.zadanie.model.User;
import org.twohead.zadanie.queue.DBBlockingQueue;

public class UserPrintConsumer extends UserConsumer {

	public UserPrintConsumer(DBBlockingQueue<User> sharedQueue) {
		super(sharedQueue);
	}

	public UserPrintConsumer(DBBlockingQueue<User> sharedQueue, long timeout, TimeUnit unit) {
		super(sharedQueue, timeout, unit);
	}

	@Override
	public void run() {
		try {
			getSharedQueue().printAll();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
