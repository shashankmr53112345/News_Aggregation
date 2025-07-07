package UI;

import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import service.ExternalAPIService;

public class ExternalServerHandler {
	private final ExternalAPIService externalApiService;
	private final InputValidator inputValidator;

	public ExternalServerHandler(ExternalAPIService externalApiService, InputValidator inputValidator) {
		this.externalApiService = externalApiService;
		this.inputValidator = inputValidator;
	}

	public void displayExternalServersList(Scanner scanner) {
		try {
			JSONObject response = externalApiService.viewExternalApiDetails();
			if (response.getBoolean("success")) {
				System.out.println("\nList of external servers:");
				JSONArray apisArray = response.getJSONObject("data").getJSONArray("apis");
				for (int i = 0; i < apisArray.length(); i++) {
					JSONObject apiObject = apisArray.getJSONObject(i);
					String apiName = apiObject.optString("name", "Unknown");
					String status = apiObject.optBoolean("active", false) ? "Active" : "Not Active";
					String lastAccessed = apiObject.optString("lastAccessed", "Unknown");
					System.out.printf("%d. %s - %s - last accessed: %s%n", i + 1, apiName, status, lastAccessed);
				}
			} else {
				System.out.println("Failed to retrieve servers: " + response.getString("message"));
			}
		} catch (Exception e) {
			System.out.println("Error retrieving server list: " + e.getMessage());
		}
	}

	public void displayExternalServerDetails(Scanner scanner) {
		try {
			JSONObject response = externalApiService.viewExternalApiDetails();
			if (response.getBoolean("success")) {
				System.out.println("\nList of external server details:");
				JSONArray apisArray = response.getJSONObject("data").getJSONArray("apis");
				for (int i = 0; i < apisArray.length(); i++) {
					JSONObject apiObject = apisArray.getJSONObject(i);
					String apiName = apiObject.optString("name", "Unknown");
					String apiKey = apiObject.optString("apiKey", "<API_KEY>");
					System.out.printf("%d. %s - %s%n", i + 1, apiName, apiKey);
				}
			} else {
				System.out.println("Failed to retrieve server details: " + response.getString("message"));
			}
		} catch (Exception e) {
			System.out.println("Error retrieving server details: " + e.getMessage());
		}
	}

	public void updateExternalServerDetails(Scanner scanner) {
		System.out.println("\nUpdate/Edit the external server's details");
		System.out.println("Enter the external server ID:");
		String serverId = scanner.nextLine().trim();
		if (!inputValidator.isValidId(serverId)) {
			System.out.println("Invalid ID format. Please enter a numeric ID.");
			return;
		}
		System.out.println("Enter the updated API key:");
		String newApiKey = scanner.nextLine().trim();
		if (newApiKey.isEmpty()) {
			System.out.println("API key cannot be empty.");
			return;
		}

		try {
			JSONObject response = externalApiService.updateExternalApiKey(serverId, newApiKey);
			System.out.println(response.getString("message"));
		} catch (Exception e) {
			System.out.println("Error updating API key: " + e.getMessage());
		}
	}
}