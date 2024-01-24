package org.twohead.zadanie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.twohead.zadanie.model.User;
import org.twohead.zadanie.persistance.DBUtils;
import org.twohead.zadanie.queue.UserQueue;

public class UserQueueTest {

	private int userId = 0;

	private static final String DB_TEST = "derbyDBTest";

	@BeforeClass
	public static void init() throws SQLException {
		DBUtils.init(DB_TEST);
	}

	@AfterClass
	public static void shutDown() throws SQLException {
		DBUtils.getInstance().shutdown(true);
	}

	@Before
	public void clean() throws SQLException {
		final var conn = DBUtils.getInstance().getConnection();
		final var s = conn.createStatement();
		s.execute("delete from suser");
		conn.commit();
		DBUtils.getInstance().close(s);
	}

	@Test
	public void isEmptyTest() throws SQLException {
		final var cat = UserQueue.getInstance();
		final var res = cat.isEmpty();
		assertTrue("Queue should be empty", res);
	}

	@Test
	public void takeTest() throws InterruptedException, SQLException {
		final var cat = UserQueue.getInstance();
		final var user = createUser();
		cat.put(user);
		final var userTake = cat.take();
		assertEquals("Added user should be equlas to take user", user, userTake);
	}

	@Test
	public void takeManyTest() throws InterruptedException, SQLException {
		final var count = 1000;
		final var cat = UserQueue.getInstance();
		final var userList = createUserList(count);
		
		for (User user : userList) {
			cat.put(user);
		}
		User takenUser = null;
		int i = 0;
		while (i<count) {
			takenUser = cat.take();
			assertEquals("Taken user should be equla to added user", userList.get(i), takenUser);
			i++;
		} 
		
		assertEquals("Number of taken users should match the number of added users", count, i);
	}

	@Test
	public void takeTimeoutTest() throws InterruptedException, SQLException {
		final var cat = UserQueue.getInstance();
		final long timeOutInMs = 2000L;
		long start = System.nanoTime();

		final var userTake = cat.take(timeOutInMs, TimeUnit.MILLISECONDS);
		assertNull("Taken user should be null", userTake);

		long finish = System.nanoTime();
		long timeElapsedInMs = (finish - start) / 1_000_000L;
		assertTrue("Time elapsed should be greater or equal than time out", timeElapsedInMs > timeOutInMs);
	}

	@Test
	public void takeEmptyTest() throws InterruptedException {
		final var cat = UserQueue.getInstance();
		final var user = createUser();
		final var executor = Executors.newFixedThreadPool(2);
		final long sleepTimeInMs = 2000L;

		executor.submit(new Runnable() {
			long start = System.nanoTime();

			public void run() {
				User userTake = null;
				try {
					userTake = cat.take();
				} catch (InterruptedException | SQLException e) {
					e.printStackTrace();
					assertTrue("The exception should not happen", false);
				}
				assertEquals("Added user should be equlas to take user", user, userTake);
				long finish = System.nanoTime();
				long timeElapsedInMs = (finish - start) / 1_000_000L;
				assertTrue("Time elapsed should be greater or equal than sleep time", timeElapsedInMs > sleepTimeInMs);

			}
		});
		executor.submit(new Runnable() {
			public void run() {
				try {
					// let's wait the other thread for the first item in the empty queue
					Thread.sleep(sleepTimeInMs);
				} catch (InterruptedException e) {
					e.printStackTrace();
					assertTrue("The exception should not happen", false);
				}
				try {
					cat.put(user);
				} catch (InterruptedException | SQLException e) {
					e.printStackTrace();
					assertTrue("The exception should not happen", false);
				}
			}
		});

		executor.shutdown();
		try {
			executor.awaitTermination(30L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void sizeTest() throws InterruptedException, SQLException {
		final int count = 100;
		final var cat = UserQueue.getInstance();

		for (int i = 0; i < count; i++) {
			final var user = createUser();
			cat.put(user);
		}

		final var resSize = cat.size();
		assertEquals("Result size should match number of added users", count, resSize);
	}

	@Test
	public void peekTest() throws InterruptedException, SQLException {
		final var count = 100;
		final var cat = UserQueue.getInstance();
		User user0 = null;

		for (int i = 0; i < count; i++) {
			final var user = createUser();
			if (user0 == null) {
				user0 = user;
			}
			cat.put(user);
			final var peekedUser = cat.peek();
			assertEquals("Peek user should be equals to the firstly added user", user0, peekedUser);
		}
	}

	@Test
	public void clearTest() throws InterruptedException, SQLException {
		final var count = 10;
		final var cat = UserQueue.getInstance();

		for (int i = 0; i < count; i++) {
			final var user = createUser();
			cat.put(user);
		}

		final var resSize = cat.size();
		assertEquals("Result size should match number of offered users", count, resSize);

		cat.clear();

		final var resClearSize = cat.size();
		assertEquals("Result size should be zero", 0, resClearSize);

	}

	@Test
	public void drainToTest() throws InterruptedException, SQLException {
		final var count = 10;
		final var cat = UserQueue.getInstance();
		final var userList = createUserList(count);

		for (final var user : userList) {
			cat.put(user);
		}

		final var resSize = cat.size();
		assertEquals("Result size should match the number of offered users", count, resSize);

		final var drainList = new ArrayList<User>();
		final var drainResult = cat.drainTo(drainList);

		assertEquals("Drain result should match the number of added users", count, drainResult);
		assertEquals("Drain list size should match the number od added users", count, drainList.size());

		var i = 0;
		for (User user : userList) {
			final var drainedUser = drainList.get(i);
			assertEquals("User should be equal to drained user", user, drainedUser);
			i++;
		}
	}

	@Test
	public void drainToMaxTest() throws InterruptedException, SQLException {
		final int count = 10;
		final var cat = UserQueue.getInstance();
		final var userList = createUserList(count);
		final var max = 5;

		for (User user : userList) {
			cat.put(user);
		}

		final var resSize = cat.size();
		assertEquals("Result size should match number of offered users", count, resSize);

		final var drainList = new ArrayList<User>();
		final var drainResult = cat.drainTo(drainList, max);

		assertEquals("Drain result should match number of added users", max, drainResult);
		assertEquals("Drain list size should match the number od added users", max, drainList.size());
		var i = 0;
		for (User user : userList) {
			final var drainedUser = drainList.get(i);
			assertEquals("User should be equal to drained user", user, drainedUser);
			i++;
			if (i == max) {
				break;
			}
		}
	}

	private List<User> createUserList(int size) {
		final var res = new ArrayList<User>();
		for (int i = 0; i < size; i++) {
			res.add(createUser());
		}
		return res;
	}

	private User createUser() {
		User user = new User(userId, UUID.randomUUID().toString().substring(0, 32), "Name" + userId);
		userId++;
		return user;
	}

}
