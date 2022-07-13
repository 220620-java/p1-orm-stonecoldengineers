package com.revature.models;

import java.util.Objects;

public class User {
	private int id;
	private String username;
	private String password;
	private List<Account> account;

	public User() {
		super();
		this.id = 0;
		this.username = "";
		this.password = "";
		this.account = new ArrayList<>();
	}

	public User(String username, String password) {
		super();
		this.id = 0;
		this.username = username;
		this.password = password;
		this.account = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Account> getAccounts() {
		return account;
	}

	public void setAccounts(List<Account> account) {
		this.account = account;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, password, username);
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
		return id == other.id && Objects.equals(password, other.password) && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + "]";
	}
}