package task;

import java.util.List;

import Service.NewsService;
import model.NewsArticles;

public class NewsRefreshTask implements Runnable {
	private final NewsService newsService;

	public NewsRefreshTask(NewsService newsService) {
		this.newsService = newsService;
	}

	@Override
	public void run() {
		try {
			List<NewsArticles> newsApiArticles = newsService.fetchNewsFromNewsApi();
			newsService.saveArticlesToDatabase(newsApiArticles);
			List<NewsArticles> theNewsApiArticles = newsService.fetchNewsFromTheNewsApi();
			newsService.saveArticlesToDatabase(theNewsApiArticles);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}