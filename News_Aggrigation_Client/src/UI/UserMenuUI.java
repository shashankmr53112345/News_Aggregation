package UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import service.UserNewsOperationsService;

public class UserMenuUI {
	private final InputValidator inputValidator;
	private final Scanner userInputScanner;
	private final String userId; // Changed to userId for clarity (can still hold username)
	private final UserNewsOperationsService userNewsOperationsService;
	private boolean isUserMenuActive;

	public UserMenuUI(InputValidator inputValidator, Scanner userInputScanner, String userId) {
		if (inputValidator == null || userInputScanner == null || userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("InputValidator, Scanner, and userId cannot be null or empty");
		}
		this.inputValidator = inputValidator;
		this.userInputScanner = userInputScanner;
		this.userId = userId;
		this.userNewsOperationsService = new UserNewsOperationsService();
		this.isUserMenuActive = true;
	}

	public void displayUserMenu() {
		while (isUserMenuActive) {
			printHeader("News Application");
			System.out.println("Please choose an option:");
			System.out.println("1. Headlines");
			System.out.println("2. Saved Articles");
			System.out.println("3. Search");
			System.out.println("4. Notifications");
			System.out.println("5. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			switch (choice) {
			case "1":
				displayHeadlinesMenu();
				break;
			case "2":
				displaySavedArticlesMenu();
				break;
			case "3":
				displaySearchMenu();
				break;
			case "4":
				displayNotificationsMenu();
				break;
			case "5":
				isUserMenuActive = false;
				System.out.println("Logging out...");
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void printHeader(String title) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			String currentDate = dateFormat.format(new Date());
			String currentTime = timeFormat.format(new Date());
			System.out.printf("\nWelcome to the %s, %s! Date: %s Time: %s\n", title, userId, currentDate, currentTime);
		} catch (Exception e) {
			System.err.println("Error formatting header: " + e.getMessage());
			System.out.printf("\nWelcome to the %s, %s!\n", title, userId);
		}
	}

	private void displayHeadlinesMenu() {
		boolean isHeadlinesMenuActive = true;
		while (isHeadlinesMenuActive) {
			printHeader("News Application");
			System.out.println("Please choose an option:");
			System.out.println("1. Today");
			System.out.println("2. Date range");
			System.out.println("3. Back");
			System.out.println("4. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			switch (choice) {
			case "1":
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String currentDate = dateFormat.format(new Date());
				displayCategoryMenu(currentDate, null);
				break;
			case "2":
				System.out.println("Enter start date (yyyy-MM-dd):");
				String startDate = userInputScanner.nextLine();
				if (!inputValidator.isValidDate(startDate)) {
					System.out.println("Invalid date format. Use yyyy-MM-dd.");
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					break;
				}
				System.out.println("Enter end date (yyyy-MM-dd, optional, press Enter to skip):");
				String endDate = userInputScanner.nextLine();
				if (!endDate.isEmpty() && !inputValidator.isValidDate(endDate)) {
					System.out.println("Invalid date format. Use yyyy-MM-dd.");
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					break;
				}
				displayCategoryMenu(startDate, endDate.isEmpty() ? null : endDate);
				break;
			case "3":
				isHeadlinesMenuActive = false;
				break;
			case "4":
				isUserMenuActive = false;
				isHeadlinesMenuActive = false;
				System.out.println("Logging out...");
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void displayCategoryMenu(String startDate, String endDate) {
		boolean isCategoryMenuActive = true;
		while (isCategoryMenuActive) {
			printHeader("News Application");
			System.out.println("Please choose a category for Headlines:");
			System.out.println("1. All");
			System.out.println("2. Business");
			System.out.println("3. Entertainment");
			System.out.println("4. Sports");
			System.out.println("5. Technology");
			System.out.println("6. Back");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			String category = null;
			switch (choice) {
			case "1":
				category = null;
				displayArticles("headlines", startDate, endDate, category);
				break;
			case "2":
				category = "business";
				displayArticles("headlines", startDate, endDate, category);
				break;
			case "3":
				category = "entertainment";
				displayArticles("headlines", startDate, endDate, category);
				break;
			case "4":
				category = "sports";
				displayArticles("headlines", startDate, endDate, category);
				break;
			case "5":
				category = "technology";
				displayArticles("headlines", startDate, endDate, category);
				break;
			case "6":
				isCategoryMenuActive = false;
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void displayArticles(String endpoint, String startDate, String endDate, String category) {
		boolean isArticlesMenuActive = true;
		boolean fetchArticles = true;
		JSONArray articles = null;

		while (isArticlesMenuActive) {
			printHeader("News Application");
			System.out.println("H E A D L I N E S\n");

			if (fetchArticles) {
				JSONObject response = userNewsOperationsService.getArticles(endpoint, userId, startDate, endDate,
						category);
				if (response == null) {
					System.out.println("Error retrieving articles. Please try again.");
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isArticlesMenuActive = false;
					}
					continue;
				}

				try {
					articles = response.getJSONObject("data").getJSONArray("articles");
				} catch (Exception e) {
					System.out.println("Error parsing articles. Please try again.");
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isArticlesMenuActive = false;
					}
					continue;
				}

				if (articles.length() == 0) {
					System.out.println("No articles found.");
					System.out.print("Press Enter to continue or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isArticlesMenuActive = false;
					}
					continue;
				}

				for (int i = 0; i < articles.length(); i++) {
					JSONObject article = articles.getJSONObject(i);
					printArticle(article, i + 1);
				}
				fetchArticles = false;
			}

			// Display menu options
			System.out.println("\n--- Options ---");
			System.out.println("1. Back");
			System.out.println("2. Logout");
			System.out.println("3. Save Article");
			System.out.println("4. Like Article");
			System.out.println("5. Dislike Article");
			System.out.println("6. Report Article");
			System.out.print("\nEnter your option: ");

			String choice = userInputScanner.nextLine();
			switch (choice) {
			case "1":
				isArticlesMenuActive = false;
				break;
			case "2":
				isUserMenuActive = false;
				isArticlesMenuActive = false;
				System.out.println("Logging out...");
				break;
			case "3":
				System.out.println("Enter Article ID to save:");
				String saveArticleId = userInputScanner.nextLine();
				if (!saveArticleId.trim().isEmpty()) {
					System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
					String result = userNewsOperationsService.saveArticle(userId, saveArticleId);
					System.out.println(result);
					if (result.contains("Error")) {
						fetchArticles = true; // Refetch articles on error
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "4":
				System.out.println("Enter Article ID to like:");
				String likeArticleId = userInputScanner.nextLine();
				if (!likeArticleId.trim().isEmpty()) {
					System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
					String result = userNewsOperationsService.likeArticle(userId, likeArticleId);
					System.out.println(result);
					if (result.contains("Error")) {
						fetchArticles = true; // Refetch articles on error
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "5":
				System.out.println("Enter Article ID to dislike:");
				String dislikeArticleId = userInputScanner.nextLine();
				if (!dislikeArticleId.trim().isEmpty()) {
					System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
					String result = userNewsOperationsService.dislikeArticle(userId, dislikeArticleId);
					System.out.println(result);
					if (result.contains("Error")) {
						fetchArticles = true; // Refetch articles on error
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "6":
				System.out.println("Enter Article ID to report:");
				String reportArticleId = userInputScanner.nextLine();
				if (!reportArticleId.trim().isEmpty()) {
					System.out.println("Enter reason for reporting:");
					String reason = userInputScanner.nextLine();
					if (!reason.trim().isEmpty()) {
						System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
						String result = userNewsOperationsService.reportArticle(userId, reportArticleId, reason);
						System.out.println(result);
						if (result.contains("Error")) {
							fetchArticles = true; // Refetch articles on error
						}
					} else {
						System.out.println("Reason cannot be empty.");
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void displaySavedArticlesMenu() {
		boolean isSavedMenuActive = true;
		while (isSavedMenuActive) {
			printHeader("News Application");
			System.out.println("Please choose a category for Saved Articles:");
			System.out.println("1. All");
			System.out.println("2. Business");
			System.out.println("3. Entertainment");
			System.out.println("4. Sports");
			System.out.println("5. Technology");
			System.out.println("6. Back");
			System.out.println("7. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			String category = null;
			switch (choice) {
			case "1":
				category = null;
				break;
			case "2":
				category = "business";
				break;
			case "3":
				category = "entertainment";
				break;
			case "4":
				category = "sports";
				break;
			case "5":
				category = "technology";
				break;
			case "6":
				isSavedMenuActive = false;
				continue;
			case "7":
				isUserMenuActive = false;
				isSavedMenuActive = false;
				System.out.println("Logging out...");
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				continue;
			}

			if (category != null || choice.equals("1")) {
				printHeader("News Application");
				System.out.println("S A V E D\n");
				System.out.println("1. Back");
				System.out.println("2. Logout");
				System.out.println("3. Delete Article");
				System.out.println("4. Like Article");
				System.out.println("5. Dislike Article");
				System.out.println("6. Report Article");

				JSONObject response = userNewsOperationsService.getSavedArticles(userId, category);
				if (response == null) {
					System.out.println("Error retrieving saved articles. Please try again.");
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					continue;
				}
				JSONArray articles;
				try {
					articles = response.getJSONObject("data").getJSONArray("articles");
				} catch (Exception e) {
					System.out.println("Error parsing saved articles. Please try again.");
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					continue;
				}
				if (articles.length() == 0) {
					System.out.println("No saved articles found.");
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					continue;
				}

				for (int i = 0; i < articles.length(); i++) {
					JSONObject article = articles.getJSONObject(i);
					printArticle(article, i + 1);
				}

				System.out.print("\nEnter your option: ");
				String subChoice = userInputScanner.nextLine();
				switch (subChoice) {
				case "1":
					break;
				case "2":
					isUserMenuActive = false;
					isSavedMenuActive = false;
					System.out.println("Logging out...");
					break;
				case "3":
					System.out.println("Enter Article ID to delete:");
					String deleteArticleId = userInputScanner.nextLine();
					System.out.println(userNewsOperationsService.deleteArticle(userId, deleteArticleId));
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					break;
				case "4":
					System.out.println("Enter Article ID to like:");
					String likeArticleId = userInputScanner.nextLine();
					System.out.println(userNewsOperationsService.likeArticle(userId, likeArticleId));
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					break;
				case "5":
					System.out.println("Enter Article ID to dislike:");
					String dislikeArticleId = userInputScanner.nextLine();
					System.out.println(userNewsOperationsService.dislikeArticle(userId, dislikeArticleId));
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					break;
				case "6":
					System.out.println("Enter Article ID to report:");
					String reportArticleId = userInputScanner.nextLine();
					System.out.println("Enter reason for reporting:");
					String reason = userInputScanner.nextLine();
					System.out.println(userNewsOperationsService.reportArticle(userId, reportArticleId, reason));
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
					break;
				default:
					System.out.println("Invalid option. Please try again.");
					System.out.print("Press Enter to continue: ");
					userInputScanner.nextLine();
				}
			}
		}
	}

	private void displaySearchMenu() {
		boolean isSearchMenuActive = true;
		while (isSearchMenuActive) {
			printHeader("News Application");
			System.out.println("S E A R C H\n");
			System.out.println("Enter search query:");
			String query = userInputScanner.nextLine();
			if (query.trim().isEmpty()) {
				System.out.println("Search query cannot be empty.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				continue;
			}
			System.out.println("Enter start date (yyyy-MM-dd, optional, press Enter to skip):");
			String startDate = userInputScanner.nextLine();
			if (!startDate.isEmpty() && !inputValidator.isValidDate(startDate)) {
				System.out.println("Invalid date format. Use yyyy-MM-dd.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				continue;
			}
			System.out.println("Enter end date (yyyy-MM-dd, optional, press Enter to skip):");
			String endDate = userInputScanner.nextLine();
			if (!endDate.isEmpty() && !inputValidator.isValidDate(endDate)) {
				System.out.println("Invalid date format. Use yyyy-MM-dd.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				continue;
			}
			System.out.println("Sort by (1. Likes, 2. None):");
			String sortChoice = userInputScanner.nextLine();
			String sortBy = null;
			switch (sortChoice) {
			case "1":
				sortBy = "likes";
				break;
			case "2":
				sortBy = null;
				break;
			default:
				System.out.println("Invalid sort option. Defaulting to no sorting.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
			displaySearchResults(query, startDate.isEmpty() ? null : startDate, endDate.isEmpty() ? null : endDate,
					sortBy);
			isSearchMenuActive = false;
		}
	}

	private void displaySearchResults(String query, String startDate, String endDate, String sortBy) {
		boolean isSearchResultsActive = true;
		boolean fetchArticles = true;
		JSONArray articles = null;

		while (isSearchResultsActive) {
			printHeader("News Application");
			System.out.println("S E A R C H\n");
			System.out.println("Results for \"" + query + "\"\n");

			if (fetchArticles) {
				JSONObject response = userNewsOperationsService.searchArticles(userId, query, startDate, endDate,
						sortBy);
				if (response == null) {
					System.out.println("Error retrieving search results. Please try again.");
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isSearchResultsActive = false;
					}
					continue;
				}
				try {
					articles = response.getJSONObject("data").getJSONArray("articles");
				} catch (Exception e) {
					System.out.println("Error parsing search results. Please try again.");
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isSearchResultsActive = false;
					}
					continue;
				}
				if (articles.length() == 0) {
					System.out.println("No articles found for query: " + query);
					System.out.print("Press Enter to continue or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isSearchResultsActive = false;
					}
					continue;
				}

				for (int i = 0; i < articles.length(); i++) {
					JSONObject article = articles.getJSONObject(i);
					printArticle(article, i + 1);
				}
				fetchArticles = false;
			}

			System.out.println("\n--- Options ---");
			System.out.println("1. Back");
			System.out.println("2. Logout");
			System.out.println("3. Save Article");
			System.out.println("4. Like Article");
			System.out.println("5. Dislike Article");
			System.out.println("6. Report Article");
			System.out.print("\nEnter your option: ");

			String choice = userInputScanner.nextLine();
			switch (choice) {
			case "1":
				isSearchResultsActive = false;
				break;
			case "2":
				isUserMenuActive = false;
				isSearchResultsActive = false;
				System.out.println("Logging out...");
				break;
			case "3":
				System.out.println("Enter Article ID to save:");
				String saveArticleId = userInputScanner.nextLine();
				if (!saveArticleId.trim().isEmpty()) {
					System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
					String result = userNewsOperationsService.saveArticle(userId, saveArticleId);
					System.out.println(result);
					if (result.contains("Error")) {
						fetchArticles = true;
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "4":
				System.out.println("Enter Article ID to like:");
				String likeArticleId = userInputScanner.nextLine();
				if (!likeArticleId.trim().isEmpty()) {
					System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
					String result = userNewsOperationsService.likeArticle(userId, likeArticleId);
					System.out.println(result);
					if (result.contains("Error")) {
						fetchArticles = true;
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "5":
				System.out.println("Enter Article ID to dislike:");
				String dislikeArticleId = userInputScanner.nextLine();
				if (!dislikeArticleId.trim().isEmpty()) {
					System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
					String result = userNewsOperationsService.dislikeArticle(userId, dislikeArticleId);
					System.out.println(result);
					if (result.contains("Error")) {
						fetchArticles = true;
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "6":
				System.out.println("Enter Article ID to report:");
				String reportArticleId = userInputScanner.nextLine();
				if (!reportArticleId.trim().isEmpty()) {
					System.out.println("Enter reason for reporting:");
					String reason = userInputScanner.nextLine();
					if (!reason.trim().isEmpty()) {
						System.out.println("Debug: UserId = " + (userId != null ? userId : "null"));
						String result = userNewsOperationsService.reportArticle(userId, reportArticleId, reason);
						System.out.println(result);
						if (result.contains("Error")) {
							fetchArticles = true;
						}
					} else {
						System.out.println("Reason cannot be empty.");
					}
				} else {
					System.out.println("Article ID cannot be empty.");
				}
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void displayNotificationsMenu() {
		boolean isNotificationsMenuActive = true;
		while (isNotificationsMenuActive) {
			printHeader("News Application");
			System.out.println("N O T I F I C A T I O N S\n");
			System.out.println("1. View Notifications");
			System.out.println("2. Configure Notifications");
			System.out.println("3. Back");
			System.out.println("4. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			switch (choice) {
			case "1":
				System.out.println("View Notifications: Feature not yet implemented.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "2":
				configureNotifications();
				break;
			case "3":
				isNotificationsMenuActive = false;
				break;
			case "4":
				isUserMenuActive = false;
				isNotificationsMenuActive = false;
				System.out.println("Logging out...");
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void configureNotifications() {
		boolean isConfigMenuActive = true;
		while (isConfigMenuActive) {
			printHeader("News Application");
			System.out.println("C O N F I G U R E - N O T I F I C A T I O N S\n");
			System.out.println("1. Business - Disabled");
			System.out.println("2. Entertainment - Disabled");
			System.out.println("3. Sports - Disabled");
			System.out.println("4. Technology - Disabled");
			System.out.println("5. Keywords - Disabled");
			System.out.println("6. Back");
			System.out.println("7. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			switch (choice) {
			case "1":
			case "2":
			case "3":
			case "4":
				System.out.println("Toggle Category Notification: Feature not yet implemented.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "5":
				System.out.println("Enter keywords (comma-separated, or empty to disable):");
				userInputScanner.nextLine();
				System.out.println("Configure Keywords: Feature not yet implemented.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "6":
				isConfigMenuActive = false;
				break;
			case "7":
				isUserMenuActive = false;
				isConfigMenuActive = false;
				System.out.println("Logging out...");
				break;
			default:
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}

	private void printArticle(JSONObject article, int index) {
		System.out.println("\nArticle " + index + ": ID: " + article.getString("id"));
		System.out.println(article.getString("title"));
		System.out.println(article.getString("description"));
		System.out.println("Source: " + article.getString("source"));
		System.out.println("URL: " + article.getString("url"));
		System.out.println("Category: " + article.getString("category"));
		System.out.println("Likes: " + article.getInt("likes"));
		System.out.println("Dislikes: " + article.getInt("dislikes"));
		System.out.println("Report Count: " + article.getInt("report_count"));
	}
}