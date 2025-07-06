package Service;

import java.sql.SQLException;
import java.util.List;

import Repository.NewsArticlesOperationsRepository;
import model.NewsArticles;

public class NewsService {
	private final NewsArticlesOperationsRepository repository;

	public NewsService() {
		this.repository = new NewsArticlesOperationsRepository();
	}

	public List<NewsArticles> getHeadlines(String startDate, String endDate, String category) throws SQLException {
		return repository.getHeadlines(startDate, endDate, category);
	}

	public void saveUserArticle(String username, String articleId) throws SQLException {
		if (!repository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		repository.saveArticle(username, articleId);
	}

	public List<NewsArticles> getSavedArticles(String username, String category) throws SQLException {
		return repository.getSavedArticles(username, category);
	}

	public void deleteUserArticle(String username, String articleId) throws SQLException {
		repository.deleteArticle(username, articleId);
	}

	public void likeArticle(String username, String articleId) throws SQLException {
		if (!repository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		repository.likeArticle(username, articleId);
	}

	public void dislikeArticle(String username, String articleId) throws SQLException {
		if (!repository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		repository.dislikeArticle(username, articleId);
	}

	public void reportArticle(String username, String articleId, String reason) throws SQLException {
		if (!repository.articleExists(articleId)) {
			throw new SQLException("Article not found");
		}
		if (reason == null || reason.trim().isEmpty()) {
			throw new SQLException("Report reason is required");
		}
		repository.reportArticle(username, articleId, reason);
	}

	public List<NewsArticles> searchArticles(String query, String startDate, String endDate, String sortBy)
			throws SQLException {
		return repository.searchArticles(query, startDate, endDate, sortBy);
	}
}