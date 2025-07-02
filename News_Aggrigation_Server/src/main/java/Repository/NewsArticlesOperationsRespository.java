package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.NewsArticles;
import util.DatabaseConnection;

public class NewsArticlesOperationsRespository {
	public List<NewsArticles> getHeadlines(String startDate, String endDate, String category) throws SQLException {
		StringBuilder query = new StringBuilder(
				"SELECT id, title, description, source, url, category, published_at, likes, dislikes, report_count FROM news_articles WHERE ishide = FALSE");
		List<String> params = new ArrayList<>();
		if (startDate != null && !startDate.isEmpty()) {
			if (endDate != null && !endDate.isEmpty()) {
				query.append(" AND published_at BETWEEN ? AND ?");
				params.add(startDate + " 00:00:00");
				params.add(endDate + " 23:59:59");
			} else {
				query.append(" AND published_at BETWEEN ? AND ?");
				params.add(startDate + " 00:00:00");
				params.add(startDate + " 23:59:59");
			}
		}
		if (category != null && !category.isEmpty()) {
			query.append(" AND LOWER(category) = ?");
			params.add(category.toLowerCase());
		}
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query.toString())) {
			for (int i = 0; i < params.size(); i++) {
				stmt.setString(i + 1, params.get(i));
			}
			ResultSet rs = stmt.executeQuery();
			List<NewsArticles> articles = new ArrayList<>();
			while (rs.next()) {
				articles.add(new NewsArticles(rs.getString("id"), rs.getString("title"), rs.getString("description"),
						rs.getString("source"), rs.getString("url"), rs.getString("category"),
						rs.getString("published_at"), rs.getInt("likes"), rs.getInt("dislikes"),
						rs.getInt("report_count")));
			}
			return articles;
		}
	}

	public List<NewsArticles> getSavedArticles(String username, String category) throws SQLException {
		StringBuilder query = new StringBuilder(
				"SELECT na.id, na.title, na.description, na.source, na.url, na.category, na.published_at, na.likes, na.dislikes, na.report_count FROM news_articles na JOIN user_articles ua ON na.id = ua.article_id WHERE ua.username = ? AND na.ishide = FALSE");
		if (category != null && !category.isEmpty()) {
			query.append(" AND LOWER(na.category) = ?");
		}
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query.toString())) {
			stmt.setString(1, username);
			if (category != null && !category.isEmpty()) {
				stmt.setString(2, category.toLowerCase());
			}
			ResultSet rs = stmt.executeQuery();
			List<NewsArticles> articles = new ArrayList<>();
			while (rs.next()) {
				articles.add(new NewsArticles(rs.getString("id"), rs.getString("title"), rs.getString("description"),
						rs.getString("source"), rs.getString("url"), rs.getString("category"),
						rs.getString("published_at"), rs.getInt("likes"), rs.getInt("dislikes"),
						rs.getInt("report_count")));
			}
			return articles;
		}
	}

	public void saveArticle(String username, String articleId) throws SQLException {
		String query = "INSERT INTO saved_news_articles (username, article_id) VALUES (?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			stmt.executeUpdate();
		}
	}

	public boolean articleExists(String articleId) throws SQLException {
		String query = "SELECT COUNT(*) FROM news_articles WHERE id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
			return false;
		}
	}

	public void deleteArticle(String username, String articleId) throws SQLException {
		String query = "DELETE FROM saved_news_articles WHERE username = ? AND article_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				throw new SQLException("Article not found in saved list");
			}
		}
	}

	public void likeArticle(String username, String articleId) throws SQLException {
		String query = "UPDATE news_articles SET likes = likes + 1 WHERE id = ? AND ishide = FALSE";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				throw new SQLException("Article not found or is hidden");
			}
		}
	}

	public void dislikeArticle(String username, String articleId) throws SQLException {
		String query = "UPDATE news_articles SET dislikes = dislikes + 1 WHERE id = ? AND ishide = FALSE";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				throw new SQLException("Article not found or is hidden");
			}
		}
	}

	public void reportArticle(String username, String articleId, String reason) throws SQLException {
		String insertQuery = "INSERT INTO article_reports (userid, article_id, reason) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
			stmt.setString(1, username);
			stmt.setString(2, articleId);
			stmt.setString(3, reason);
			stmt.executeUpdate();
		}

		String updateQuery = "UPDATE news_articles SET report_count = report_count + 1, ishide = CASE WHEN report_count + 1 >= 5 THEN TRUE ELSE FALSE END WHERE id = ? AND ishide = FALSE";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
			stmt.setString(1, articleId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				throw new SQLException("Article not found or is already hidden");
			}
		}
	}
}