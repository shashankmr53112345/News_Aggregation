package Factory;

import java.util.Arrays;
import java.util.List;

import Service.ApiNewsFetcher;
import Service.NewsApiFetcherImpl;
import Service.TheNewsApiFetcherImpl;

public class NewsFetcherFactory {
	private static final List<String> SUPPORTED_APIS = Arrays.asList("News API", "The News API");

	public List<ApiNewsFetcher> getAllFetchers() {
		List<ApiNewsFetcher> fetchers = Arrays.asList(new NewsApiFetcherImpl(), new TheNewsApiFetcherImpl());
		return fetchers;
	}

	public ApiNewsFetcher getFetcher(String apiName) {
		if (!SUPPORTED_APIS.contains(apiName)) {
			throw new IllegalArgumentException("Unsupported API: " + apiName);
		}
		switch (apiName) {
		case "News API":
			return new NewsApiFetcherImpl();
		case "The News API":
			return new TheNewsApiFetcherImpl();
		default:
			throw new IllegalArgumentException("Unknown API: " + apiName);
		}
	}
}
