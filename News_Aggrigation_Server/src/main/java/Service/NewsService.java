package Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Repository.NewsArticlesOperationsRespository;
import model.NewsArticles;
import util.DatabaseConnection;

public class NewsService {
	private static final String NEWS_API_BASE_URL = "https://newsapi.org/v2/top-headlines?country=us";
	private static final String THE_NEWS_API_URL = "https://api.thenewsapi.com/v1/news/top?locale=us&limit=10";
	private static final List<String> CATEGORIES = Arrays.asList("business", "entertainment", "sports", "technology",
			"general");
	private final NewsArticlesOperationsRespository NewsArticlesOperationsRespository;

	public NewsService() {
		this.NewsArticlesOperationsRespository = new NewsArticlesOperationsRespository();
	}

	public List<NewsArticles> fetchNewsFromNewsApi() throws Exception {
		String apiKey = getApiKey("News API");
		if (apiKey == null)
			throw new Exception("NewsAPI key not found");

		List<NewsArticles> allArticles = new ArrayList<>();
		for (String category : CATEGORIES) {
			URL url = new URL(NEWS_API_BASE_URL + "&category=" + category + "&apiKey=" + apiKey);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				conn.disconnect();
				continue;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			conn.disconnect();

			JSONObject json = new JSONObject(response.toString());
			if (!json.has("articles")) {
				continue;
			}
			JSONArray jsonArticles = json.getJSONArray("articles");

			for (int i = 0; i < jsonArticles.length(); i++) {
				JSONObject article = jsonArticles.getJSONObject(i);
				if (!article.has("url") || !article.has("title")) {
					continue;
				}
				String id = String.valueOf(article.getString("url").hashCode());
				String title = article.getString("title");
				String description = article.optString("description", "");
				String source = article.has("source") && article.getJSONObject("source").has("name")
						? article.getJSONObject("source").getString("name")
						: "Unknown";
				String urlStr = article.getString("url");
				String publishedAt = article.getString("publishedAt");
				String articleCategory = category.toLowerCase();
				if (articleCategory.isEmpty()) {
					articleCategory = determineCategory(title + " " + description);
				}

				allArticles.add(new NewsArticles(id, title, description, source, urlStr, articleCategory, publishedAt,
						0, 0, 0));
			}
		}
		updateLastAccessed("News API");
		return allArticles;
	}

	public List<NewsArticles> fetchNewsFromTheNewsApi() throws Exception {
		String apiKey = getApiKey("The News API");
		if (apiKey == null)
			throw new Exception("TheNewsAPI key not found");

		URL url = new URL(THE_NEWS_API_URL + "&api_token=" + apiKey);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		int responseCode = conn.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			conn.disconnect();
			throw new Exception("TheNewsAPI request failed with status " + responseCode);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();
		conn.disconnect();

		List<NewsArticles> articles = new ArrayList<>();
		JSONObject json = new JSONObject(response.toString());
		if (!json.has("data")) {
			throw new Exception("No 'data' array in TheNewsAPI response");
		}
		JSONArray jsonArticles = json.getJSONArray("data");

		for (int i = 0; i < jsonArticles.length(); i++) {
			JSONObject article = jsonArticles.getJSONObject(i);
			if (!article.has("uuid") || !article.has("title") || !article.has("url")) {
				continue;
			}
			String id = article.getString("uuid");
			String title = article.getString("title");
			String description = article.optString("description", "");
			String source = article.optString("source", "Unknown");
			String urlStr = article.getString("url");
			String publishedAt = article.getString("published_at");
			String category = article.optString("categories", "").split(",")[0].toLowerCase();
			if (category.isEmpty()) {
				category = determineCategory(title + " " + description);
			}

			articles.add(new NewsArticles(id, title, description, source, urlStr, category, publishedAt, 0, 0, 0));
		}
		updateLastAccessed("The News API");
		return articles;
	}

	private String determineCategory(String text) {
		text = text.toLowerCase();
		if (text.contains("business") || text.contains("market") || text.contains("finance")) {
			return "business";
		} else if (text.contains("sports") || text.contains("game") || text.contains("team")) {
			return "sports";
		} else if (text.contains("entertainment") || text.contains("movie") || text.contains("music")) {
			return "entertainment";
		} else if (text.contains("technology") || text.contains("tech") || text.contains("innovation")) {
			return "technology";
		}
		return "general";
	}

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

	private String getApiKey(String apiName) throws SQLException {
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

	private void updateLastAccessed(String apiName) throws SQLException {
		String query = "UPDATE external_apis SET last_accessed = ? WHERE name = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setString(2, apiName);
			stmt.executeUpdate();
		}
	}

	public List<NewsArticles> getHeadlines(String startDate, String endDate, String category) throws SQLException {
		return NewsArticlesOperationsRespository.getHeadlines(startDate, endDate, category);
	}

	public void saveUserArticle(String username, String articleId) throws SQLException {
		if (!NewsArticlesOperationsRespository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		NewsArticlesOperationsRespository.saveArticle(username, articleId);
	}

	public List<NewsArticles> getSavedArticles(String username, String category) throws SQLException {
		return NewsArticlesOperationsRespository.getSavedArticles(username, category);
	}

	public void deleteUserArticle(String username, String articleId) throws SQLException {
		NewsArticlesOperationsRespository.deleteArticle(username, articleId);
	}

	public void likeArticle(String username, String articleId) throws SQLException {
		if (!NewsArticlesOperationsRespository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		NewsArticlesOperationsRespository.likeArticle(username, articleId);
	}

	public void dislikeArticle(String username, String articleId) throws SQLException {
		if (!NewsArticlesOperationsRespository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		NewsArticlesOperationsRespository.dislikeArticle(username, articleId);
	}

	public void reportArticle(String username, String articleId, String reason) throws SQLException {
		if (!NewsArticlesOperationsRespository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		if (reason == null || reason.trim().isEmpty()) {
			throw new SQLException("Report reason is required");
		}
		NewsArticlesOperationsRespository.reportArticle(username, articleId, reason);
	}

	public void fetchAndSaveAllNews() {
		try {
			List<NewsArticles> newsApiArticles = fetchNewsFromNewsApi();
			saveArticlesToDatabase(newsApiArticles);
		} catch (Exception e) {
		}
		try {
			List<NewsArticles> theNewsApiArticles = fetchNewsFromTheNewsApi();
			saveArticlesToDatabase(theNewsApiArticles);
		} catch (Exception e) {
		}
	}
}