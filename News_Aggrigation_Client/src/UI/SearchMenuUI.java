package UI;

import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.UserNewsOperationsService;

public class SearchMenuUI extends UserBaseMenuUI {
	private final UserNewsOperationsService userNewsOperationsService;

	public SearchMenuUI(InputValidator inputValidator, Scanner userInputScanner, String userId,
			UserNewsOperationsService userNewsOperationsService, boolean isUserMenuActive) {
		super(inputValidator, userInputScanner, userId);
		this.userNewsOperationsService = userNewsOperationsService;
		this.isUserMenuActive = isUserMenuActive;
	}

	public void display() {
		boolean isSearchMenuActive = true;
		while (isSearchMenuActive && isUserMenuActive) {
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

		while (isSearchResultsActive && isUserMenuActive) {
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
}