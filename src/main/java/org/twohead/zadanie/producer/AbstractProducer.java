package org.twohead.zadanie.producer;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import org.twohead.zadanie.queue.DBBlockingQueue;

public class AbstractProducer<E> implements Runnable {

	private final DBBlockingQueue<E> sharedQueue;
	
	private Collection<E> load;
	
	public AbstractProducer(DBBlockingQueue<E> sharedQueue) {
		super();
		this.sharedQueue = sharedQueue;
	}
	
	public AbstractProducer(DBBlockingQueue<E> sharedQueue, E e) {
		super();
		this.sharedQueue = sharedQueue;
		this.load = Collections.singleton(e);
	}
	
	public AbstractProducer(DBBlockingQueue<E> sharedQueue, Collection<E> load) {
		super();
		this.sharedQueue = sharedQueue;
		this.load = load;
	}

	@Override
	public void run() {
		if (load != null) {
			for (E e : load) {
				try {
					sharedQueue.put(e);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (SQLException ex1) {
					ex1.printStackTrace();
				}
			}
		}
	}
	
	public void add(E e) throws SQLException {
		try {
			sharedQueue.put(e);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
