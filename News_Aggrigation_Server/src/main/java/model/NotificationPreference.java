package model;

import java.util.List;

public class NotificationPreference {
	private String username;
	private List<String> categories;
	private List<String> keywords;

	public NotificationPreference(String username, List<String> categories, List<String> keywords) {
		this.username = username;
		this.categories = categories;
		this.keywords = keywords;
	}

	public String getUsername() {
		return username;
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<String> getKeywords() {
		return keywords;
	}
}