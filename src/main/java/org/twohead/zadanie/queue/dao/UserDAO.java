package org.twohead.zadanie.queue.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.twohead.zadanie.persistance.DBUtils;
import org.twohead.zadanie.queue.model.User;

public class UserDAO {
	public void insert(User user) throws SQLException {
		
		final var conn = DBUtils.getInstance().getConnection();

		PreparedStatement psInsert = null;

		try {
			final var sb = new StringBuilder();
			sb.append("insert into suser (user_ID,USER_GUID,USER_NAME) values (");
			sb.append(user.getUserId());
			sb.append(",'");
			sb.append(user.getUserGuid());
			sb.append("','");
			sb.append(user.getName());
			sb.append("')");

			psInsert = conn.prepareStatement(sb.toString());
			psInsert.executeUpdate();
			conn.commit();
		} finally {
			DBUtils.getInstance().close(psInsert);
		}
	}

	public User extract() throws SQLException {
		final var conn = DBUtils.getInstance().getConnection();
		PreparedStatement psQueryHead = null;
		ResultSet rs = null;
		User result = null;

		try {
			psQueryHead = conn.prepareStatement("select * from suser fetch next 1 rows only",
					ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = psQueryHead.executeQuery();
			if (rs != null && rs.next()) {
				result = new User(rs.getLong(1), rs.getString(2), rs.getString(3));
				rs.deleteRow();
			}
			conn.commit();
		} finally {
			DBUtils.getInstance().close(rs);
			DBUtils.getInstance().close(psQueryHead);
		}
		return result;
	}
	
	public User itemAt(int i) throws SQLException {
		final var conn = DBUtils.getInstance().getConnection();
		PreparedStatement psQueryHead = null;
		ResultSet rs = null;
		User result = null;

		try {
			final var sb = new StringBuilder();
			sb.append("select * from suser offset "); 
			sb.append(i);
			sb.append(" rows fetch next 1 rows only");
			psQueryHead = conn.prepareStatement(sb.toString(),
					ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = psQueryHead.executeQuery();
			if (rs != null && rs.next()) {
				result = new User(rs.getLong(1), rs.getString(2), rs.getString(3));
			}
			conn.commit();
		} finally {
			DBUtils.getInstance().close(rs);
			DBUtils.getInstance().close(psQueryHead);
		}
		return result;
	}
	
	public int count() throws SQLException {
		final var conn = DBUtils.getInstance().getConnection();
		PreparedStatement psQueryHead = null;
		ResultSet rs = null;
		int result = -1;

		try {
			psQueryHead = conn.prepareStatement("select count(*) from suser",
					ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = psQueryHead.executeQuery();
			if (rs != null && rs.next()) {
				result = rs.getInt(1);
			}
			conn.commit();
		} finally {
			DBUtils.getInstance().close(rs);
			DBUtils.getInstance().close(psQueryHead);
		}
		return result;
	}
	
	public void removeItemAt(int i) throws SQLException {
		final var conn = DBUtils.getInstance().getConnection();
		PreparedStatement psQueryHead = null;
		ResultSet rs = null;

		try {
			final var sb = new StringBuilder();
			sb.append("select * from suser offset "); 
			sb.append(i);
			sb.append(" rows fetch next 1 rows only");
			psQueryHead = conn.prepareStatement(sb.toString(),
					ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = psQueryHead.executeQuery();
			System.out.println(sb);
			if (rs != null && rs.next()) {
				rs.deleteRow();
			}
			conn.commit();
		} finally {
			DBUtils.getInstance().close(rs);
			DBUtils.getInstance().close(psQueryHead);
		}
	}
	
	public void clearTable() throws SQLException {
		final var conn = DBUtils.getInstance().getConnection();
		Statement stm = null;
		try {
			stm = conn.createStatement();
			stm.executeUpdate("delete from suser");
			conn.commit();
		} finally {
			DBUtils.getInstance().close(stm);
		}
	}
}
