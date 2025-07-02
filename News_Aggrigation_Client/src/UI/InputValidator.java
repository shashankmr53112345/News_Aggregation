package UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class InputValidator {
	public boolean isValidEmail(String email) {
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		return Pattern.matches(emailRegex, email);
	}

	public boolean isValidPassword(String password) {
		return password.length() >= 8;
	}

	public boolean isValidId(String id) {
		return id.matches("\\d+");
	}

	public boolean isValidBoolean(String input) {
		return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
	}

	public boolean isValidDate(String date) {
		if (date == null || date.trim().isEmpty()) {
			return false;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
}