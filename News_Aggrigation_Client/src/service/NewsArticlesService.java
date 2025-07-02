package service;

import org.json.JSONObject;

import data.HttpRequestClient;

public class NewsArticlesService {
	private final HttpRequestClient httpRequestClient;
	private final String username;

	public NewsArticlesService(HttpRequestClient httpRequestClient, String username) {
		this.httpRequestClient = httpRequestClient;
		this.username = username;
	}

	public JSONObject getHeadlines(String startDate, String endDate, String category) throws Exception {
		String endpoint = "/api/news/headlines?username=" + username;
		if (startDate != null)
			endpoint += "&startDate=" + startDate;
		if (endDate != null)
			endpoint += "&endDate=" + endDate;
		if (category != null)
			endpoint += "&category=" + category;
		return httpRequestClient.get(endpoint);
	}

	public JSONObject getSavedArticles(String category) throws Exception {
		String endpoint = "/api/news/saved?username=" + username;
		if (category != null)
			endpoint += "&category=" + category;
		return httpRequestClient.get(endpoint);
	}

	public JSONObject saveArticle(String articleId) throws Exception {
		JSONObject requestBody = new JSONObject();
		requestBody.put("username", username);
		requestBody.put("articleId", articleId);
		return httpRequestClient.post("/api/news/save", requestBody.toString());
	}

	// Placeholder for delete (to be implemented in NewsServlet)
	public JSONObject deleteArticle(String articleId) throws Exception {
		// Note: Requires DELETE endpoint in NewsServlet
		throw new UnsupportedOperationException("Delete functionality not implemented yet.");
	}
}