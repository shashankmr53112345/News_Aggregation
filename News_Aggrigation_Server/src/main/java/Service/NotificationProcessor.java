package Service;

import java.util.ArrayList;
import java.util.List;

import Repository.NotificationRepository;
import model.NewsArticles;
import util.Email;

public class NotificationProcessor {
	private final NotificationService notificationService;

	public NotificationProcessor() {
		this.notificationService = new NotificationService(new NotificationRepository(), new Email());
	}

	public NotificationProcessor(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void processNotifications(List<NewsArticles> newlyFetchedArticles) {
		if (newlyFetchedArticles == null || newlyFetchedArticles.isEmpty()) {
			System.out.println("Debug: No newly fetched articles to process for notifications");
			return;
		}

		int totalNewArticles = newlyFetchedArticles.size();
		int notificationsStored = 0;
		System.out
				.println("Debug: Starting notification processing for " + totalNewArticles + " newly fetched articles");

		// Fetch user preferences
		List<UserPreference> userPreferences = new ArrayList<>();
		try {
			String fetchUserPreferencesSql = "SELECT np.username, u.email, np.categories, np.keywords "
					+ "FROM notification_preferences np JOIN users u ON np.username = u.username";
			try (java.sql.Connection databaseConnection = util.DatabaseConnection.getConnection();
					java.sql.PreparedStatement preparedStatement = databaseConnection
							.prepareStatement(fetchUserPreferencesSql);
					java.sql.ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					String userName = resultSet.getString("username");
					String userEmail = resultSet.getString("email");
					String categoryList = resultSet.getString("categories");
					String keywordList = resultSet.getString("keywords");
					String[] categoryArray = categoryList != null ? categoryList.split(",") : new String[0];
					String[] keywordArray = keywordList != null ? keywordList.split(",") : new String[0];
					userPreferences.add(new UserPreference(userName, userEmail, categoryArray, keywordArray));
				}
				System.out.println("Debug: Fetched " + userPreferences.size() + " user preferences");
			}
		} catch (java.sql.SQLException databaseException) {
			System.err.println("Debug: Error fetching user preferences: " + databaseException.getMessage());
			databaseException.printStackTrace();
			return;
		}

		// Process each article
		for (NewsArticles article : newlyFetchedArticles) {
			String articleId = article.getId();
			String articleTitle = article.getTitle();
			String articleCategory = article.getCategory();
			String articleContent = article.getDescription();
			String articleUrl = article.getUrl();
			String notificationMessage = "New article: " + articleTitle + " (Category: " + articleCategory + ")\nURL: "
					+ articleUrl;

			System.out.println("Debug: Processing article ID: " + articleId + ", Category: " + articleCategory);

			for (UserPreference preference : userPreferences) {
				String username = preference.getUserName();
				String emailId = preference.getUserEmail();
				if (username == null || emailId == null) {
					System.out.println("Debug: Skipping preference for null username or email");
					continue;
				}

				boolean isMatchFound = false;
				if (preference.getCategories() != null) {
					for (String preferredCategory : preference.getCategories()) {
						if (preferredCategory != null && preferredCategory.trim().equalsIgnoreCase(articleCategory)) {
							isMatchFound = true;
							System.out.println(
									"Debug: Match found for category " + preferredCategory + " for user " + username);
							break;
						}
					}
				}
				if (!isMatchFound && preference.getKeywords() != null) {
					isMatchFound = containsKeywords(articleContent, preference.getKeywords());
					if (isMatchFound) {
						System.out.println(
								"Debug: Match found for keyword in article " + articleId + " for user " + username);
					}
				}

				if (isMatchFound) {
					// Use NotificationService to save notification and send email
					boolean saved = notificationService.saveNotification(username, articleId, notificationMessage,
							emailId);
					if (saved) {
						notificationsStored++;
					} else {
						System.err.println("Debug: Failed to store notification for article " + articleId + " for user "
								+ username);
					}
				}
			}
		}

		System.out.println("Debug: Notification processing completed. Total new articles: " + totalNewArticles
				+ ", Notifications stored: " + notificationsStored);
	}

	private boolean containsKeywords(String articleContent, String[] keywords) {
		if (articleContent == null || keywords == null || keywords.length == 0) {
			return false;
		}
		articleContent = articleContent.toLowerCase();
		for (String keyword : keywords) {
			if (keyword != null && articleContent.contains(keyword.toLowerCase().trim())) {
				return true;
			}
		}
		return false;
	}

	private static class UserPreference {
		private final String userName;
		private final String userEmail;
		private final String[] categories;
		private final String[] keywords;

		UserPreference(String userName, String userEmail, String[] categories, String[] keywords) {
			this.userName = userName;
			this.userEmail = userEmail;
			this.categories = categories;
			this.keywords = keywords;
		}

		public String getUserName() {
			return userName;
		}

		public String getUserEmail() {
			return userEmail;
		}

		public String[] getCategories() {
			return categories;
		}

		public String[] getKeywords() {
			return keywords;
		}
	}
}