package org.twohead.zadanie.queue;

import java.sql.SQLException;

import org.twohead.zadanie.dao.UserDAO;
import org.twohead.zadanie.model.User;
import org.twohead.zadanie.persistance.DBUtils;

public class UserQueue extends DBBlockingQueue<User> {
	
	private static UserQueue instance;
	
	public static UserQueue getInstance() {
		if (instance == null) {
			synchronized (DBUtils.class) {
				if (instance == null) {
					instance = new UserQueue();
				}
			}
		}
		return instance;
	}

	private UserQueue() {}

	@Override
	protected void insertItem(User user) throws SQLException {
		new UserDAO().insert(user);
	}

	@Override
	protected User extractItem() throws SQLException {
		return new UserDAO().extract();
	}

	@Override
	protected User itemAt(int i) throws SQLException {
		return new UserDAO().itemAt(i);
	}

	@Override
	protected int count() throws SQLException {
		return new UserDAO().count();
	}

	@Override
	protected void removeItemAt(int i) throws SQLException {
		new UserDAO().removeItemAt(i);
	}

	@Override
	protected void clearTable() throws SQLException {
		new UserDAO().clearTable();
	}
}
