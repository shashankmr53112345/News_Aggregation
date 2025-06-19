package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.ExternalAPIs;
import util.DatabaseConnection;

public class ExternalApisDao {

	public List<ExternalAPIs> getActiveApis() {
		List<ExternalAPIs> apis = new ArrayList<>();
		String sql = "SELECT id, name, api_url, api_key, is_active, last_accessed FROM external_apis WHERE is_active = 1";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				ExternalAPIs api = new ExternalAPIs(rs.getInt("id"), rs.getString("name"), rs.getString("api_key"),
						rs.getBoolean("is_active"), rs.getTimestamp("last_accessed"), rs.getString("api_url"));
				apis.add(api);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch active APIs", e);
		}
		return apis;
	}

	public void updateLastAccessed(int id, String name, Timestamp lastAccessed) {
		String sql = "UPDATE external_apis SET last_accessed = ? WHERE id = ? AND name = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setTimestamp(1, lastAccessed);
			stmt.setInt(2, id);
			stmt.setString(3, name);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update last accessed timestamp", e);
		}
	}
}