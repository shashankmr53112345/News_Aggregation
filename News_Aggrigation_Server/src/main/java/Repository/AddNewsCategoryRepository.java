package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Category;
import util.DatabaseConnection;

public class AddNewsCategoryRepository {
	public boolean AddNewsCategory(Category category) {
		String sql = "INSERT INTO categories (name) VALUES (?)";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, category.getName());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}