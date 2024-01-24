package org.twohead.zadanie.model;

import java.util.Objects;

public class User {

	private long userId;
	private String userGuid;
	private String name;

	public User(long userId, String userGuid, String name) {
		this.userId = userId;
		this.userGuid = userGuid;
		this.name = name;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserGuid() {
		return userGuid;
	}

	public void setUserGuid(String userGuid) {
		this.userGuid = userGuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", userGuid=" + userGuid + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, userGuid, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(name, other.name) && Objects.equals(userGuid, other.userGuid) && userId == other.userId;
	}

}
