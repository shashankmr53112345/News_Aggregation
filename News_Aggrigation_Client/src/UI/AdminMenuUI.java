package UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.ExternalAPIService;
import service.UserAuthenticationService;

public class AdminMenuUI {
	private final UserAuthenticationService authenticationService;
	private final ExternalAPIService externalApiService;
	private final InputValidator inputValidator;
	private final Scanner userInputScanner;
	private final String username;
	private boolean isMenuActive;

	public AdminMenuUI(UserAuthenticationService authenticationService, ExternalAPIService externalApiService,
			InputValidator inputValidator, Scanner userInputScanner, String username) {
		this.authenticationService = authenticationService;
		this.externalApiService = externalApiService;
		this.inputValidator = inputValidator;
		this.userInputScanner = userInputScanner;
		this.username = username;
		this.isMenuActive = true;
	}

	public void displayAdminMenu() {
		while (isMenuActive) {
			printAdminMenuOptions();
			processAdminMenuInput();
		}
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
		System.out.println("5. Logout");
		System.out.print("Enter your option: ");
	}

	private void processAdminMenuInput() {
		String userChoice = userInputScanner.nextLine();
		switch (userChoice) {
		case "1":
			displayExternalServersList();
			break;
		case "2":
			displayExternalServerDetails();
			break;
		case "3":
			updateExternalServerDetails();
			break;
		case "4":
			addNewNewsCategory();
			break;
		case "5":
			isMenuActive = false;
			System.out.println("Logged out successfully.");
			break;
		default:
			System.out.println("Invalid option. Please try again.");
		}
	}

	private void displayExternalServersList() {
		try {
			JSONObject externalApisResponse = externalApiService.viewExternalApiDetails();
			if (externalApisResponse.getBoolean("success")) {
				System.out.println("\nList of external servers:");
				JSONArray apisArray = externalApisResponse.getJSONObject("data").getJSONArray("apis");
				for (int i = 0; i < apisArray.length(); i++) {
					JSONObject apiObject = apisArray.getJSONObject(i);
					String apiName = apiObject.optString("name", "Unknown");
					String status = apiObject.optBoolean("active", false) ? "Active" : "Not Active";
					String lastAccessed = apiObject.optString("lastAccessed", "21 Mar 2025");
					System.out.printf("%d. %s - %s - last accessed: %s%n", i + 1, apiName, status, lastAccessed);
				}
			} else {
				System.out.println("Failed to retrieve servers: " + externalApisResponse.getString("message"));
			}
		} catch (Exception e) {
			System.out.println("Error retrieving server list: " + e.getMessage());
		}
	}

	private void displayExternalServerDetails() {
		try {
			JSONObject externalApisResponse = externalApiService.viewExternalApiDetails();
			if (externalApisResponse.getBoolean("success")) {
				System.out.println("\nList of external server details:");
				JSONArray apisArray = externalApisResponse.getJSONObject("data").getJSONArray("apis");
				for (int i = 0; i < apisArray.length(); i++) {
					JSONObject apiObject = apisArray.getJSONObject(i);
					String apiName = apiObject.optString("name", "Unknown");
					String apiKey = apiObject.optString("apiKey", "<API_KEY>");
					System.out.printf("%d. %s - %s%n", i + 1, apiName, apiKey);
				}
			} else {
				System.out.println("Failed to retrieve server details: " + externalApisResponse.getString("message"));
			}
		} catch (Exception e) {
			System.out.println("Error retrieving server details: " + e.getMessage());
		}
	}

	private void updateExternalServerDetails() {
		System.out.println("\nUpdate/Edit the external server's details");
		System.out.println("Enter the external server ID");
		String serverId = userInputScanner.nextLine();
		if (!inputValidator.isValidId(serverId)) {
			System.out.println("Invalid ID format. Please enter a numeric ID.");
			return;
		}
		System.out.println("Enter the updated API key");
		String newApiKey = userInputScanner.nextLine();
		if (newApiKey.trim().isEmpty()) {
			System.out.println("API key cannot be empty.");
			return;
		}

		try {
			JSONObject updateResponse = externalApiService.updateExternalApiKey(serverId, newApiKey);
			System.out.println(updateResponse.getString("message"));
		} catch (Exception e) {
			System.out.println("Error updating API key: " + e.getMessage());
		}
	}

	private void addNewNewsCategory() {
		System.out.println("\nEnter new category name:");
		String categoryName = userInputScanner.nextLine();
		if (categoryName.trim().isEmpty()) {
			System.out.println("Category name cannot be empty.");
			return;
		}

		try {
			JSONObject categoryResponse = externalApiService.addNewsCategory(categoryName);
			System.out.println(categoryResponse.getString("message"));
		} catch (Exception e) {
			System.out.println("Error adding category: " + e.getMessage());
		}
	}
}