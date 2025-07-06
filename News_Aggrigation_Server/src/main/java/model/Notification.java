package model;

import java.sql.Timestamp;

public class Notification {
	private String id;
	private String username;
	private String articleId;
	private String message;
	private Timestamp createdAt;
	private String status;

	public Notification() {
	}

	public Notification(String id, String username, String articleId, String message, Timestamp createdAt,
			String status) {
		this.id = id;
		this.username = username;
		this.articleId = articleId;
		this.message = message;
		this.createdAt = createdAt;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
