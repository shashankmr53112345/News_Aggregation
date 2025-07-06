package model;

public class User {
	private final String username;
	private final String email;
	private final String passwordHash;
	private final boolean isAdmin;

	public User(String username, String email, String passwordHash, boolean isAdmin) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.isAdmin = isAdmin;
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
