package Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.CategoryRepository;
import Repository.UserRepository.DuplicateKeyException;
import model.Category;

public class CategoryService {
	private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
	private final CategoryRepository categoryRepository;

	public CategoryService() {
		this.categoryRepository = new CategoryRepository();
	}

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public void addCategory(String categoryName) throws DuplicateKeyException {
		logger.debug("Attempting to add category: {}", categoryName);
		if (categoryName == null || categoryName.trim().isEmpty()) {
			logger.warn("Invalid category name: {}", categoryName);
			throw new IllegalArgumentException("Category name cannot be null or empty");
		}
		Category category = new Category(categoryName);
		boolean added = false;
		try {
			added = categoryRepository.addCategory(category);
		} catch (Repository.CategoryRepository.DuplicateKeyException e) {
			e.printStackTrace();
		}
		if (added) {
			logger.info("Category added successfully: {}", categoryName);
		} else {
			logger.warn("Failed to add category: {}", categoryName);
			throw new RuntimeException("Failed to add category due to database error");
		}
	}
}