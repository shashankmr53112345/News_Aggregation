package UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import org.json.JSONObject;

public abstract class UserBaseMenuUI {
	protected final InputValidator inputValidator;
	protected final Scanner userInputScanner;
	protected final String userId;
	protected boolean isUserMenuActive;

	public UserBaseMenuUI(InputValidator inputValidator, Scanner userInputScanner, String userId) {
		if (inputValidator == null || userInputScanner == null || userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("InputValidator, Scanner, and userId cannot be null or empty");
		}
		this.inputValidator = inputValidator;
		this.userInputScanner = userInputScanner;
		this.userId = userId;
		this.isUserMenuActive = true;
	}

	protected void printHeader(String title) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			String currentDate = dateFormat.format(new Date());
			String currentTime = timeFormat.format(new Date());
			System.out.printf("\nWelcome to the %s, %s! Date: %s Time: %s\n", title, userId, currentDate, currentTime);
		} catch (Exception e) {
			System.err.println("Error formatting header: " + e.getMessage());
			System.out.printf("\nWelcome to the %s, %s!\n", title, userId);
		}
	}

	protected void printArticle(JSONObject article, int index) {
		System.out.println("\nArticle " + index + ": ID: " + article.getString("id"));
		System.out.println(article.getString("title"));
		System.out.println(article.getString("description"));
		System.out.println("Source: " + article.getString("source"));
		System.out.println("URL: " + article.getString("url"));
		System.out.println("Category: " + article.getString("category"));
		System.out.println("Likes: " + article.getInt("likes"));
		System.out.println("Dislikes: " + article.getInt("dislikes"));
		System.out.println("Report Count: " + article.getInt("report_count"));
	}
}