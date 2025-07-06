package Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONObject;

import Repository.NotificationRepository;
import model.Notification;
import model.NotificationPreference;
import util.Email;
import util.JsonResponseBody;

public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final Email emailSender;
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");

	public NotificationService(NotificationRepository notificationRepository, Email emailSender) {
		this.notificationRepository = notificationRepository;
		this.emailSender = emailSender;
	}

	public JsonResponseBody getNotificationPreferences(String userName) {
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(userName);
			if (preference == null) {
				return new JsonResponseBody(false, "No preferences found for user: " + userName);
			}

			JSONArray categoriesArray = new JSONArray();
			for (String category : VALID_CATEGORIES) {
				JSONObject categoryObj = new JSONObject();
				categoryObj.put("name", category);
				categoryObj.put("enabled", preference.getCategories().contains(category.toLowerCase()));
				categoriesArray.put(categoryObj);
			}

			JSONArray keywordsArray = new JSONArray(preference.getKeywords());

			JSONObject data = new JSONObject();
			data.put("userName", userName);
			data.put("categories", categoriesArray);
			data.put("keywords", keywordsArray);

			return new JsonResponseBody(true, "Notification preferences retrieved", data);
		} catch (Exception e) {
			System.err.println("Error retrieving preferences for user " + userName + ": " + e.getMessage());
			e.printStackTrace();
			return new JsonResponseBody(false, "Error retrieving preferences: " + e.getMessage());
		}
	}

	public JsonResponseBody updateNotificationPreference(String userName, String category, Boolean enabled) {
		if (!VALID_CATEGORIES.contains(category)) {
			return new JsonResponseBody(false, "Invalid category: " + category);
		}
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(userName);
			if (preference == null) {
				preference = new NotificationPreference(userName, new ArrayList<>(), new ArrayList<>());
			}
			List<String> categories = new ArrayList<>(preference.getCategories());
			if (enabled) {
				if (!categories.contains(category.toLowerCase())) {
					categories.add(category.toLowerCase());
				}
			} else {
				categories.remove(category.toLowerCase());
			}
			boolean success = notificationRepository.saveNotificationPreference(userName, categories,
					preference.getKeywords());
			if (success) {
				String emailAddress = notificationRepository.getUserEmail(userName);
				if (emailAddress != null) {
					try {
						emailSender.sendEmail(emailAddress, "Notification Preference Updated", "Category " + category
								+ " has been " + (enabled ? "enabled" : "disabled") + " for user " + userName);
						System.out.println("Debug: Email sent to " + emailAddress + " for preference update");
					} catch (MessagingException e) {
						System.err
								.println("Warning: Failed to send email for user " + userName + ": " + e.getMessage());
					}
				}
			}
			return new JsonResponseBody(success,
					success ? "Category " + category + " updated" : "Failed to update category");
		} catch (Exception e) {
			System.err.println("Error updating preference for user " + userName + ", category " + category + ": "
					+ e.getMessage());
			e.printStackTrace();
			return new JsonResponseBody(false, "Error updating preference: " + e.getMessage());
		}
	}

	public JsonResponseBody addKeyword(String userName, String keyword) {
		if (keyword == null || keyword.trim().isEmpty() || keyword.length() > 255) {
			return new JsonResponseBody(false, "Invalid or missing keyword (max 255 characters)");
		}
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(userName);
			if (preference == null || !preference.getCategories().contains("keywords")) {
				return new JsonResponseBody(false, "Keywords category must be enabled to add keywords");
			}
			List<String> keywords = new ArrayList<>(preference.getKeywords());
			String keywordLower = keyword.toLowerCase();
			if (!keywords.contains(keywordLower)) {
				keywords.add(keywordLower);
				boolean success = notificationRepository.saveNotificationPreference(userName,
						preference.getCategories(), keywords);
				if (success) {
					String emailAddress = notificationRepository.getUserEmail(userName);
					if (emailAddress != null) {
						try {
							emailSender.sendEmail(emailAddress, "Keyword Added",
									"Keyword " + keyword + " has been added for user " + userName);
							System.out.println("Debug: Email sent to " + emailAddress + " for keyword addition");
						} catch (MessagingException e) {
							System.err.println(
									"Warning: Failed to send email for user " + userName + ": " + e.getMessage());
						}
					}
				}
				return new JsonResponseBody(success, success ? "Keyword added" : "Failed to add keyword");
			}
			return new JsonResponseBody(false, "Keyword already exists");
		} catch (Exception e) {
			System.err.println("Error adding keyword for user " + userName + ": " + e.getMessage());
			e.printStackTrace();
			return new JsonResponseBody(false, "Error adding keyword: " + e.getMessage());
		}
	}

	public JsonResponseBody deleteKeyword(String userName, String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return new JsonResponseBody(false, "Invalid or missing keyword");
		}
		try {
			NotificationPreference preference = notificationRepository.getNotificationPreference(userName);
			if (preference == null || !preference.getCategories().contains("keywords")) {
				return new JsonResponseBody(false, "Keywords category must be enabled to delete keywords");
			}
			List<String> keywords = new ArrayList<>(preference.getKeywords());
			String keywordLower = keyword.toLowerCase();
			if (keywords.contains(keywordLower)) {
				keywords.remove(keywordLower);
				boolean success = notificationRepository.saveNotificationPreference(userName,
						preference.getCategories(), keywords);
				if (success) {
					String emailAddress = notificationRepository.getUserEmail(userName);
					if (emailAddress != null) {
						try {
							emailSender.sendEmail(emailAddress, "Keyword Removed",
									"Keyword " + keyword + " has been removed for user " + userName);
							System.out.println("Debug: Email sent to " + emailAddress + " for keyword removal");
						} catch (MessagingException e) {
							System.err.println(
									"Warning: Failed to send email for user " + userName + ": " + e.getMessage());
						}
					}
				}
				return new JsonResponseBody(success, success ? "Keyword removed" : "Failed to remove keyword");
			}
			return new JsonResponseBody(false, "Keyword not found");
		} catch (Exception e) {
			System.err.println("Error deleting keyword for user " + userName + ": " + e.getMessage());
			e.printStackTrace();
			return new JsonResponseBody(false, "Error deleting keyword: " + e.getMessage());
		}
	}

	public List<Notification> getNotifications(String userName) {
		return notificationRepository.getNotifications(userName);
	}

	public boolean saveNotification(String userName, String articleId, String message, String emailAddress) {
		try {
			boolean saved = notificationRepository.saveNotification(userName, articleId, message);
			if (saved) {
				System.out.println("Debug: Notification saved for user: " + userName + ", articleId: " + articleId);
				if (emailAddress != null) {
					try {
						emailSender.sendEmail(emailAddress, "News Alert: " + message, message);
						System.out.println(
								"Debug: Notification email sent to " + emailAddress + " for article " + articleId);
					} catch (MessagingException e) {
						System.err.println("Warning: Failed to send notification email to " + emailAddress + ": "
								+ e.getMessage());
					}
				}
			} else {
				System.err.println(
						"Error: Failed to save notification for user: " + userName + ", articleId: " + articleId);
			}
			return saved;
		} catch (Exception e) {
			System.err.println("Error in saveNotification for user: " + userName + ", articleId: " + articleId + ": "
					+ e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public JsonResponseBody clearNotifications(String userName) {
		try {
			boolean success = notificationRepository.deleteNotifications(userName);
			return new JsonResponseBody(success, success ? "Notifications cleared" : "Failed to clear notifications");
		} catch (Exception e) {
			System.err.println("Error clearing notifications for user " + userName + ": " + e.getMessage());
			e.printStackTrace();
			return new JsonResponseBody(false, "Error clearing notifications: " + e.getMessage());
		}
	}

	public NotificationPreference getNotificationPreference(String userName) {
		return notificationRepository.getNotificationPreference(userName);
	}

	public String getUserEmail(String userName) {
		return notificationRepository.getUserEmail(userName);
	}
}