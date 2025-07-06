package Service;

import java.util.List;

import model.NewsArticles;

public interface ApiNewsFetcher {
	List<NewsArticles> fetchNewsArticles() throws Exception;

	String getApiName();
}