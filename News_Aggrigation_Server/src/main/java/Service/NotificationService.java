package Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.NotificationRepository;
import jakarta.servlet.http.HttpServletResponse;
import model.Notification;
import model.NotificationPreference;
import util.Email;
import util.JsonResponse;

public class NotificationService {
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");
	private final NotificationRepository notificationRepository;
	private final Email emailSender;

	public NotificationService(NotificationRepository notificationRepository, Email emailSender) {
		this.notificationRepository = notificationRepository;
		this.emailSender = emailSender;
	}

	public void getNotificationPreferences(String username, HttpServletResponse response) throws IOException {
		logger.debug("Retrieving notification preferences for username: {}", username);
		response.setContentType("application/json");
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(username);
			if (preference == null) {
				logger.warn("No preferences found for username: {}", username);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "No preferences found for user: " + username);
				return;
			}

			JSONArray categoriesArray = new JSONArray();
			for (String category : VALID_CATEGORIES) {
				JSONObject categoryObj = new JSONObject();
				categoryObj.put("name", category);
				categoryObj.put("enabled", preference.getCategories().contains(category.toLowerCase()));
				categoriesArray.put(categoryObj);
			}

			JSONObject data = new JSONObject();
			data.put("username", username);
			data.put("categories", categoriesArray);
			data.put("keywords", new JSONArray(preference.getKeywords()));

			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response, "Notification preferences retrieved", data);
			logger.info("Retrieved preferences for username: {}", username);
		} catch (SQLException e) {
			logger.error("Database error retrieving preferences for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error retrieving preferences for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	public void updateNotificationPreference(String username, String category, Boolean enabled,
			HttpServletResponse response) throws IOException {
		logger.debug("Updating notification preference for username: {}, category: {}, enabled: {}", username, category,
				enabled);
		response.setContentType("application/json");
		if (!VALID_CATEGORIES.contains(category)) {
			logger.warn("Invalid category: {} for username: {}", category, username);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Invalid category: " + category);
			return;
		}
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(username);
			if (preference == null) {
				preference = new NotificationPreference(username, new ArrayList<>(), new ArrayList<>());
			}
			List<String> categories = new ArrayList<>(preference.getCategories());
			if (enabled) {
				if (!categories.contains(category.toLowerCase())) {
					categories.add(category.toLowerCase());
				}
			} else {
				categories.remove(category.toLowerCase());
			}
			boolean success = notificationRepository.saveNotificationPreference(username, categories,
					preference.getKeywords());
			if (success) {
				String email = notificationRepository.getUserEmail(username);
				logger.info("Updated category {} to {} for username: {}, email: {}", category, enabled, username,
						email);
			} else {
				logger.warn("Failed to update category {} for username: {}", category, username);
			}
			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response,
					success ? "Category " + category + " updated" : "Failed to update category", null);
		} catch (SQLException e) {
			logger.error("Database error updating preference for username: {}, category: {}", username, category, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error updating preference for username: {}, category: {}", username, category, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	public void addKeyword(String username, String keyword, HttpServletResponse response) throws IOException {
		logger.debug("Adding keyword: {} for username: {}", keyword, username);
		response.setContentType("application/json");
		if (keyword == null || keyword.trim().isEmpty() || keyword.length() > 255) {
			logger.warn("Invalid or missing keyword for username: {}", username);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Invalid or missing keyword (max 255 characters)");
			return;
		}
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(username);
			if (preference == null || !preference.getCategories().contains("keywords")) {
				logger.warn("Keywords category not enabled for username: {}", username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Keywords category must be enabled to add keywords");
				return;
			}
			List<String> keywords = new ArrayList<>(preference.getKeywords());
			String keywordLower = keyword.toLowerCase();
			if (!keywords.contains(keywordLower)) {
				keywords.add(keywordLower);
				boolean success = notificationRepository.saveNotificationPreference(username,
						preference.getCategories(), keywords);
				if (success) {
					String email = notificationRepository.getUserEmail(username);
					logger.info("Added keyword: {} for username: {}, email: {}", keywordLower, username, email);
				} else {
					logger.warn("Failed to add keyword: {} for username: {}", keywordLower, username);
				}
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, success ? "Keyword added" : "Failed to add keyword", null);
			} else {
				logger.warn("Keyword already exists: {} for username: {}", keywordLower, username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Keyword already exists");
			}
		} catch (SQLException e) {
			logger.error("Database error adding keyword for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error adding keyword for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	public void deleteKeyword(String username, String keyword, HttpServletResponse response) throws IOException {
		logger.debug("Deleting keyword: {} for username: {}", keyword, username);
		response.setContentType("application/json");
		if (keyword == null || keyword.trim().isEmpty()) {
			logger.warn("Invalid or missing keyword for username: {}", username);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Invalid or missing keyword");
			return;
		}
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(username);
			if (preference == null || !preference.getCategories().contains("keywords")) {
				logger.warn("Keywords category not enabled for username: {}", username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Keywords category must be enabled to delete keywords");
				return;
			}
			List<String> keywords = new ArrayList<>(preference.getKeywords());
			String keywordLower = keyword.toLowerCase();
			if (keywords.contains(keywordLower)) {
				keywords.remove(keywordLower);
				boolean success = notificationRepository.saveNotificationPreference(username,
						preference.getCategories(), keywords);
				if (success) {
					String email = notificationRepository.getUserEmail(username);
					logger.info("Deleted keyword: {} for username: {}, email: {}", keywordLower, username, email);
				} else {
					logger.warn("Failed to delete keyword: {} for username: {}", keywordLower, username);
				}
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, success ? "Keyword removed" : "Failed to remove keyword", null);
			} else {
				logger.warn("Keyword not found: {} for username: {}", keywordLower, username);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "Keyword not found");
			}
		} catch (SQLException e) {
			logger.error("Database error deleting keyword for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error deleting keyword for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	public void clearNotifications(String username, HttpServletResponse response) throws IOException {
		logger.debug("Clearing notifications for username: {}", username);
		response.setContentType("application/json");
		try {
			boolean success = notificationRepository.deleteNotifications(username);
			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response, success ? "Notifications cleared" : "No notifications to clear", null);
			logger.info("Notifications cleared for username: {}, success: {}", username, success);
		} catch (SQLException e) {
			logger.error("Database error clearing notifications for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error clearing notifications for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	public List<Notification> getNotifications(String username) throws SQLException {
		logger.debug("Fetching notifications for username: {}", username);
		return notificationRepository.getNotifications(username);
	}

	public NotificationPreference getNotificationPreference(String username) throws SQLException {
		logger.debug("Fetching notification preference for username: {}", username);
		return notificationRepository.getNotificationPreference(username);
	}

	public String getUserEmail(String username) throws SQLException {
		logger.debug("Fetching user email for username: {}", username);
		return notificationRepository.getUserEmail(username);
	}
}