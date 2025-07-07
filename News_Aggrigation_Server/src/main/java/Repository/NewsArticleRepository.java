package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.ArticleReport;
import model.NewsArticles;
import util.DatabaseConnection;

public class NewsArticleRepository {
	private static final Logger logger = LoggerFactory.getLogger(NewsArticleRepository.class);

	public static class NotFoundException extends Exception {
		public NotFoundException(String message) {
			super(message);
		}
	}

	public List<NewsArticles> getHeadlines(String startDate, String endDate, String category) throws SQLException {
		StringBuilder query = new StringBuilder(
				"SELECT id, title, description, source, url, category, published_at, likes, dislikes, report_count, inserted_at "
						+ "FROM news_articles WHERE is_hidden = FALSE");
		List<String> params = new ArrayList<>();
		if (startDate != null && !startDate.isEmpty()) {
			if (endDate != null && !endDate.isEmpty()) {
				query.append(" AND published_at BETWEEN ? AND ?");
				params.add(startDate + "T00:00:00Z");
				params.add(endDate + "T23:59:59Z");
			} else {
				query.append(" AND published_at BETWEEN ? AND ?");
				params.add(startDate + "T00:00:00Z");
				params.add(startDate + "T23:59:59Z");
			}
		}
		if (category != null && !category.isEmpty()) {
			query.append(" AND LOWER(category) = ?");
			params.add(category.toLowerCase());
		}

		logger.debug("Executing getHeadlines query: {}", query);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query.toString())) {
			for (int i = 0; i < params.size(); i++) {
				stmt.setString(i + 1, params.get(i));
			}
			try (ResultSet rs = stmt.executeQuery()) {
				List<NewsArticles> articles = new ArrayList<>();
				while (rs.next()) {
					articles.add(createArticleFromResultSet(rs));
				}
				logger.info("Retrieved {} headlines", articles.size());
				return articles;
			}
		} catch (SQLException e) {
			logger.error("Error retrieving headlines: startDate={}, endDate={}, category={}", startDate, endDate,
					category, e);
			throw new SQLException("Database error retrieving headlines", e);
		}
	}

	public List<NewsArticles> getSavedArticles(String username, String category) throws SQLException {
		StringBuilder query = new StringBuilder(
				"SELECT na.id, na.title, na.description, na.source, na.url, na.category, na.published_at, na.likes, na.dislikes, na.report_count, na.inserted_at "
						+ "FROM news_articles na JOIN user_articles ua ON na.id = ua.article_id WHERE ua.username = ? AND na.is_hidden = FALSE");
		if (category != null && !category.isEmpty()) {
			query.append(" AND LOWER(na.category) = ?");
		}

		logger.debug("Executing getSavedArticles query for username: {}, category: {}", username, category);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query.toString())) {
			stmt.setString(1, username);
			if (category != null && !category.isEmpty()) {
				stmt.setString(2, category.toLowerCase());
			}
			try (ResultSet rs = stmt.executeQuery()) {
				List<NewsArticles> articles = new ArrayList<>();
				while (rs.next()) {
					articles.add(createArticleFromResultSet(rs));
				}
				logger.info("Retrieved {} saved articles for username: {}", articles.size(), username);
				return articles;
			}
		} catch (SQLException e) {
			logger.error("Error retrieving saved articles for username: {}, category: {}", username, category, e);
			throw new SQLException("Database error retrieving saved articles", e);
		}
	}

	public void saveArticle(String username, String articleId) throws SQLException {
		String query = "INSERT INTO user_articles (username, article_id) VALUES (?, ?)";
		logger.debug("Saving article: {} for username: {}", articleId, username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Article {} saved for username: {}", articleId, username);
			} else {
				logger.warn("Failed to save article: {} for username: {}", articleId, username);
				throw new SQLException("Failed to save article");
			}
		} catch (SQLException e) {
			logger.error("Error saving article: {} for username: {}", articleId, username, e);
			throw new SQLException("Database error saving article", e);
		}
	}

	public boolean articleExists(String articleId) throws SQLException {
		String query = "SELECT COUNT(*) FROM news_articles WHERE id = ?";
		logger.debug("Checking if article exists: {}", articleId);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					boolean exists = rs.getInt(1) > 0;
					logger.debug("Article {} exists: {}", articleId, exists);
					return exists;
				}
				return false;
			}
		} catch (SQLException e) {
			logger.error("Error checking article existence: {}", articleId, e);
			throw new SQLException("Database error checking article existence", e);
		}
	}

	public void deleteArticle(String username, String articleId) throws SQLException, NotFoundException {
		String query = "DELETE FROM user_articles WHERE username = ? AND article_id = ?";
		logger.debug("Deleting article: {} for username: {}", articleId, username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("Article {} not found in saved list for username: {}", articleId, username);
				throw new NotFoundException("Article not found in saved list");
			}
			logger.info("Article {} deleted from saved list for username: {}", articleId, username);
		} catch (SQLException e) {
			logger.error("Error deleting article: {} for username: {}", articleId, username, e);
			throw new SQLException("Database error deleting article", e);
		}
	}

	public void likeArticle(String username, String articleId) throws SQLException, NotFoundException {
		String query = "UPDATE news_articles SET likes = likes + 1 WHERE id = ? AND is_hidden = FALSE";
		logger.debug("Liking article: {} by username: {}", articleId, username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("Article {} not found or is hidden for like by username: {}", articleId, username);
				throw new NotFoundException("Article not found or is hidden");
			}
			logger.info("Article {} liked by username: {}", articleId, username);
		} catch (SQLException e) {
			logger.error("Error liking article: {} for username: {}", articleId, username, e);
			throw new SQLException("Database error liking article", e);
		}
	}

	public void dislikeArticle(String username, String articleId) throws SQLException, NotFoundException {
		String query = "UPDATE news_articles SET dislikes = dislikes + 1 WHERE id = ? AND is_hidden = FALSE";
		logger.debug("Disliking article: {} by username: {}", articleId, username);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("Article {} not found or is hidden for dislike by username: {}", articleId, username);
				throw new NotFoundException("Article not found or is hidden");
			}
			logger.info("Article {} disliked by username: {}", articleId, username);
		} catch (SQLException e) {
			logger.error("Error disliking article: {} for username: {}", articleId, username, e);
			throw new SQLException("Database error disliking article", e);
		}
	}

	public void reportArticle(String username, String articleId, String reason) throws SQLException, NotFoundException {
		String insertQuery = "INSERT INTO article_reports (username, article_id, reason) VALUES (?, ?, ?)";
		logger.debug("Reporting article: {} by username: {} with reason: {}", articleId, username, reason);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			stmt.setString(3, reason);
			stmt.executeUpdate();
			logger.debug("Report recorded for article: {} by username: {}", articleId, username);
		} catch (SQLException e) {
			logger.error("Error inserting report for article: {} by username: {}", articleId, username, e);
			throw new SQLException("Database error reporting article", e);
		}

		String updateQuery = "UPDATE news_articles SET report_count = report_count + 1, "
				+ "is_hidden = CASE WHEN report_count + 1 >= 5 THEN TRUE ELSE FALSE END WHERE id = ? AND is_hidden = FALSE";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				logger.warn("Article {} not found or is already hidden for report by username: {}", articleId,
						username);
				throw new NotFoundException("Article not found or is already hidden");
			}
			logger.info("Article {} reported by username: {}, report count updated", articleId, username);
		} catch (SQLException e) {
			logger.error("Error updating report count for article: {} by username: {}", articleId, username, e);
			throw new SQLException("Database error updating report count", e);
		}
	}

	public List<NewsArticles> searchArticles(String query, String startDate, String endDate, String sortBy)
			throws SQLException {
		StringBuilder sql = new StringBuilder(
				"SELECT id, title, description, source, url, category, published_at, likes, dislikes, report_count, inserted_at "
						+ "FROM news_articles WHERE is_hidden = FALSE AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)");
		List<String> params = new ArrayList<>();
		params.add("%" + query.toLowerCase() + "%");
		params.add("%" + query.toLowerCase() + "%");
		if (startDate != null && !startDate.isEmpty()) {
			if (endDate != null && !endDate.isEmpty()) {
				sql.append(" AND published_at BETWEEN ? AND ?");
				params.add(startDate + "T00:00:00Z");
				params.add(endDate + "T23:59:59Z");
			} else {
				sql.append(" AND published_at BETWEEN ? AND ?");
				params.add(startDate + "T00:00:00Z");
				params.add(startDate + "T23:59:59Z");
			}
		}
		if (sortBy != null && !sortBy.isEmpty()) {
			if (sortBy.equalsIgnoreCase("likes")) {
				sql.append(" ORDER BY likes DESC");
			} else if (sortBy.equalsIgnoreCase("dislikes")) {
				sql.append(" ORDER BY dislikes DESC");
			} else if (sortBy.equalsIgnoreCase("published_at")) {
				sql.append(" ORDER BY published_at DESC");
			}
		}

		logger.debug("Executing searchArticles query: query={}, startDate={}, endDate={}, sortBy={}", query, startDate,
				endDate, sortBy);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				stmt.setString(i + 1, params.get(i));
			}
			try (ResultSet rs = stmt.executeQuery()) {
				List<NewsArticles> articles = new ArrayList<>();
				while (rs.next()) {
					articles.add(createArticleFromResultSet(rs));
				}
				logger.info("Found {} articles for query: {}", articles.size(), query);
				return articles;
			}
		} catch (SQLException e) {
			logger.error("Error searching articles: query={}, startDate={}, endDate={}, sortBy={}", query, startDate,
					endDate, sortBy, e);
			throw new SQLException("Database error searching articles", e);
		}
	}

	public List<ArticleReport> getArticleReports() throws SQLException {
		String query = "SELECT username, article_id, reason FROM article_reports";
		logger.debug("Retrieving all article reports");
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			try (ResultSet rs = stmt.executeQuery()) {
				List<ArticleReport> reports = new ArrayList<>();
				while (rs.next()) {
					reports.add(new ArticleReport(rs.getString("username"), rs.getString("article_id"),
							rs.getString("reason")));
				}
				logger.info("Retrieved {} article reports", reports.size());
				return reports;
			}
		} catch (SQLException e) {
			logger.error("Error retrieving article reports", e);
			throw new SQLException("Database error retrieving article reports", e);
		}
	}

	public int hideArticleById(String articleId) throws SQLException {
		String query = "UPDATE news_articles SET is_hidden = TRUE WHERE id = ? AND is_hidden = FALSE";
		logger.debug("Hiding article by ID: {}", articleId);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			logger.debug("Rows affected hiding article {}: {}", articleId, rowsAffected);
			return rowsAffected;
		} catch (SQLException e) {
			logger.error("Error hiding article by ID: {}", articleId, e);
			throw new SQLException("Database error hiding article", e);
		}
	}

	public int hideArticlesByKeyword(String keyword) throws SQLException {
		String query = "UPDATE news_articles SET is_hidden = TRUE WHERE is_hidden = FALSE AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)";
		logger.debug("Hiding articles by keyword: {}", keyword);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, "%" + keyword.toLowerCase() + "%");
			stmt.setString(2, "%" + keyword.toLowerCase() + "%");
			int rowsAffected = stmt.executeUpdate();
			logger.debug("Rows affected hiding articles by keyword {}: {}", keyword, rowsAffected);
			return rowsAffected;
		} catch (SQLException e) {
			logger.error("Error hiding articles by keyword: {}", keyword, e);
			throw new SQLException("Database error hiding articles by keyword", e);
		}
	}

	public int hideArticlesByCategory(String category) throws SQLException {
		String query = "UPDATE news_articles SET is_hidden = TRUE WHERE is_hidden = FALSE AND LOWER(category) = ?";
		logger.debug("Hiding articles by category: {}", category);
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, category.toLowerCase());
			int rowsAffected = stmt.executeUpdate();
			logger.debug("Rows affected hiding articles by category {}: {}", category, rowsAffected);
			return rowsAffected;
		} catch (SQLException e) {
			logger.error("Error hiding articles by category: {}", category, e);
			throw new SQLException("Database error hiding articles by category", e);
		}
	}

	private NewsArticles createArticleFromResultSet(ResultSet rs) throws SQLException {
		return new NewsArticles(rs.getString("id"), rs.getString("title"), rs.getString("description"),
				rs.getString("category"), rs.getString("url"), rs.getString("source"), rs.getString("published_at"),
				rs.getInt("likes"), rs.getInt("dislikes"), rs.getInt("report_count"), rs.getTimestamp("inserted_at"));
	}
}