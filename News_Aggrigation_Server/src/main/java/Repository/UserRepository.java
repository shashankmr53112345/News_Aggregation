package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.User;
import util.DatabaseConnection;

public class UserRepository {

	public static class DuplicateKeyException extends Exception {
		public DuplicateKeyException(String message) {
			super(message);
		}
	}

	public User findByUsername(String username) {
		String sql = "SELECT * FROM users WHERE username = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new User(rs.getString("username"), rs.getString("email"), rs.getString("password"),
						rs.getBoolean("is_admin"));
			}
			return null;
		} catch (SQLException e) {
			System.err.println("Error querying user by username: " + e.getMessage());
			throw new RuntimeException("Database error while querying user", e);
		}
	}

	public boolean save(User user) throws DuplicateKeyException {
		String sql = "INSERT INTO users (username, email, password, is_admin) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getEmail());
			stmt.setString(3, user.getPasswordHash());
			stmt.setBoolean(4, user.isAdmin());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			if (isDuplicateKeyError(e)) {
				String message = determineDuplicateField(e, user);
				throw new DuplicateKeyException(message);
			}
			System.err.println("Error saving user: " + e.getMessage());
			throw new RuntimeException("Database error while saving user", e);
		}
	}

	private boolean isDuplicateKeyError(SQLException e) {
		String sqlState = e.getSQLState();
		int errorCode = e.getErrorCode();
		return sqlState != null && sqlState.equals("23505") || errorCode == 1062;
	}

	private String determineDuplicateField(SQLException e, User user) {
		String message = e.getMessage().toLowerCase();
		if (message.contains("username")) {
			return "Username '" + user.getUsername() + "' already exists";
		} else if (message.contains("email")) {
			return "Email '" + user.getEmail() + "' already exists";
		}
		return "Duplicate entry detected";
	}
}