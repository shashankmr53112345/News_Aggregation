package model;

import java.util.Arrays;

public class UserPreference {
    private final String userName;
    private final String userEmail;
    private final String[] categories;
    private final String[] keywords;

    public UserPreference(String userName, String userEmail, String[] categories, String[] keywords) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.categories = categories != null ? categories : new String[0];
        this.keywords = keywords != null ? keywords : new String[0];
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "UserPreference{" +
                "userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", categories=" + Arrays.toString(categories) +
                ", keywords=" + Arrays.toString(keywords) +
                '}';
    }
}