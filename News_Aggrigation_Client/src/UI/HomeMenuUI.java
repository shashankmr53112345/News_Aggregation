package UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONObject;

import service.ExternalAPIService;
import service.UserAuthenticationService;

public class HomeMenuUI {
	private final UserAuthenticationService authenticationService;
	private final ExternalAPIService externalApiService;
	private final InputValidator inputValidator;
	private final Scanner userInputScanner;
	private boolean isApplicationRunning;

	public HomeMenuUI(UserAuthenticationService authenticationService, ExternalAPIService externalApiService,
			InputValidator inputValidator) {
		this.authenticationService = authenticationService;
		this.externalApiService = externalApiService;
		this.inputValidator = inputValidator;
		this.userInputScanner = new Scanner(System.in);
		this.isApplicationRunning = true;
	}

	public void startApplication() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		String currentDate = dateFormat.format(new Date());
		String currentTime = timeFormat.format(new Date());
		System.out.printf("\nWelcome to the News Application! Date: %s Time: %s\n", currentDate, currentTime);
		while (isApplicationRunning) {
			displayHomeMenu();
		}
		userInputScanner.close();
	}

	private void displayHomeMenu() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		String currentDate = dateFormat.format(new Date());
		String currentTime = timeFormat.format(new Date());
		System.out.printf("\nWelcome to the News Application! Date: %s Time: %s\n", currentDate, currentTime);
		System.out.println("Please choose the options below");
		System.out.println("1. Login");
		System.out.println("2. Sign up");
		System.out.println("3. Exit");
		System.out.print("Enter your option: ");
		String userChoice = userInputScanner.nextLine();

		switch (userChoice) {
		case "1":
			processLoginRequest();
			break;
		case "2":
			processSignupRequest();
			break;
		case "3":
			isApplicationRunning = false;
			System.out.println("Exiting application.");
			break;
		default:
			System.out.println("Invalid option. Please try again.");
		}
	}

	private void processLoginRequest() {
		System.out.println("\nEnter username:");
		String username = userInputScanner.nextLine();
		System.out.println("Enter password:");
		String password = userInputScanner.nextLine();

		try {
			JSONObject loginResponse = authenticationService.login(username, password);
			if (loginResponse.getBoolean("success")) {
				boolean isAdminUser = loginResponse.getJSONObject("data").getBoolean("isAdmin");
				System.out.println(loginResponse.getString("message"));
				if (isAdminUser) {
					AdminMenuUI adminMenu = new AdminMenuUI(authenticationService, externalApiService, inputValidator,
							userInputScanner, username);
					adminMenu.displayAdminMenu();
				} else {
					UserMenuUI userMenu = new UserMenuUI(inputValidator, userInputScanner, username);
					userMenu.displayUserMenu();
				}
			} else {
				System.out.println("Login failed: " + loginResponse.getString("message"));
			}
		} catch (Exception e) {
			System.out.println("Error during login: " + e.getMessage());
		}
	}

	private void processSignupRequest() {
		System.out.println("\nEnter username:");
		String username = userInputScanner.nextLine();
		System.out.println("Enter email:");
		String email = userInputScanner.nextLine();
		System.out.println("Enter password (minimum 8 characters):");
		String password = userInputScanner.nextLine();

		if (!inputValidator.isValidEmail(email)) {
			System.out.println("Invalid email format (e.g., user@domain.com).");
			return;
		}
		if (!inputValidator.isValidPassword(password)) {
			System.out.println("Password must be at least 8 characters long.");
			return;
		}

		try {
			JSONObject signupResponse = authenticationService.signup(username, email, password, false);
			System.out.println(signupResponse.getString("message"));
		} catch (Exception e) {
			System.out.println("Error during signup: " + e.getMessage());
		}
	}
}