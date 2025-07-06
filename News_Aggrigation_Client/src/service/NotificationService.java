package service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import UI.InputValidator;
import data.HttpRequestClient;

public class NotificationService {
	private final HttpRequestClient httpRequestClient;
	private final InputValidator inputValidator;
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");

	public NotificationService() {
		this(new HttpRequestClient("http://localhost:8080/News_Aggrigation_Server"), new InputValidator());
	}

	public NotificationService(HttpRequestClient httpRequestClient, InputValidator inputValidator) {
		this.httpRequestClient = httpRequestClient;
		this.inputValidator = inputValidator;
	}

	public JSONObject getNotificationConfigurations(String username) {
		try {
			if (username == null || username.trim().isEmpty()) {
				System.err.println("Error: Invalid username: " + username);
				return null;
			}
			String url = "api/notifications/configure/" + URLEncoder.encode(username, StandardCharsets.UTF_8);
			System.out.println("Debug: GET URL = " + url);
			JSONObject response = httpRequestClient.get(url);
			if (response == null) {
				System.err.println("Error: Null response from server for username: " + username);
				return null;
			}
			boolean success = response.getBoolean("success");
			String message = response.getString("message");
			System.out.println("Debug: Configuration response for username " + username + " - success: " + success
					+ ", message: " + message);
			if (!success) {
				JSONObject defaultConfig = new JSONObject();
				defaultConfig.put("success", true);
				defaultConfig.put("message", "Default configuration for username: " + username);
				JSONObject data = new JSONObject();
				data.put("username", username);
				JSONArray categories = new JSONArray();
				for (String category : VALID_CATEGORIES) {
					JSONObject categoryObj = new JSONObject();
					categoryObj.put("name", category);
					categoryObj.put("enabled", false);
					categories.put(categoryObj);
				}
				data.put("categories", categories);
				data.put("keywords", new JSONArray());
				defaultConfig.put("data", data);
				System.out.println("Debug: Returning default configuration for username: " + username);
				return defaultConfig;
			}
			return response;
		} catch (Exception e) {
			System.err
					.println("Error in getNotificationConfigurations for username " + username + ": " + e.getMessage());
			return null;
		}
	}

	public JSONObject updateNotificationPreference(String username, String category, boolean enabled) {
		try {
			if (category == null || category.trim().isEmpty() || !VALID_CATEGORIES.contains(category.trim())) {
				System.err.println("Error: Invalid category: " + category);
				return null;
			}
			if (username == null || username.trim().isEmpty()) {
				System.err.println("Error: Invalid username: " + username);
				return null;
			}
			String url = "api/notifications/configure?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
					+ "&action=update_category" + "&category=" + URLEncoder.encode(category, StandardCharsets.UTF_8)
					+ "&enabled=" + enabled;
			System.out.println(URLEncoder.encode(username, StandardCharsets.UTF_8));
			System.out.println("Debug: POST URL = " + url);
			JSONObject response = httpRequestClient.post(url, "");
			if (response == null) {
				System.err.println(
						"Error: Null response from server for username: " + username + ", category: " + category);
				return null;
			}
			System.out.println("Debug: Response for username " + username + ", category " + category + " - success: "
					+ response.getBoolean("success") + ", message: " + response.getString("message"));
			return response;
		} catch (Exception e) {
			System.err.println("Error in updateNotificationPreference for username " + username + ", category "
					+ category + ": " + e.getMessage());
			return null;
		}
	}

	public JSONObject addKeyword(String username, String keyword) {
		try {
			if (!inputValidator.isValidKeyword(keyword)) {
				System.err.println("Error: Invalid keyword: " + keyword);
				return null;
			}
			if (username == null || username.trim().isEmpty()) {
				System.err.println("Error: Invalid username: " + username);
				return null;
			}
			String url = "api/notifications/configure?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
					+ "&action=add_keyword" + "&keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
			System.out.println("Debug: POST URL = " + url); // No separate postData since all in URL
			JSONObject response = httpRequestClient.post(url, ""); // Empty body, all params in URL
			if (response == null) {
				System.err.println(
						"Error: Null response from server for username: " + username + ", keyword: " + keyword);
				return null;
			}
			System.out.println("Debug: Response for username " + username + ", keyword " + keyword + " - success: "
					+ response.getBoolean("success") + ", message: " + response.getString("message"));
			return response;
		} catch (Exception e) {
			System.err.println(
					"Error in addKeyword for username " + username + ", keyword " + keyword + ": " + e.getMessage());
			return null;
		}
	}

	public JSONObject getNotifications(String username) {
		try {
			if (username == null || username.trim().isEmpty()) {
				System.err.println("Error: Invalid username: " + username);
				return null;
			}
			String url = "api/notifications/view?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
			System.out.println("Debug: GET URL = " + url);
			JSONObject response = httpRequestClient.get(url);
			if (response == null) {
				System.err.println("Error: Null response from server for username: " + username);
				return null;
			}
			System.out.println("Debug: Response for username " + username + " - success: "
					+ response.getBoolean("success") + ", message: " + response.getString("message"));
			return response;
		} catch (Exception e) {
			System.err.println("Error in getNotifications for username " + username + ": " + e.getMessage());
			return null;
		}
	}
}