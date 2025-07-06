package UI;

import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.UserNewsOperationsService;

public class SavedArticlesMenuUI extends UserBaseMenuUI {
	private final UserNewsOperationsService userNewsOperationsService;

	public SavedArticlesMenuUI(InputValidator inputValidator, Scanner userInputScanner, String userId,
			UserNewsOperationsService userNewsOperationsService, boolean isUserMenuActive) {
		super(inputValidator, userInputScanner, userId);
		this.userNewsOperationsService = userNewsOperationsService;
		this.isUserMenuActive = isUserMenuActive;
	}

	public void display() {
		boolean isSavedMenuActive = true;
		while (isSavedMenuActive && isUserMenuActive) {
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
					printArticle(articles.getJSONObject(i), i + 1);
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
}