package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.NewsArticles;
import model.Notification;
import model.NotificationPreference;
import util.DatabaseConnection;

public class NotificationRepository {
	private static final Logger logger = LoggerFactory.getLogger(NotificationRepository.class);

	public NotificationPreference getNotificationPreference(String username) throws SQLException {
		logger.debug("Fetching notification preferences for username: {}", username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT categories, keywords FROM notification_preferences WHERE username = ?")) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String categories = rs.getString("categories");
					String keywords = rs.getString("keywords");
					List<String> categoryList = categories != null && !categories.trim().isEmpty()
							? Arrays.stream(categories.split(",")).map(String::trim).filter(cat -> !cat.isEmpty())
									.map(String::toLowerCase).collect(Collectors.toList())
							: new ArrayList<>();
					List<String> keywordList = keywords != null && !keywords.trim().isEmpty()
							? Arrays.stream(keywords.split(",")).map(String::trim).filter(kw -> !kw.isEmpty())
									.map(String::toLowerCase).collect(Collectors.toList())
							: new ArrayList<>();
					logger.info("Retrieved preferences for username: {}, categories: {}, keywords: {}", username,
							categoryList, keywordList);
					return new NotificationPreference(username, categoryList, keywordList);
				}
				logger.info("No preferences found for username: {}", username);
				return null;
			}
		} catch (SQLException e) {
			logger.error("Database error fetching preferences for username: {}", username, e);
			throw new SQLException("Error fetching notification preferences", e);
		}
	}

	public boolean saveNotificationPreference(String username, List<String> categories, List<String> keywords)
			throws SQLException {
		logger.debug("Saving notification preferences for username: {}, categories: {}, keywords: {}", username,
				categories, keywords);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"INSERT INTO notification_preferences (username, categories, keywords) VALUES (?, ?, ?) "
								+ "ON DUPLICATE KEY UPDATE categories = ?, keywords = ?")) {
			String categoryStr = categories != null ? String.join(",", categories) : "";
			String keywordStr = keywords != null ? String.join(",", keywords) : "";
			stmt.setString(1, username);
			stmt.setString(2, categoryStr);
			stmt.setString(3, keywordStr);
			stmt.setString(4, categoryStr);
			stmt.setString(5, keywordStr);
			int rowsAffected = stmt.executeUpdate();
			logger.info("Saved preferences for username: {}, rows affected: {}", username, rowsAffected);
			return rowsAffected > 0;
		} catch (SQLException e) {
			logger.error("Database error saving preferences for username: {}", username, e);
			throw new SQLException("Error saving notification preferences", e);
		}
	}

	public boolean saveNotification(String username, String articleId, String message) throws SQLException {
		logger.debug("Saving notification for username: {}, articleId: {}", username, articleId);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("INSERT INTO notifications (username, article_id, message, sent_at, status) "
								+ "VALUES (?, ?, ?, NOW(), ?) "
								+ "ON DUPLICATE KEY UPDATE message = ?, sent_at = NOW()")) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			stmt.setString(3, message);
			stmt.setString(4, "unread");
			stmt.setString(5, message);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Notification {} for username: {}, articleId: {}",
						rowsAffected == 1 ? "inserted" : "updated", username, articleId);
				return true;
			} else {
				logger.warn("No rows affected when saving notification for username: {}, articleId: {}", username,
						articleId);
				return false;
			}
		} catch (SQLException e) {
			logger.error("Database error saving notification for username: {}, articleId: {}", username, articleId, e);
			throw new SQLException("Error saving notification", e);
		}
	}

	public List<Notification> getNotifications(String username) throws SQLException {
		logger.debug("Fetching notifications for username: {}", username);
		List<Notification> notifications = new ArrayList<>();
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT notification_id,article_id, message, sent_at, status FROM notifications WHERE username = ?")) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					notifications.add(new Notification(username, rs.getString("article_id"), rs.getString("message"),
							rs.getString("notification_id"), rs.getTimestamp("sent_at"), rs.getString("status")));
				}
				logger.info("Retrieved {} notifications for username: {}", notifications.size(), username);
				return notifications;
			}
		} catch (SQLException e) {
			logger.error("Database error fetching notifications for username: {}", username, e);
			throw new SQLException("Error fetching notifications", e);
		}
	}

	public String getUserEmail(String username) throws SQLException {
		logger.debug("Fetching email for username: {}", username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT email FROM users WHERE username = ?")) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String email = rs.getString("email");
					logger.info("Retrieved email {} for username: {}", email, username);
					return email;
				}
				logger.info("No email found for username: {}", username);
				return null;
			}
		} catch (SQLException e) {
			logger.error("Database error fetching email for username: {}", username, e);
			throw new SQLException("Error fetching user email", e);
		}
	}

	public boolean updateNotificationStatus(String username, String articleId, String status) throws SQLException {
		logger.debug("Updating notification status for username: {}, articleId: {}, status: {}", username, articleId,
				status);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"UPDATE notifications SET status = ? WHERE article_id = ? AND username = ?")) {
			stmt.setString(1, status);
			stmt.setString(2, articleId);
			stmt.setString(3, username);
			int rowsAffected = stmt.executeUpdate();
			logger.info("Updated notification status for username: {}, articleId: {}, rows affected: {}", username,
					articleId, rowsAffected);
			return rowsAffected > 0;
		} catch (SQLException e) {
			logger.error("Database error updating notification status for username: {}, articleId: {}", username,
					articleId, e);
			throw new SQLException("Error updating notification status", e);
		}
	}

	public boolean deleteNotifications(String username) throws SQLException {
		logger.debug("Deleting notifications for username: {}", username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("DELETE FROM notifications WHERE username = ?")) {
			stmt.setString(1, username);
			int rowsAffected = stmt.executeUpdate();
			logger.info("Deleted {} notifications for username: {}", rowsAffected, username);
			return rowsAffected > 0;
		} catch (SQLException e) {
			logger.error("Database error deleting notifications for username: {}", username, e);
			throw new SQLException("Error deleting notifications", e);
		}
	}

	public List<NewsArticles> fetchArticlesByCategory(String[] categories, int minutes) throws SQLException {
		logger.debug("Fetching articles for categories: {}, minutes: {}", Arrays.toString(categories), minutes);
		List<NewsArticles> articles = new ArrayList<>();
		if (categories == null || categories.length == 0) {
			logger.info("No categories provided for article fetch");
			return articles;
		}
		List<String> validCategories = Arrays.stream(categories).filter(cat -> cat != null && !cat.trim().isEmpty())
				.map(String::trim).map(String::toLowerCase).distinct().collect(Collectors.toList());
		if (validCategories.isEmpty()) {
			logger.info("No valid categories after filtering");
			return articles;
		}
		try (Connection conn = DatabaseConnection.getConnection()) {
			String placeholders = String.join(",", Collections.nCopies(validCategories.size(), "?"));
			String sql = "SELECT id, title, description, category, source, url, published_at, likes, dislikes, report_count, inserted_at "
					+ "FROM news_articles WHERE inserted_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE) "
					+ "AND LOWER(category) IN (" + placeholders + ")";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, minutes);
				for (int i = 0; i < validCategories.size(); i++) {
					stmt.setString(i + 2, validCategories.get(i));
				}
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String articleCategory = rs.getString("category");
						if (articleCategory == null) {
							logger.debug("Skipping article ID: {} due to null category", rs.getString("id"));
							continue;
						}
						articleCategory = articleCategory.toLowerCase();
						if (!validCategories.contains(articleCategory)) {
							logger.debug("Skipping article ID: {}, category: {} not in preferences: {}",
									rs.getString("id"), articleCategory, validCategories);
							continue;
						}
						articles.add(new NewsArticles(rs.getString("id"), rs.getString("title"),
								rs.getString("description"), articleCategory, rs.getString("url"),
								rs.getString("source"), rs.getString("published_at"), rs.getInt("likes"),
								rs.getInt("dislikes"), rs.getInt("report_count"), rs.getTimestamp("inserted_at")));
					}
					logger.info("Fetched {} articles for categories: {}", articles.size(), validCategories);
					return articles;
				}
			}
		} catch (SQLException e) {
			logger.error("Database error fetching articles by categories: {}", Arrays.toString(categories), e);
			throw new SQLException("Error fetching articles by category", e);
		}
	}

	public List<NewsArticles> fetchArticlesByKeywords(String[] keywords, int minutes) throws SQLException {
		logger.debug("Fetching articles for keywords: {}, minutes: {}", Arrays.toString(keywords), minutes);
		List<NewsArticles> articles = new ArrayList<>();
		if (keywords == null || keywords.length == 0) {
			logger.info("No keywords provided for article fetch");
			return articles;
		}
		List<String> validKeywords = Arrays.stream(keywords).filter(kw -> kw != null && !kw.trim().isEmpty())
				.map(String::trim).map(String::toLowerCase).distinct().collect(Collectors.toList());
		if (validKeywords.isEmpty()) {
			logger.info("No valid keywords after filtering");
			return articles;
		}
		try (Connection conn = DatabaseConnection.getConnection()) {
			String placeholders = String.join(" OR ", Collections.nCopies(validKeywords.size(),
					"LOWER(CONCAT(COALESCE(title, ''), ' ', COALESCE(description, ''))) LIKE ?"));
			String sql = "SELECT id, title, description, category, source, url, published_at, likes, dislikes, report_count, inserted_at "
					+ "FROM news_articles WHERE inserted_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE) AND (" + placeholders
					+ ")";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, minutes);
				for (int i = 0; i < validKeywords.size(); i++) {
					stmt.setString(i + 2, "%" + validKeywords.get(i) + "%");
				}
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						articles.add(new NewsArticles(rs.getString("id"), rs.getString("title"),
								rs.getString("description"), rs.getString("category"), rs.getString("source"),
								rs.getString("url"), rs.getString("published_at"), rs.getInt("likes"),
								rs.getInt("dislikes"), rs.getInt("report_count"), rs.getTimestamp("inserted_at")));
					}
					logger.info("Fetched {} articles for keywords: {}", articles.size(), validKeywords);
					return articles;
				}
			}
		} catch (SQLException e) {
			logger.error("Database error fetching articles by keywords: {}", Arrays.toString(keywords), e);
			throw new SQLException("Error fetching articles by keywords", e);
		}
	}
}