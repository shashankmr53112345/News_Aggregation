package service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import data.HttpRequestClient;

public class UserNewsOperationsService {
	private final HttpRequestClient httpRequestClient;

	public UserNewsOperationsService() {
		this.httpRequestClient = new HttpRequestClient("http://localhost:8080/News_Aggrigation_Server");
	}

	public JSONObject getArticles(String endpoint, String username, String startDate, String endDate, String category) {
		try {
			StringBuilder url = new StringBuilder("api/news/" + endpoint);
			boolean hasParams = false;
			if (startDate != null) {
				url.append("?startDate=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
				hasParams = true;
			}
			if (endDate != null) {
				url.append(hasParams ? "&" : "?").append("endDate=")
						.append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
				hasParams = true;
			}
			if (category != null) {
				url.append(hasParams ? "&" : "?").append("category=")
						.append(URLEncoder.encode(category, StandardCharsets.UTF_8));
				hasParams = true;
			}
			if (endpoint.equals("headlines") && category == null) {
				url.append(hasParams ? "&" : "?").append("all=true");
			}
			System.out.println("Debug: GET URL = " + url.toString());
			JSONObject response = httpRequestClient.get(url.toString());
			if (!response.getBoolean("success")) {
				return null;
			}
			return response;
		} catch (Exception e) {
			System.err.println("Error in getArticles: " + e.getMessage());
			return null;
		}
	}

	public JSONObject getSavedArticles(String username, String category) {
		try {
			StringBuilder url = new StringBuilder("api/news/saved?username=")
					.append(URLEncoder.encode(username, StandardCharsets.UTF_8));
			if (category != null) {
				url.append("&category=").append(URLEncoder.encode(category, StandardCharsets.UTF_8));
			}
			System.out.println("Debug: GET URL = " + url.toString());
			JSONObject response = httpRequestClient.get(url.toString());
			if (!response.getBoolean("success")) {
				return null;
			}
			return response;
		} catch (Exception e) {
			System.err.println("Error in getSavedArticles: " + e.getMessage());
			return null;
		}
	}

	public String saveArticle(String username, String articleId) {
		try {
			if (username == null || username.trim().isEmpty()) {
				return "Error: user ID is required";
			}
			if (articleId == null || articleId.trim().isEmpty()) {
				return "Error: article ID is required";
			}
			String url = "api/news/save?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&articleId="
					+ URLEncoder.encode(articleId, StandardCharsets.UTF_8);
			System.out.println("Debug: POST URL = " + url);
			JSONObject response = httpRequestClient.post(url, "");
			return response.getString("message");
		} catch (Exception e) {
			return "Error saving article: " + e.getMessage();
		}
	}

	public String deleteArticle(String username, String articleId) {
		try {
			String url = "api/news/saved?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
					+ "&articleId=" + URLEncoder.encode(articleId, StandardCharsets.UTF_8);
			System.out.println("Debug: DELETE URL = " + url);
			JSONObject response = httpRequestClient.delete(url);
			return response.getString("message");
		} catch (Exception e) {
			return "Error deleting article: " + e.getMessage();
		}
	}

	public String likeArticle(String username, String articleId) {
		try {
			if (username == null || username.trim().isEmpty()) {
				return "Error: user ID is required";
			}
			if (articleId == null || articleId.trim().isEmpty()) {
				return "Error: article ID is required";
			}
			String url = "api/news/like?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&articleId="
					+ URLEncoder.encode(articleId, StandardCharsets.UTF_8);
			System.out.println("Debug: POST URL = " + url);
			JSONObject response = httpRequestClient.post(url, "");
			return response.getString("message");
		} catch (Exception e) {
			return "Error liking article: " + e.getMessage();
		}
	}

	public String dislikeArticle(String username, String articleId) {
		try {
			if (username == null || username.trim().isEmpty()) {
				return "Error: user ID is required";
			}
			if (articleId == null || articleId.trim().isEmpty()) {
				return "Error: article ID is required";
			}
			String url = "api/news/dislike?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
					+ "&articleId=" + URLEncoder.encode(articleId, StandardCharsets.UTF_8);
			System.out.println("Debug: POST URL = " + url);
			JSONObject response = httpRequestClient.post(url, "");
			return response.getString("message");
		} catch (Exception e) {
			return "Error disliking article: " + e.getMessage();
		}
	}

	public String reportArticle(String username, String articleId, String reason) {
		try {
			if (username == null || username.trim().isEmpty()) {
				return "Error: user ID is required";
			}
			if (articleId == null || articleId.trim().isEmpty()) {
				return "Error: article ID is required";
			}
			if (reason == null || reason.trim().isEmpty()) {
				return "Error: report reason is required";
			}
			String url = "api/news/report?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
					+ "&articleId=" + URLEncoder.encode(articleId, StandardCharsets.UTF_8) + "&reason="
					+ URLEncoder.encode(reason, StandardCharsets.UTF_8);
			System.out.println("Debug: POST URL = " + url);
			JSONObject response = httpRequestClient.post(url, "");
			return response.getString("message");
		} catch (Exception e) {
			return "Error reporting article: " + e.getMessage();
		}
	}

	public JSONObject searchArticles(String username, String query, String startDate, String endDate, String sortBy) {
		try {
			StringBuilder url = new StringBuilder(
					"api/news/search?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&query="
							+ URLEncoder.encode(query, StandardCharsets.UTF_8));
			if (startDate != null) {
				url.append("&startDate=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
			}
			if (endDate != null) {
				url.append("&endDate=").append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
			}
			if (sortBy != null) {
				url.append("&sortBy=").append(URLEncoder.encode(sortBy, StandardCharsets.UTF_8));
			}
			System.out.println("Debug: GET URL = " + url.toString());
			JSONObject response = httpRequestClient.get(url.toString());
			if (!response.getBoolean("success")) {
				return null;
			}
			return response;
		} catch (Exception e) {
			System.err.println("Error in searchArticles: " + e.getMessage());
			return null;
		}
	}
}