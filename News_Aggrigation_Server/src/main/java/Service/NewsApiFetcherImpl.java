package Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import Repository.NewsArticleFetcherRepository;
import model.NewsArticles;

public class NewsApiFetcherImpl implements ApiNewsFetcher {
	private static final String NEWS_API_BASE_URL = "https://newsapi.org/v2/top-headlines?country=us";
	private static final List<String> CATEGORIES = Arrays.asList("business", "entertainment", "sports", "technology",
			"health", "science", "general");
	private final NewsArticleFetcherRepository newsArticleFetcherRepository;

	public NewsApiFetcherImpl() {
		this.newsArticleFetcherRepository = new NewsArticleFetcherRepository();
	}

	@Override
	public String getApiName() {
		return "News API";
	}

	@Override
	public List<NewsArticles> fetchNewsArticles() throws Exception {
		String apiKey = newsArticleFetcherRepository.getApiKey(getApiName());
		if (apiKey == null) {
			throw new Exception("NewsAPI key not found");
		}

		List<NewsArticles> allArticles = new ArrayList<>();
		for (String category : CATEGORIES) {
			String currentUrl = NEWS_API_BASE_URL + "&category=" + category + "&apiKey=" + apiKey;
			HttpURLConnection conn = null;
			int redirects = 0;
			final int MAX_REDIRECTS = 5;

			while (redirects < MAX_REDIRECTS) {
				URL url = new URL(currentUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setInstanceFollowRedirects(false); // Handle redirects manually

				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					break;
				} else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
						|| responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
					String newUrl = conn.getHeaderField("Location");
					conn.disconnect();
					if (newUrl == null) {
						throw new Exception("Redirect response with no Location header for category: " + category);
					}
					currentUrl = newUrl;
					redirects++;
				} else {
					conn.disconnect();
					System.err.println(
							"NewsAPI request failed with status " + responseCode + " for category: " + category);
					break;
				}
			}

			if (redirects >= MAX_REDIRECTS) {
				System.err.println("Too many redirects for NewsAPI request for category: " + category);
				continue;
			}

			if (conn == null || conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
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
				System.err.println("No 'articles' array in NewsAPI response for category: " + category);
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
				String articleCategory = capitalize(category);
				if (articleCategory.equals("General")) {
					articleCategory = determineCategory(title + " " + description);
				}

				NewsArticles newsArticle = new NewsArticles(id, title, description, source, urlStr, articleCategory,
						publishedAt, 0, 0, 0, null);
				allArticles.add(newsArticle);
				newsArticleFetcherRepository.insertArticle(newsArticle);
			}
		}
		newsArticleFetcherRepository.updateLastAccessed(getApiName());
		return allArticles;
	}

	protected String determineCategory(String text) {
		Map<String, String> keywordToCategory = new HashMap<>();

		String[] businessKeywords = { "business", "market", "finance", "economy", "stock", "investment", "company",
				"corporate", "banking", "trade", "commerce", "industry", "entrepreneur", "startup", "revenue" };
		for (String keyword : businessKeywords) {
			keywordToCategory.put(keyword, "Business");
		}

		String[] entertainmentKeywords = { "entertainment", "movie", "music", "celebrity", "film", "television",
				"actor", "actress", "concert", "festival", "award", "premiere", "hollywood", "streaming", "showbiz" };
		for (String keyword : entertainmentKeywords) {
			keywordToCategory.put(keyword, "Entertainment");
		}

		String[] sportsKeywords = { "sports", "game", "team", "athlete", "match", "tournament", "championship",
				"player", "coach", "league", "score", "stadium", "olympics", "fitness", "competition" };
		for (String keyword : sportsKeywords) {
			keywordToCategory.put(keyword, "Sports");
		}

		String[] technologyKeywords = { "technology", "tech", "innovation", "software", "hardware", "gadget",
				"smartphone", "computer", "internet", "artificial intelligence", "blockchain", "cybersecurity", "data",
				"cloud", "robotics" };
		for (String keyword : technologyKeywords) {
			keywordToCategory.put(keyword, "Technology");
		}

		String[] healthKeywords = { "health", "medical", "wellness", "hospital", "doctor", "medicine", "disease",
				"vaccine", "nutrition", "fitness", "mental health", "therapy", "pharmaceutical", "pandemic",
				"healthcare" };
		for (String keyword : healthKeywords) {
			keywordToCategory.put(keyword, "Health");
		}

		String[] scienceKeywords = { "science", "research", "discovery", "scientist", "experiment", "laboratory",
				"physics", "biology", "astronomy", "chemistry", "space", "environment", "climate", "geology",
				"genetics" };
		for (String keyword : scienceKeywords) {
			keywordToCategory.put(keyword, "Science");
		}

		text = text.toLowerCase();
		for (Map.Entry<String, String> entry : keywordToCategory.entrySet()) {
			if (text.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return "General";
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}