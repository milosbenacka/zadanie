package org.twohead.zadanie;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.twohead.zadanie.consumer.UserDeleteConsumer;
import org.twohead.zadanie.consumer.UserPrintConsumer;
import org.twohead.zadanie.model.User;
import org.twohead.zadanie.persistance.DBUtils;
import org.twohead.zadanie.producer.UserProducer;
import org.twohead.zadanie.queue.UserQueue;

public class App extends UserProducer {
	
	private ExecutorService exe = Executors.newCachedThreadPool();

	public App() {
		super(UserQueue.getInstance());
	}

	public void add(int userId, String userGuid, String userName) throws InterruptedException, SQLException {
		add(new User(userId, userGuid, userName));
	}

	public void printAll() {
		exe.execute(new UserPrintConsumer(UserQueue.getInstance()));
	}

	public void deleteAll() {
		exe.execute(new UserDeleteConsumer(UserQueue.getInstance()));
	}
	
	public void shutdown() throws InterruptedException {
		exe.shutdown();
		exe.awaitTermination(1, TimeUnit.MINUTES);
	}

	public static void main(String[] args) throws InterruptedException, SQLException {
		try {
			final var app = new App();
			app.add(1, "a1", "Robert");
			app.add(2, "a2", "Martin");
			app.printAll();
			app.deleteAll();
			app.printAll();
			app.shutdown();
		}
		finally {
			DBUtils.getInstance().shutdown(false);
		}
	}

}
