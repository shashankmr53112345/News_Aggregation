package model;

public class ArticleReport {
	private String username;
	private String articleId;
	private String reason;

	public ArticleReport(String username, String articleId, String reason) {
		this.username = username;
		this.articleId = articleId;
		this.reason = reason;
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

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
