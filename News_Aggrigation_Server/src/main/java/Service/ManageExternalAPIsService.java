package Service;

import java.util.List;

import Repository.ManageExternalAPIsRepository;
import model.ExternalAPIs;

public class ManageExternalAPIsService {
	private final ManageExternalAPIsRepository repository = new ManageExternalAPIsRepository();

	public List<ExternalAPIs> getAllApis() {
		return repository.getAllExternalAPIs();
	}

	public ExternalAPIs getApiDetails(int id) {
		return repository.getExternalAPIById(id);
	}

	public boolean updateApiKey(int id, String newApiKey) {
		return repository.updateExternalAPIKey(id, newApiKey);
	}
}
