package Service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.NewsArticleRepository;
import Repository.NewsArticleRepository.NotFoundException;
import model.ArticleReport;
import model.NewsArticles;

public class ArticleService {
	private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");
	private final NewsArticleRepository repository;

	public ArticleService() {
		this.repository = new NewsArticleRepository();
	}

	public ArticleService(NewsArticleRepository repository) {
		this.repository = repository;
	}

	public List<NewsArticles> getHeadlines(String startDate, String endDate, String category) throws SQLException {
		logger.debug("Fetching headlines: startDate={}, endDate={}, category={}", startDate, endDate, category);
		if (category != null && !category.isEmpty() && !VALID_CATEGORIES.contains(category)) {
			logger.warn("Invalid category: {}", category);
			throw new IllegalArgumentException("Invalid category: " + category);
		}
		List<NewsArticles> articles = repository.getHeadlines(startDate, endDate, category);
		logger.info("Fetched {} headlines", articles.size());
		return articles;
	}

	public void saveUserArticle(String username, String articleId)
			throws SQLException, NewsArticleRepository.NotFoundException {
		logger.debug("Saving article: {} for username: {}", articleId, username);
		if (!repository.articleExists(articleId)) {
			logger.warn("Article not found: {}", articleId);
			throw new NewsArticleRepository.NotFoundException("Article not found");
		}
		repository.saveArticle(username, articleId);
		logger.info("Article {} saved for username: {}", articleId, username);
	}

	public List<NewsArticles> getSavedArticles(String username, String category) throws SQLException {
		logger.debug("Fetching saved articles for username: {}, category: {}", username, category);
		if (category != null && !category.isEmpty() && !VALID_CATEGORIES.contains(category)) {
			logger.warn("Invalid category: {}", category);
			throw new IllegalArgumentException("Invalid category: " + category);
		}
		List<NewsArticles> articles = repository.getSavedArticles(username, category);
		logger.info("Fetched {} saved articles for username: {}", articles.size(), username);
		return articles;
	}

	public void deleteUserArticle(String username, String articleId) throws SQLException, NotFoundException {
		logger.debug("Deleting article: {} for username: {}", articleId, username);
		repository.deleteArticle(username, articleId);
		logger.info("Article {} deleted for username: {}", articleId, username);
	}

	public void likeArticle(String username, String articleId)
			throws SQLException, NewsArticleRepository.NotFoundException {
		logger.debug("Liking article: {} for username: {}", articleId, username);
		if (!repository.articleExists(articleId)) {
			logger.warn("Article not found: {}", articleId);
			throw new NewsArticleRepository.NotFoundException("Article not found");
		}
		repository.likeArticle(username, articleId);
		logger.info("Article {} liked by username: {}", articleId, username);
	}

	public void dislikeArticle(String username, String articleId)
			throws SQLException, NewsArticleRepository.NotFoundException {
		logger.debug("Disliking article: {} for username: {}", articleId, username);
		if (!repository.articleExists(articleId)) {
			logger.warn("Article not found: {}", articleId);
			throw new NewsArticleRepository.NotFoundException("Article not found");
		}
		repository.dislikeArticle(username, articleId);
		logger.info("Article {} disliked by username: {}", articleId, username);
	}

	public void reportArticle(String username, String articleId, String reason)
			throws SQLException, NewsArticleRepository.NotFoundException {
		logger.debug("Reporting article: {} by username: {} with reason: {}", articleId, username, reason);
		if (!repository.articleExists(articleId)) {
			logger.warn("Article not found: {}", articleId);
			throw new NewsArticleRepository.NotFoundException("Article not found");
		}
		if (reason == null || reason.trim().isEmpty()) {
			logger.warn("Report reason missing for article: {} by username: {}", articleId, username);
			throw new IllegalArgumentException("Report reason is required");
		}
		repository.reportArticle(username, articleId, reason);
		logger.info("Article {} reported by username: {}", articleId, username);
	}

	public List<NewsArticles> searchArticles(String query, String startDate, String endDate, String sortBy)
			throws SQLException {
		logger.debug("Searching articles: query={}, startDate={}, endDate={}, sortBy={}", query, startDate, endDate,
				sortBy);
		if (query == null || query.trim().isEmpty()) {
			logger.warn("Query parameter is empty");
			throw new IllegalArgumentException("Query parameter is required");
		}
		List<NewsArticles> articles = repository.searchArticles(query, startDate, endDate, sortBy);
		logger.info("Found {} articles for query: {}", articles.size(), query);
		return articles;
	}

	public List<ArticleReport> getArticleReports() throws SQLException {
		logger.debug("Fetching article reports");
		List<ArticleReport> reports = repository.getArticleReports();
		logger.info("Fetched {} article reports", reports.size());
		return reports;
	}

	public void hideArticleById(String articleId) throws SQLException, NewsArticleRepository.NotFoundException {
		logger.debug("Hiding article by ID: {}", articleId);
		if (!repository.articleExists(articleId)) {
			logger.warn("Article not found: {}", articleId);
			throw new NewsArticleRepository.NotFoundException("Article not found");
		}
		int rowsAffected = repository.hideArticleById(articleId);
		if (rowsAffected > 0) {
			logger.info("Article {} hidden successfully", articleId);
		} else {
			logger.warn("No articles hidden for ID: {}", articleId);
			throw new NewsArticleRepository.NotFoundException("Article not found or already hidden");
		}
	}

	public void hideArticlesByKeyword(String keyword) throws SQLException {
		logger.debug("Hiding articles by keyword: {}", keyword);
		if (keyword == null || keyword.trim().isEmpty()) {
			logger.warn("Keyword is empty");
			throw new IllegalArgumentException("Keyword is required");
		}
		if (keyword.length() > 255) {
			logger.warn("Keyword too long: {}", keyword);
			throw new IllegalArgumentException("Keyword must be 255 characters or less");
		}
		int rowsAffected = repository.hideArticlesByKeyword(keyword);
		logger.info("Hide {} articles for keyword: {}", rowsAffected, keyword);
	}

	public void hideArticlesByCategory(String category) throws SQLException {
		logger.debug("Hiding articles by category: {}", category);
		if (category == null || category.trim().isEmpty()) {
			logger.warn("Category is empty");
			throw new IllegalArgumentException("Category is required");
		}
		if (!VALID_CATEGORIES.contains(category)) {
			logger.warn("Invalid category: {}", category);
			throw new IllegalArgumentException("Invalid category: " + category);
		}
		int rowsAffected = repository.hideArticlesByCategory(category);
		logger.info("Hid {} articles for category: {}", rowsAffected, category);
	}
}