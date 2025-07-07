package Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.ExternalAPIsRepository;
import Repository.UserRepository.DuplicateKeyException;
import model.ExternalAPIs;

public class ExternalAPIsService {
	private static final Logger logger = LoggerFactory.getLogger(ExternalAPIsService.class);
	private final ExternalAPIsRepository repository;

	public ExternalAPIsService() {
		this.repository = new ExternalAPIsRepository();
	}

	public ExternalAPIsService(ExternalAPIsRepository repository) {
		this.repository = repository;
	}

	public List<ExternalAPIs> getAllApis() {
		logger.debug("Retrieving all external APIs");
		List<ExternalAPIs> apis = repository.getAllExternalAPIs();
		logger.info("Retrieved {} external APIs", apis.size());
		return apis;
	}

	public ExternalAPIs getApiDetails(int id) {
		logger.debug("Retrieving API details for ID: {}", id);
		ExternalAPIs api = repository.getExternalAPIById(id);
		if (api == null) {
			logger.warn("No API found for ID: {}", id);
		} else {
			logger.info("Retrieved API details for ID: {}", id);
		}
		return api;
	}

	public void updateApiKey(int id, String newApiKey) throws DuplicateKeyException {
		logger.debug("Updating API key for ID: {}", id);
		if (newApiKey == null || newApiKey.trim().isEmpty()) {
			logger.warn("Invalid API key for ID: {}", id);
			throw new IllegalArgumentException("API key cannot be null or empty");
		}
		boolean updated = false;
		try {
			updated = repository.updateExternalAPIKey(id, newApiKey);
		} catch (Repository.ExternalAPIsRepository.DuplicateKeyException e) {
			e.printStackTrace();
		}
		if (updated) {
			logger.info("API key updated successfully for ID: {}", id);
		} else {
			logger.warn("Failed to update API key for ID: {}", id);
			throw new RuntimeException("Failed to update API key due to database error");
		}
	}
}