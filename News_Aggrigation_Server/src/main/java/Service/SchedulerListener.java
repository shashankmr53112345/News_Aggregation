package Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SchedulerListener implements ServletContextListener {
	private ScheduledExecutorService scheduler;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleAtFixedRate(() -> {
			try {
				NewsStorageService newsStorageService = new NewsStorageService();
				newsStorageService.fetchAndStoreAllNews();

				NotificationProcessor notificationProcessor = new NotificationProcessor();
				notificationProcessor.processNotifications(newsStorageService.getLastFetchedArticles());
			} catch (Exception executionException) {
				System.err.println("Error in scheduled fetch or notification: " + executionException.getMessage());
				executionException.printStackTrace();
			}
		}, 0, 30, TimeUnit.MINUTES);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdownNow();
		}
	}
}