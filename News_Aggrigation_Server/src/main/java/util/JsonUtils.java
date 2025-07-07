package util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import model.ArticleReport;
import model.NewsArticles;
import model.Notification;

public class JsonUtils {
	public static JSONArray articlesToJsonArray(List<NewsArticles> articles) {
		JSONArray jsonArticles = new JSONArray();
		for (NewsArticles article : articles) {
			JSONObject json = new JSONObject();
			json.put("id", article.getId());
			json.put("title", article.getDescription());
			json.put("source", article.getSource());
			json.put("url", article.getUrl());
			json.put("category", article.getCategory());
			json.put("publishedAt", article.getPublishedAt());
			json.put("likes", article.getLikes());
			json.put("dislikes", article.getDislikes());
			json.put("reportCount", article.getReportCount());
			json.put("insertedAt", article.getInsertedAt() != null ? article.getInsertedAt().toString() : null);
			jsonArticles.put(json);
		}
		return jsonArticles;
	}

	public static JSONArray reportsToJsonArray(List<ArticleReport> reports) {
		JSONArray jsonReports = new JSONArray();
		for (ArticleReport report : reports) {
			JSONObject json = new JSONObject();
			json.put("username", report.getUsername());
			json.put("articleId", report.getArticleId());
			json.put("reason", report.getReason());
			jsonReports.put(json);
		}
		return jsonReports;
	}

	public static JSONArray notificationsToJsonArray(List<Notification> notifications) {
		JSONArray jsonNotifications = new JSONArray();
		for (Notification notification : notifications) {
			JSONObject json = new JSONObject();
			json.put("username", notification.getUsername());
			json.put("articleId", notification.getArticleId());
			json.put("message", notification.getMessage());
			json.put("createdAt", notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null);
			json.put("status", notification.getStatus());
			jsonNotifications.put(json);
		}
		return jsonNotifications;
	}
}
