package UI;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import service.ExternalAPIService;

public class CategoryHandler {
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");
	private final ExternalAPIService externalApiService;
	private final InputValidator inputValidator;

	public CategoryHandler(ExternalAPIService externalApiService, InputValidator inputValidator) {
		this.externalApiService = externalApiService;
		this.inputValidator = inputValidator;
	}

	public void addNewNewsCategory(Scanner scanner) {
		System.out.println("\nEnter new category name:");
		String categoryName = scanner.nextLine().trim();
		if (categoryName.isEmpty()) {
			System.out.println("Category name cannot be empty.");
			return;
		}

		try {
			JSONObject response = externalApiService.addNewsCategory(categoryName);
			System.out.println(response.getString("message"));
		} catch (Exception e) {
			System.out.println("Error adding category: " + e.getMessage());
		}
	}
}