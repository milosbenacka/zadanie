package org.twohead.zadanie.persistance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBUtils {

	private static DBUtils instance;

	private static final String protocol = "jdbc:derby:";
	private static final String defaultDbName = "derbyDB";
	private static String dbName = null;

	private Connection connection;

	public static synchronized void init(String dbName) throws SQLException {
		if (DBUtils.dbName != null) {
			return;
		}
		DBUtils.dbName = dbName;
		getInstance();
	}

	private DBUtils() throws SQLException {
		createConnection();
		createTables();
	}

	private void createConnection() {

		final var props = new Properties();
		props.put("user", "user1");
		props.put("password", "user1");

		try {
			connection = DriverManager.getConnection(protocol + dbName + ";create=true", props);
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createTables() throws SQLException {
		if (getConnection() != null) {
			Statement s = null;
			try {
				if (isTableExist("SUSER")) {
					return;
				}

				s = getConnection().createStatement();
				s.execute("create table suser(user_ID int, USER_GUID varchar(32), USER_NAME varchar(255))");
				getConnection().commit();
			} finally {
				close(s);
			}
		}
	}

	private void dropTables() throws SQLException {
		if (getConnection() != null) {
			Statement s = null;
			try {
				if (isTableExist("SUSER")) {
					s = getConnection().createStatement();
					s.execute("drop table suser");
					getConnection().commit();
					System.out.println("suser dropped");
				}
			} finally {
				close(s);
			}
		}
	}

	public static DBUtils getInstance() throws SQLException {
		if (instance == null) {
			synchronized (DBUtils.class) {
				if (instance == null) {
					if (dbName == null) {
						dbName = defaultDbName;
					}
					instance = new DBUtils();
				}
			}
		}
		return instance;
	}

	public synchronized void shutdown(boolean dropTables) {
		try {
			if (dropTables) {
				getInstance().dropTables();
			}
			connection = null;
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {
			if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {
				// we got the expected exception
				System.out.println("Derby shut down normally");
				// Note that for single database shutdown, the expected
				// SQL state is "08006", and the error code is 45000.
			} else {
				// if the error code or SQLState is different, we have
				// an unexpected exception (shutdown failed)
				System.err.println("Derby did not shut down normally");
				se.printStackTrace();
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void close(Statement s) throws SQLException {
		if (s != null) {
			s.close();
		}
	}

	public void close(ResultSet s) throws SQLException {
		if (s != null) {
			s.close();
		}
	}

	public boolean isTableExist(String tablename) throws SQLException {
		final var dbmd = getConnection().getMetaData();
		final var rs = dbmd.getTables(null, null, tablename.toUpperCase(), null);
		try {
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} finally {
			close(rs);
		}
	}
}
