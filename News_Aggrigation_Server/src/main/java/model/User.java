package model;

public class User {
	private final int id;
	private final String username;
	private final String email;
	private final String passwordHash;
	private final boolean isAdmin;

	public User(int id, String username, String email, String passwordHash, boolean isAdmin) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.isAdmin = isAdmin;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public boolean isAdmin() {
		return isAdmin;
	}
}
