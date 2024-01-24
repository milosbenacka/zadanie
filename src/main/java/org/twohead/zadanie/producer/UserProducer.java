package org.twohead.zadanie.producer;

import java.util.Collection;

import org.twohead.zadanie.model.User;
import org.twohead.zadanie.queue.DBBlockingQueue;

public class UserProducer extends AbstractProducer<User> {

	public UserProducer(DBBlockingQueue<User> sharedQueue) {
		super(sharedQueue);
	}

	public UserProducer(DBBlockingQueue<User> sharedQueue, User e) {
		super(sharedQueue, e);
	}

	public UserProducer(DBBlockingQueue<User> sharedQueue, Collection<User> load) {
		super(sharedQueue, load);
	}

}
