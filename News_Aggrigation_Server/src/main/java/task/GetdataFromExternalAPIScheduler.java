package task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import Service.NewsService;

@WebListener
public class GetdataFromExternalAPIScheduler implements ServletContextListener {
	private ScheduledExecutorService scheduler;
	private NewsService newsService;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		newsService = new NewsService();
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new NewsRefreshTask(newsService), 0, 4, TimeUnit.HOURS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdown();
		}
	}
}
