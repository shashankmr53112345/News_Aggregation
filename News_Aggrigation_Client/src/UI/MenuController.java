package UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MenuController {
	private final ExternalServerHandler serverHandler;
	private final CategoryHandler categoryHandler;
	private final ArticleHandler articleHandler;
	private final String username;
	private final Scanner scanner;

	public MenuController(ExternalServerHandler serverHandler, CategoryHandler categoryHandler,
			ArticleHandler articleHandler, String username) {
		this.serverHandler = serverHandler;
		this.categoryHandler = categoryHandler;
		this.articleHandler = articleHandler;
		this.username = username;
		this.scanner = new Scanner(System.in);
	}

	public boolean displayMenu() {
		boolean isMenuActive = true;
		while (isMenuActive) {
			printAdminMenuOptions();
			String userChoice = scanner.nextLine().trim();
			switch (userChoice) {
			case "1":
				serverHandler.displayExternalServersList(scanner);
				break;
			case "2":
				serverHandler.displayExternalServerDetails(scanner);
				break;
			case "3":
				serverHandler.updateExternalServerDetails(scanner);
				break;
			case "4":
				categoryHandler.addNewNewsCategory(scanner);
				break;
			case "5":
				articleHandler.hideArticlesMenu(scanner);
				break;
			case "6":
				articleHandler.getReportedArticles();
				break;
			case "7":
				System.out.println("Logged out successfully.");
				isMenuActive = false;
				return true;
			default:
				System.out.println("Invalid option. Please try again.");
			}
		}
		return false;
	}

	private void printAdminMenuOptions() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		String currentDate = dateFormat.format(new Date());
		String currentTime = timeFormat.format(new Date());
		System.out.printf("\nWelcome to the News Application, %s! Date: %s Time: %s\n", username, currentDate,
				currentTime);
		System.out.println("Admin Menu:");
		System.out.println("1. View the list of external servers and status");
		System.out.println("2. View the external server's details");
		System.out.println("3. Update/Edit the external server's details");
		System.out.println("4. Add new News Category");
		System.out.println("5. Hide Articles");
		System.out.println("6. Get Reported Articles");
		System.out.println("7. Logout");
		System.out.print("Enter your option: ");
	}
}