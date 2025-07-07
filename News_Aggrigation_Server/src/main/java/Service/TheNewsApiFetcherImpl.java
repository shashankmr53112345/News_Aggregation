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

public class TheNewsApiFetcherImpl extends NewsApiFetcherImpl implements ApiNewsFetcher {
	private static final String THE_NEWS_API_URL = "https://api.thenewsapi.com/v1/news/top?locale=us&limit=3";
	private static final List<String> VALID_CATEGORIES = Arrays.asList("business", "entertainment", "sports", "tech",
			"health", "science", "politics", "food", "travel", "general");
	private final NewsArticleFetcherRepository newsArticleFetcherRepository;

	public TheNewsApiFetcherImpl() {
		this.newsArticleFetcherRepository = new NewsArticleFetcherRepository();
	}

	@Override
	public String getApiName() {
		return "The News API";
	}

	@Override
	public List<NewsArticles> fetchNewsArticles() throws Exception {
		String apiKey = newsArticleFetcherRepository.getApiKey(getApiName());
		if (apiKey == null) {
			throw new Exception("TheNewsAPI key not found");
		}

		List<NewsArticles> allArticles = new ArrayList<>();
		for (String category : VALID_CATEGORIES) {
			String currentUrl = THE_NEWS_API_URL + "&categories=" + category + "&api_token=" + apiKey;
			HttpURLConnection conn = null;
			int redirects = 0;
			final int MAX_REDIRECTS = 5;

			while (redirects < MAX_REDIRECTS) {
				URL url = new URL(currentUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setInstanceFollowRedirects(false);

				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					break; // Successful response
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
							"TheNewsAPI request failed with status " + responseCode + " for category: " + category);
					break;
				}
			}

			if (redirects >= MAX_REDIRECTS) {
				System.err.println("Too many redirects for TheNewsAPI request for category: " + category);
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
			if (!json.has("data")) {
				System.err.println("No 'data' array in TheNewsAPI response for category: " + category);
				continue;
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
				String articleCategory = determineCategoryFromResponse(article, title, description, category);

				NewsArticles newsArticle = new NewsArticles(id, title, description, source, urlStr, articleCategory,
						publishedAt, 0, 0, 0, null);
				allArticles.add(newsArticle);
				newsArticleFetcherRepository.insertArticle(newsArticle);
			}
		}
		newsArticleFetcherRepository.updateLastAccessed(getApiName());
		return allArticles;
	}

	private String determineCategoryFromResponse(JSONObject article, String title, String description,
			String requestedCategory) {
		String category = capitalize(requestedCategory);
		if (article.has("categories") && !article.isNull("categories")) {
			try {
				String categoriesStr = article.getString("categories");
				if (!categoriesStr.isEmpty()) {
					String[] categories = categoriesStr.split(",");
					for (String cat : categories) {
						String trimmedCat = cat.trim().toLowerCase();
						if (!trimmedCat.isEmpty() && VALID_CATEGORIES.contains(trimmedCat)) {
							category = capitalize(trimmedCat);
							break;
						}
					}
				}
			} catch (Exception e) {
				try {
					JSONArray categories = article.getJSONArray("categories");
					for (int i = 0; i < categories.length(); i++) {
						String trimmedCat = categories.getString(i).trim().toLowerCase();
						if (!trimmedCat.isEmpty() && VALID_CATEGORIES.contains(trimmedCat)) {
							category = capitalize(trimmedCat);
							break;
						}
					}
				} catch (Exception ignored) {
				}
			}
		}

		if (category.isEmpty() || category.equalsIgnoreCase("General")) {
			category = determineCategory(title + " " + description);
		}
		return category;
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

		String[] politicsKeywords = { "politics", "government", "election", "policy", "legislation", "senate",
				"congress", "president", "minister", "democracy", "parliament", "campaign", "vote", "law",
				"diplomacy" };
		for (String keyword : politicsKeywords) {
			keywordToCategory.put(keyword, "Politics");
		}

		String[] foodKeywords = { "food", "cooking", "recipe", "cuisine", "restaurant", "chef", "dining", "gourmet",
				"nutrition", "kitchen", "meal", "diet", "culinary", "baking", "taste" };
		for (String keyword : foodKeywords) {
			keywordToCategory.put(keyword, "Food");
		}

		String[] travelKeywords = { "travel", "tourism", "destination", "vacation", "trip", "journey", "adventure",
				"hotel", "flight", "tourist", "resort", "itinerary", "culture", "explore", "sightseeing" };
		for (String keyword : travelKeywords) {
			keywordToCategory.put(keyword, "Travel");
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