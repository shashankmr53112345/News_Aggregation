package UI;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.UserNewsOperationsService;

public class ArticleHandler {
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");
	private final UserNewsOperationsService newsService;
	private final InputValidator inputValidator;
	private final String username;

	public ArticleHandler(UserNewsOperationsService newsService, InputValidator inputValidator, String username) {
		this.newsService = newsService;
		this.inputValidator = inputValidator;
		this.username = username;
	}

	public void hideArticlesMenu(Scanner scanner) {
		boolean hideMenuActive = true;
		while (hideMenuActive) {
			System.out.println("\nHide Articles Menu:");
			System.out.println("1. Hide by Article ID");
			System.out.println("2. Hide by Keyword");
			System.out.println("3. Hide by Category");
			System.out.println("4. Back to Admin Menu");
			System.out.print("Enter your option: ");

			String choice = scanner.nextLine().trim();
			switch (choice) {
			case "1":
				hideArticleById(scanner);
				break;
			case "2":
				hideArticlesByKeyword(scanner);
				break;
			case "3":
				hideArticlesByCategory(scanner);
				break;
			case "4":
				hideMenuActive = false;
				break;
			default:
				System.out.println("Invalid option. Please try again.");
			}
		}
	}

	private void hideArticleById(Scanner scanner) {
		System.out.println("\nEnter article ID to hide:");
		String articleId = scanner.nextLine().trim();
		if (!inputValidator.isValidId(articleId)) {
			System.out.println("Invalid article ID format. Please enter a valid ID.");
			return;
		}

		try {
			JSONObject response = newsService.hideArticleById(username, articleId);
			System.out.println(response.getString("message"));
		} catch (Exception e) {
			System.out.println("Error hiding article: " + e.getMessage());
		}
	}

	private void hideArticlesByKeyword(Scanner scanner) {
		System.out.println("\nEnter keyword to hide articles:");
		String keyword = scanner.nextLine().trim();
		if (keyword.isEmpty()) {
			System.out.println("Keyword cannot be empty.");
			return;
		}
		if (!inputValidator.isValidKeyword(keyword)) {
			System.out.println("Invalid keyword format. Please enter a valid keyword (max 255 characters).");
			return;
		}

		try {
			JSONObject response = newsService.hideArticlesByKeyword(username, keyword);
			System.out.println(response.getString("message"));
		} catch (Exception e) {
			System.out.println("Error hiding articles by keyword: " + e.getMessage());
		}
	}

	private void hideArticlesByCategory(Scanner scanner) {
		System.out.println(
				"\nEnter category to hide articles (valid categories: " + String.join(", ", VALID_CATEGORIES) + "):");
		String category = scanner.nextLine().trim();
		if (category.isEmpty()) {
			System.out.println("Category cannot be empty.");
			return;
		}

		try {
			JSONObject response = newsService.hideArticlesByCategory(username, category);
			System.out.println(response.getString("message"));
		} catch (Exception e) {
			System.out.println("Error hiding articles by category: " + e.getMessage());
		}
	}

	public void getReportedArticles() {
		try {
			JSONObject response = newsService.getReportedArticles();
			if (response != null && response.getBoolean("success")) {
				System.out.println("\nReported Articles:");
				JSONArray reportsArray = response.getJSONObject("data").getJSONArray("reports");
				if (reportsArray.length() == 0) {
					System.out.println("No reported articles found.");
				} else {
					for (int i = 0; i < reportsArray.length(); i++) {
						JSONObject report = reportsArray.getJSONObject(i);
						System.out.printf("Report %d: Username: %s, Article ID: %s, Reason: %s%n", i + 1,
								report.getString("username"), report.getString("articleId"),
								report.getString("reason"));
					}
				}
			} else {
				System.out.println("Failed to retrieve reported articles: "
						+ (response != null ? response.getString("message") : "Unknown error"));
			}
		} catch (Exception e) {
			System.out.println("Error retrieving reported articles: " + e.getMessage());
		}
	}
}