package UI;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.NotificationService;

public class NotificationsMenuUI extends UserBaseMenuUI {
	private final NotificationService notificationService;
	private final List<String> categories = Arrays.asList("Business", "Entertainment", "Sports", "Technology",
			"Keywords");
	private static final DateTimeFormatter IST_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
			.withZone(ZoneId.of("Asia/Kolkata"));

	public NotificationsMenuUI(InputValidator inputValidator, Scanner userInputScanner, String username,
			boolean isUserMenuActive) {
		super(inputValidator, userInputScanner, username);
		this.notificationService = new NotificationService();
		this.isUserMenuActive = isUserMenuActive;
	}

	public void display() {
		boolean isNotificationsMenuActive = true;
		while (isNotificationsMenuActive && isUserMenuActive) {
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
				displayViewNotifications();
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

	private void displayViewNotifications() {
		boolean isViewNotificationsActive = true;
		boolean fetchNotifications = true;
		JSONArray notifications = null;

		while (isViewNotificationsActive && isUserMenuActive) {
			printHeader("News Application");
			System.out.println("V I E W - N O T I F I C A T I O N S\n");

			if (fetchNotifications) {
				System.out.println("Debug: Fetching notifications for username: " + userId);
				JSONObject response = notificationService.getNotifications(userId);
				if (response == null) {
					System.out.println(
							"Error: Unable to retrieve notifications. Server may be down or username is invalid.");
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isViewNotificationsActive = false;
					}
					continue;
				}

				try {
					if (!response.getBoolean("success")) {
						System.out.println("Error: " + response.getString("message"));
						System.out.print("Press Enter to retry or 'b' to go back: ");
						String input = userInputScanner.nextLine();
						if (input.equalsIgnoreCase("b")) {
							isViewNotificationsActive = false;
						}
						continue;
					}
					notifications = response.getJSONObject("data").getJSONArray("notifications");
				} catch (Exception e) {
					System.out.println("Error parsing notifications: " + e.getMessage());
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isViewNotificationsActive = false;
					}
					continue;
				}

				if (notifications.length() == 0) {
					System.out.println("No notifications found.");
					System.out.print("Press Enter to continue or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isViewNotificationsActive = false;
					}
					continue;
				}

				System.out.println("Notifications for " + userId + ":");
				for (int i = 0; i < notifications.length(); i++) {
					JSONObject notification = notifications.getJSONObject(i);
					String createdAt = notification.getString("createdAt");
					// Parse and convert to IST
					LocalDateTime dateTime = LocalDateTime.parse(createdAt);
					String istTime = IST_FORMATTER.format(dateTime);
					System.out.printf("%d. Article ID: %s\n   Message: %s\n   Sent At: %s\n", (i + 1),
							notification.getString("articleId"), notification.getString("message"), istTime);
				}
				fetchNotifications = false;
			}

			System.out.println("\n--- Options ---");
			System.out.println("1. Back");
			System.out.println("2. Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			switch (choice) {
			case "1":
				isViewNotificationsActive = false;
				break;
			case "2":
				isUserMenuActive = false;
				isViewNotificationsActive = false;
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
		boolean fetchConfig = true;
		JSONObject configResponse = null;

		while (isConfigMenuActive && isUserMenuActive) {
			printHeader("News Application");
			System.out.println("C O N F I G U R E - N O T I F I C A T I O N S\n");

			if (fetchConfig) {
				System.out.println("Debug: Fetching notification configurations for username: " + userId);
				configResponse = notificationService.getNotificationConfigurations(userId);
				if (configResponse == null) {
					System.out.println(
							"Error: Unable to retrieve notification configurations. Server may be down or username is invalid.");
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isConfigMenuActive = false;
					}
					continue;
				}

				try {
					if (!configResponse.getBoolean("success")) {
						System.out.println("Error: " + configResponse.getString("message"));
						System.out.print("Press Enter to retry or 'b' to go back: ");
						String input = userInputScanner.nextLine();
						if (input.equalsIgnoreCase("b")) {
							isConfigMenuActive = false;
						}
						continue;
					}

					JSONArray categoriesArray = configResponse.getJSONObject("data").getJSONArray("categories");
					JSONArray keywordsArray = configResponse.getJSONObject("data").getJSONArray("keywords");

					System.out.println("Current Notification Preferences:");
					for (int i = 0; i < categoriesArray.length(); i++) {
						JSONObject category = categoriesArray.getJSONObject(i);
						String status = category.getBoolean("enabled") ? "Enabled" : "Disabled";
						System.out.printf("%d. %s - %s\n", (i + 1), category.getString("name"), status);
					}
					System.out
							.println("\nKeywords: " + (keywordsArray.length() > 0 ? keywordsArray.toString() : "None"));
				} catch (Exception e) {
					System.out.println("Error parsing notification configurations: " + e.getMessage());
					System.out.print("Press Enter to retry or 'b' to go back: ");
					String input = userInputScanner.nextLine();
					if (input.equalsIgnoreCase("b")) {
						isConfigMenuActive = false;
					}
					continue;
				}
				fetchConfig = false;
			}

			System.out.println("\n--- Options ---");
			for (int i = 0; i < categories.size(); i++) {
				System.out.printf("%d. Toggle %s\n", (i + 1), categories.get(i));
			}
			System.out.println((categories.size() + 1) + ". Add Keyword");
			System.out.println((categories.size() + 2) + ". Back");
			System.out.println((categories.size() + 3) + ". Logout");
			System.out.print("Enter your option: ");
			String choice = userInputScanner.nextLine();

			int choiceNum;
			try {
				choiceNum = Integer.parseInt(choice);
			} catch (NumberFormatException e) {
				System.out.println("Invalid option. Please enter a number.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
				continue;
			}

			if (choiceNum >= 1 && choiceNum <= categories.size()) {
				String category = categories.get(choiceNum - 1);
				System.out
						.printf("Toggle %s (current: %s). Enable? (true/false): ", category,
								configResponse != null && configResponse.getJSONObject("data")
										.getJSONArray("categories").getJSONObject(choiceNum - 1).getBoolean("enabled")
												? "Enabled"
												: "Disabled");
				String enabledInput = userInputScanner.nextLine();
				if (!inputValidator.isValidBoolean(enabledInput)) {
					System.out.println("Invalid input. Please enter 'true' or 'false'.");
				} else {
					boolean enabled = Boolean.parseBoolean(enabledInput);
					JSONObject result = notificationService.updateNotificationPreference(userId, category, enabled);
					System.out.println("Category " + category + ": " + result);
					fetchConfig = true;
				}
			} else if (choiceNum == categories.size() + 1) {
				// Check if Keywords category is enabled
				boolean keywordsEnabled = false;
				if (configResponse != null) {
					JSONArray categoriesArray = configResponse.getJSONObject("data").getJSONArray("categories");
					for (int i = 0; i < categoriesArray.length(); i++) {
						JSONObject category = categoriesArray.getJSONObject(i);
						if (category.getString("name").equals("Keywords") && category.getBoolean("enabled")) {
							keywordsEnabled = true;
							break;
						}
					}
				}
				if (!keywordsEnabled) {
					System.out.println("Error: Keywords category must be enabled to add keywords.");
				} else {
					System.out.println("Enter keywords (comma-separated):");
					String keywordsInput = userInputScanner.nextLine();
					if (keywordsInput.trim().isEmpty()) {
						System.out.println("No keywords provided.");
					} else {
						String[] keywords = keywordsInput.split(",");
						for (String keyword : keywords) {
							keyword = keyword.trim();
							if (!inputValidator.isValidKeyword(keyword)) {
								System.out.println("Invalid keyword: " + keyword);
								continue;
							}
							JSONObject result = notificationService.addKeyword(userId, keyword);
							System.out.println("Keyword '" + keyword + "': " + result);
						}
					}
				}
				fetchConfig = true;
			} else if (choiceNum == categories.size() + 2) {
				isConfigMenuActive = false;
			} else if (choiceNum == categories.size() + 3) {
				isUserMenuActive = false;
				isConfigMenuActive = false;
				System.out.println("Logging out...");
			} else {
				System.out.println("Invalid option. Please try again.");
				System.out.print("Press Enter to continue: ");
				userInputScanner.nextLine();
			}
		}
	}
}