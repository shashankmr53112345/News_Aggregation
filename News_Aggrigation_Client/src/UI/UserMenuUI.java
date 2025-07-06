package UI;

import java.util.Scanner;

public class UserMenuUI extends UserBaseMenuUI {
	public UserMenuUI(InputValidator inputValidator, Scanner userInputScanner, String username,
			boolean isUserMenuActive) {
		super(inputValidator, userInputScanner, username);
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
				System.out.println("Viewing headlines... (To be implemented)");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "2":
				System.out.println("Viewing saved articles... (To be implemented)");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				break;
			case "3":
				System.out.println("Searching articles... (To be implemented)");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
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