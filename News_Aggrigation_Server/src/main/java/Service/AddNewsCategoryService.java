package Service;

import Exceptions.DuplicateCategoryException;
import Repository.AddNewsCategoryRepository;
import model.Category;

public class AddNewsCategoryService {
	private final AddNewsCategoryRepository NewsCategoryRepository;

	public AddNewsCategoryService(AddNewsCategoryRepository NewsCategoryRepository) {
		this.NewsCategoryRepository = NewsCategoryRepository;
	}

	public AddNewsCategoryService() {
		this.NewsCategoryRepository = new AddNewsCategoryRepository();
	}

	public boolean addCategory(String categoryName) {
		if (categoryName == null || categoryName.trim().isEmpty()) {
			throw new IllegalArgumentException("Category name cannot be null or empty");
		}
		Category category = new Category(categoryName);
		try {
			return NewsCategoryRepository.AddNewsCategory(category);
		} catch (Exception e) {
			// Check if exception indicates a duplicate name
			if (isDuplicateNameException(e)) {
				throw new DuplicateCategoryException("Category '" + categoryName + "' already exists");
			}
			throw new RuntimeException("Error adding category: " + e.getMessage(), e);
		}
	}

	private boolean isDuplicateNameException(Exception e) {
		return e.getMessage() != null && (e.getMessage().toLowerCase().contains("unique constraint")
				|| e.getMessage().toLowerCase().contains("duplicate"));
	}
}