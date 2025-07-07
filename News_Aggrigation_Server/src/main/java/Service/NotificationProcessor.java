package Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import model.NewsArticles;
import model.UserPreference;
import util.DatabaseConnection;
import util.Email;

public class NotificationProcessor {
	private final NotificationService notificationService;

	public NotificationProcessor() {
		this.notificationService = new NotificationService(new Repository.NotificationRepository(), new Email());
	}

	public NotificationProcessor(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public void processNotifications() {

		try (Connection databaseConnection = DatabaseConnection.getConnection()) {
			try (PreparedStatement timeStmt = databaseConnection.prepareStatement("SELECT NOW()")) {
				try (ResultSet timeRs = timeStmt.executeQuery()) {

				}
			} catch (SQLException e) {
				System.err.println("Warning: Failed to fetch database time: " + e.getMessage());
			}

			Repository.NotificationRepository notificationRepository = new Repository.NotificationRepository();
			List<UserPreference> userPreferences = fetchUserPreferences(databaseConnection);
			if (userPreferences.isEmpty()) {
				System.out.println("Debug: No user preferences found, skipping notification processing");
				return;
			}

			int notificationsStored = 0;
			for (UserPreference preference : userPreferences) {
				String username = preference.getUserName();
				String emailId = preference.getUserEmail();
				if (username == null || emailId == null) {
					System.out.println("Debug: Skipping preference for user due to null username or email");
					continue;
				}

				System.out.println("Debug: Processing preferences for user: " + username + ", Categories: "
						+ Arrays.toString(preference.getCategories()) + ", Keywords: "
						+ Arrays.toString(preference.getKeywords()));

				List<NewsArticles> allArticles = new ArrayList<>();
				if (preference.getCategories().length > 0) {
					System.out.println(
							"Debug: Fetching articles for categories: " + Arrays.toString(preference.getCategories()));
					allArticles.addAll(notificationRepository.fetchArticlesByCategory(preference.getCategories(), 150));
				} else {
					System.out.println("Debug: No valid categories for user " + username + ", skipping category fetch");
				}
				if (preference.getKeywords().length > 0) {
					System.out.println(
							"Debug: Fetching articles for keywords: " + Arrays.toString(preference.getKeywords()));
					allArticles.addAll(notificationRepository.fetchArticlesByKeywords(preference.getKeywords(), 150));
				} else {
					System.out.println("Debug: No valid keywords for user " + username + ", skipping keyword fetch");
				}

				List<NewsArticles> uniqueArticles = allArticles.stream().filter(article -> article.getId() != null)
						.collect(Collectors.collectingAndThen(Collectors.toMap(NewsArticles::getId, article -> article,
								(existing, replacement) -> existing), map -> new ArrayList<>(map.values())));

				System.out.println(
						"Debug: Processing " + uniqueArticles.size() + " unique articles for user " + username);
				System.out.println("Debug: Unique article IDs: " + uniqueArticles.stream()
						.map(article -> "ID: " + article.getId() + ", Title: " + article.getTitle())
						.collect(Collectors.joining("; ")));

				for (NewsArticles article : uniqueArticles) {
					String notificationMessage = createNotificationMessage(article);
					System.out.println("Debug: Attempting to save notification for user: " + username + ", articleId: "
							+ article.getId() + ", title: " + article.getTitle());
					try {
						boolean saved = notificationRepository.saveNotification(username, article.getId(),
								notificationMessage);
						if (saved) {
							notificationsStored++;
							System.out.println("Debug: Notification saved or updated for user: " + username
									+ ", articleId: " + article.getId());
						} else {
							System.err.println("Error: Failed to store notification for articleId: " + article.getId()
									+ " for user: " + username);
						}
					} catch (Exception e) {
						System.err.println("Error: Exception while saving notification for user: " + username
								+ ", articleId: " + article.getId() + ": " + e.getMessage());
						e.printStackTrace();
					}
				}
			}

			System.out
					.println("Debug: Notification processing completed. Notifications stored: " + notificationsStored);
		} catch (SQLException e) {
			System.err.println("Error: Failed to establish database connection: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private List<UserPreference> fetchUserPreferences(Connection databaseConnection) throws SQLException {
		List<UserPreference> userPreferences = new ArrayList<>();
		String fetchUserPreferencesSql = "SELECT np.username, u.email, np.categories, np.keywords "
				+ "FROM notification_preferences np JOIN users u ON np.username = u.username";
		try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(fetchUserPreferencesSql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				String userName = resultSet.getString("username");
				String userEmail = resultSet.getString("email");
				String categoryList = resultSet.getString("categories");
				String keywordList = resultSet.getString("keywords");

				String[] categoryArray = new String[0];
				if (categoryList != null && !categoryList.trim().isEmpty()) {
					categoryArray = Arrays.stream(categoryList.split(",")).map(String::trim)
							.filter(cat -> cat != null && !cat.isEmpty()).map(String::toLowerCase).distinct()
							.toArray(String[]::new);
				}

				String[] keywordArray = new String[0];
				if (keywordList != null && !keywordList.trim().isEmpty()) {
					keywordArray = Arrays.stream(keywordList.split(",")).map(String::trim)
							.filter(kw -> kw != null && !kw.isEmpty()).map(String::toLowerCase).distinct()
							.toArray(String[]::new);
				}

				UserPreference preference = new UserPreference(userName, userEmail, categoryArray, keywordArray);
				userPreferences.add(preference);
				System.out.println(
						"Debug: Fetched preferences for user: " + userName + ", Raw categories: '" + categoryList + "'"
								+ ", Cleaned categories: " + Arrays.toString(categoryArray) + ", Raw keywords: '"
								+ keywordList + "'" + ", Cleaned keywords: " + Arrays.toString(keywordArray));
			}
			System.out.println("Debug: Fetched " + userPreferences.size() + " user preferences");
		}
		return userPreferences;
	}

	private String createNotificationMessage(NewsArticles article) {
		String articleId = article.getId();
		String articleTitle = article.getTitle();
		String articleCategory = article.getCategory();
		String articleUrl = article.getUrl();
		return "New article: " + (articleTitle != null ? articleTitle : "Untitled") + " (Category: "
				+ (articleCategory != null ? articleCategory : "Unknown") + ")\nURL: "
				+ (articleUrl != null ? articleUrl : "No URL available");
	}
}