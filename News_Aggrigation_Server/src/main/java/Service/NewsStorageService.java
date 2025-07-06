package Service;

import java.util.ArrayList;
import java.util.List;

import Factory.NewsFetcherFactory;
import Repository.NewsArticleFetcherRepository;
import model.NewsArticles;

public class NewsStorageService {
	private final NewsFetcherFactory newsFetcherFactory;
	private final NewsArticleFetcherRepository newsArticleFetcherRepository;
	private final List<NewsArticles> lastFetchedArticles = new ArrayList<>();

	public NewsStorageService() {
		this.newsFetcherFactory = new NewsFetcherFactory();
		this.newsArticleFetcherRepository = new NewsArticleFetcherRepository();
	}

	public void fetchAndStoreAllNews() {
		lastFetchedArticles.clear();
		List<ApiNewsFetcher> newsFetchers = newsFetcherFactory.getAllFetchers();
		for (ApiNewsFetcher fetcher : newsFetchers) {
			try {
				List<NewsArticles> articles = fetcher.fetchNewsArticles();
				System.out.println("Fetched " + articles.size() + " articles from " + fetcher.getApiName());
				newsArticleFetcherRepository.saveArticlesToDatabase(articles);
				System.out.println(
						"Stored " + articles.size() + " articles from " + fetcher.getApiName() + " in database");
				lastFetchedArticles.addAll(articles);
			} catch (Exception fetchException) {
				System.err.println(
						"Error fetching/storing from " + fetcher.getApiName() + ": " + fetchException.getMessage());
				fetchException.printStackTrace();
			}
		}
	}

	public List<NewsArticles> getLastFetchedArticles() {
		return new ArrayList<>(lastFetchedArticles);
	}
}