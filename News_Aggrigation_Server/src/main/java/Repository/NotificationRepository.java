package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import model.Notification;
import model.NotificationPreference;
import util.DatabaseConnection;

public class NotificationRepository {
	public NotificationPreference getNotificationPreference(String userName) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT categories, keywords FROM notification_preferences WHERE username = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, userName);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					String categories = rs.getString("categories");
					String keywords = rs.getString("keywords");
					List<String> categoryList = categories != null ? Arrays.asList(categories.split(","))
							: new ArrayList<>();
					List<String> keywordList = keywords != null ? Arrays.asList(keywords.split(","))
							: new ArrayList<>();
					return new NotificationPreference(userName, categoryList, keywordList);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching preference for user " + userName + ": " + e.getMessage());
		}
		return null;
	}

	public boolean saveNotificationPreference(String userName, List<String> categories, List<String> keywords) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "INSERT INTO notification_preferences (username, categories, keywords) VALUES (?, ?, ?) "
					+ "ON DUPLICATE KEY UPDATE categories = ?, keywords = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				String categoryStr = categories != null ? String.join(",", categories) : "";
				String keywordStr = keywords != null ? String.join(",", keywords) : "";
				stmt.setString(1, userName);
				stmt.setString(2, categoryStr);
				stmt.setString(3, keywordStr);
				stmt.setString(4, categoryStr);
				stmt.setString(5, keywordStr);
				stmt.executeUpdate();
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error saving preference for user " + userName + ": " + e.getMessage());
			return false;
		}
	}

	public boolean saveNotification(String userName, String articleId, String message) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "INSERT INTO notifications (id, username, article_id, message, created_at, status) VALUES (?, ?, ?, ?, NOW(), ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, UUID.randomUUID().toString());
				stmt.setString(2, userName);
				stmt.setString(3, articleId);
				stmt.setString(4, message);
				stmt.setString(5, "sent");
				stmt.executeUpdate();
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error saving notification for user " + userName + ", article " + articleId + ": "
					+ e.getMessage());
			return false;
		}
	}

	public List<Notification> getNotifications(String userName) {
		List<Notification> notifications = new ArrayList<>();
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT id, article_id, message, created_at, status FROM notifications WHERE username = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, userName);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Notification notification = new Notification();
					notification.setId(rs.getString("id"));
					notification.setUsername(userName);
					notification.setArticleId(rs.getString("article_id"));
					notification.setMessage(rs.getString("message"));
					notification.setCreatedAt(rs.getTimestamp("created_at"));
					notification.setStatus(rs.getString("status"));
					notifications.add(notification);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching notifications for user " + userName + ": " + e.getMessage());
		}
		return notifications;
	}

	public String getUserEmail(String userName) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT email FROM users WHERE username = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, userName);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					return rs.getString("email");
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching email for user " + userName + ": " + e.getMessage());
		}
		return null;
	}

	public boolean updateNotificationStatus(String userName, String notificationId, String status) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "UPDATE notifications SET status = ? WHERE id = ? AND username = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, status);
				stmt.setString(2, notificationId);
				stmt.setString(3, userName);
				int rows = stmt.executeUpdate();
				return rows > 0;
			}
		} catch (SQLException e) {
			System.err.println("Error updating notification status for user " + userName + ", id " + notificationId
					+ ": " + e.getMessage());
			return false;
		}
	}

	public boolean deleteNotifications(String userName) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "DELETE FROM notifications WHERE username = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setString(1, userName);
				stmt.executeUpdate();
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error deleting notifications for user " + userName + ": " + e.getMessage());
			return false;
		}
	}
}