package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import model.NewsArticles;
import util.DatabaseConnection;

public class NewsArticleFetcherRepository {

	public void saveArticlesToDatabase(List<NewsArticles> articles) throws SQLException {
		for (NewsArticles article : articles) {
			String query = "SELECT COUNT(*) FROM news_articles WHERE id = ?";
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setString(1, article.getId());
				ResultSet rs = stmt.executeQuery();
				if (rs.next() && rs.getInt(1) == 0) {
					saveArticle(article);
				}
			}
		}
	}

	private void saveArticle(NewsArticles article) throws SQLException {
		String query = "INSERT INTO news_articles (id, title, description, source, url, category, published_at, likes, dislikes, report_count, ishide) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, article.getId());
			stmt.setString(2, article.getTitle());
			stmt.setString(3, article.getDescription());
			stmt.setString(4, article.getSource());
			stmt.setString(5, article.getUrl());
			stmt.setString(6, article.getCategory());
			stmt.setString(7, article.getPublishedAt());
			stmt.setInt(8, article.getLikes());
			stmt.setInt(9, article.getDislikes());
			stmt.setInt(10, article.getReportCount());
			stmt.setBoolean(11, false);
			stmt.executeUpdate();
		}
	}

	public String getApiKey(String apiName) throws SQLException {
		String query = "SELECT api_key FROM external_apis WHERE name = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, apiName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("api_key");
			}
		}
		return null;
	}

	public void updateLastAccessed(String apiName) throws SQLException {
		String query = "UPDATE external_apis SET last_accessed = ? WHERE name = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setString(2, apiName);
			stmt.executeUpdate();
		}
	}

	public void insertArticle(NewsArticles article) throws SQLException {
		String sql = "INSERT INTO news_articles (id, title, description, source, url, category, published_at, likes, dislikes, report_count) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
				+ "title = VALUES(title), description = VALUES(description), source = VALUES(source), "
				+ "url = VALUES(url), category = VALUES(category), published_at = VALUES(published_at), "
				+ "likes = VALUES(likes), dislikes = VALUES(dislikes), report_count = VALUES(report_count)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, article.getId());
			stmt.setString(2, article.getTitle());
			stmt.setString(3, article.getDescription());
			stmt.setString(4, article.getSource());
			stmt.setString(5, article.getUrl());
			stmt.setString(6, article.getCategory());
			stmt.setString(7, article.getPublishedAt());
			stmt.setInt(8, article.getLikes());
			stmt.setInt(9, article.getDislikes());
			stmt.setInt(10, article.getReportCount());
			stmt.executeUpdate();
		}
	}
}