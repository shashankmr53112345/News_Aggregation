package UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.UserNewsOperationsService;

public class HeadlinesMenuUI extends UserBaseMenuUI {
	private final UserNewsOperationsService userNewsOperationsService;

	public HeadlinesMenuUI(InputValidator inputValidator, Scanner userInputScanner, String userId,
			UserNewsOperationsService userNewsOperationsService, boolean isUserMenuActive) {
		super(inputValidator, userInputScanner, userId);
		this.userNewsOperationsService = userNewsOperationsService;
		this.isUserMenuActive = isUserMenuActive;
	}

	public void display() {
		boolean isHeadlinesMenuActive = true;
		while (isHeadlinesMenuActive && isUserMenuActive) {
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
		while (isCategoryMenuActive && isUserMenuActive) {
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

		while (isArticlesMenuActive && isUserMenuActive) {
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
					printArticle(articles.getJSONObject(i), i + 1);
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
}