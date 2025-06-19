package model;

public class NewsArticles {
	private String articleId;
	private String title;
	private String description;
	private String source;
	private String url;
	private String category;
	private String publishDate;
	private String sourceid;

	public NewsArticles(String articleId, String title, String description, String source, String url, String category,
			String publishDate, String sourceid) {
		this.articleId = articleId;
		this.title = title;
		this.description = description;
		this.source = source;
		this.url = url;
		this.category = category;
		this.publishDate = publishDate;
		this.sourceid = sourceid;
	}

	public String getArticleId() {
		return articleId;
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

	public String getPublishDate() {
		return publishDate;
	}
}
