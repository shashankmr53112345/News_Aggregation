package model;

import java.sql.Timestamp;

public class NewsArticles {
	private String id;
	private String title;
	private String description;
	private String source;
	private String url;
	private String category;
	private String publishedAt;
	private int likes;
	private int dislikes;
	private int reportCount;
	private Timestamp insertedAt;

	public NewsArticles(String id, String title, String description, String source, String url, String category,
			String publishedAt, int likes, int dislikes, int reportCount, Timestamp insertedAt) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.source = source;
		this.url = url;
		this.category = category;
		this.publishedAt = publishedAt;
		this.likes = likes;
		this.dislikes = dislikes;
		this.reportCount = reportCount;
		this.insertedAt = insertedAt;
	}

	public NewsArticles(String id, String title, String description, String source, String url, String category,
			String publishedAt) {
		this(id, title, description, source, url, category, publishedAt, 0, 0, 0, null);
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getSource() {
		return source;
	}

	public String getUrl() {
		return url;
	}

	public String getCategory() {
		return category;
	}

	public String getPublishedAt() {
		return publishedAt;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getDislikes() {
		return dislikes;
	}

	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}

	public int getReportCount() {
		return reportCount;
	}

	public void setReportCount(int reportCount) {
		this.reportCount = reportCount;
	}

	public Timestamp getInsertedAt() {
		return insertedAt;
	}

	public void setInsertedAt(Timestamp insertedAt) {
		this.insertedAt = insertedAt;
	}
}