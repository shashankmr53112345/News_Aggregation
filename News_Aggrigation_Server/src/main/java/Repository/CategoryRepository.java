package Repository;

import model.Category;
import util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CategoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(CategoryRepository.class);

    public static class DuplicateKeyException extends Exception {
        public DuplicateKeyException(String message) {
            super(message);
        }
    }

    public boolean addCategory(Category category) throws DuplicateKeyException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Category insertion attempt for: {}, rows affected: {}", category.getName(), rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (isDuplicateKeyError(e)) {
                String message = "Category '" + category.getName() + "' already exists";
                logger.warn("Duplicate key error: {}", message);
                throw new DuplicateKeyException(message);
            }
            logger.error("Error adding category: {}", category.getName(), e);
            throw new RuntimeException("Database error while adding category", e);
        }
    }

    private boolean isDuplicateKeyError(SQLException e) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();
        return sqlState != null && sqlState.equals("23505") || errorCode == 1062;
    }
}