package UI;

import java.util.Scanner;

import service.UserNewsOperationsService;

public class UserMenuUI extends UserBaseMenuUI {
	private final UserNewsOperationsService userNewsOperationsService;

	public UserMenuUI(InputValidator inputValidator, Scanner userInputScanner, String username,
			UserNewsOperationsService userNewsOperationsService, boolean isUserMenuActive) {
		super(inputValidator, userInputScanner, username);
		this.userNewsOperationsService = userNewsOperationsService;
		this.isUserMenuActive = isUserMenuActive;
	}

	public void display() {
		while (isUserMenuActive) {
			printHeader("News Application");
			System.out.println("U S E R - M E N U\n");
			System.out.println("1. Headlines");
			System.out.println("2. Saved Articles");
			System.out.println("3. Search");
			System.out.println("4. Notifications");
			System.out.println("5. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			switch (choice) {
			case "1":
				HeadlinesMenuUI headlinesMenu = new HeadlinesMenuUI(inputValidator, userInputScanner, userId,
						userNewsOperationsService, isUserMenuActive);
				headlinesMenu.display();
				break;
			case "2":
				SavedArticlesMenuUI savedArticlesMenu = new SavedArticlesMenuUI(inputValidator, userInputScanner,
						userId, userNewsOperationsService, isUserMenuActive);
				savedArticlesMenu.display();
				break;
			case "3":
				SearchMenuUI searchMenu = new SearchMenuUI(inputValidator, userInputScanner, userId,
						userNewsOperationsService, isUserMenuActive);
				searchMenu.display();
				break;
			case "4":
				NotificationsMenuUI notificationsMenu = new NotificationsMenuUI(inputValidator, userInputScanner,
						userId, isUserMenuActive);
				notificationsMenu.display();

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
}