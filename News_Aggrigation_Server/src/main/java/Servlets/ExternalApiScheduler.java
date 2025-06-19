package Servlets;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import Service.ApiService;

@WebListener
public class ExternalApiScheduler implements ServletContextListener {
	private Timer timer;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new ApiFetchTask(), 0, 3 * 60 * 60 * 1000); // Every 3 hours
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (timer != null) {
			timer.cancel();
		}
	}

	private static class ApiFetchTask extends TimerTask {
		private final ApiService apiService = new ApiService();

		@Override
		public void run() {
			try {
				apiService.fetchAndProcessApiData();
			} catch (Exception e) {
				System.err.println("Error in API fetch task: " + e.getMessage());
			}
		}
	}
}