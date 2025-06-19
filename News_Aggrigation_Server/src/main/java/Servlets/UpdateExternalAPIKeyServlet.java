package Servlets;

import java.io.IOException;

import org.json.JSONObject;

import Service.ManageExternalAPIsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonResponseBody;

@WebServlet("/UpdateExternalAPIKey")
public class UpdateExternalAPIKeyServlet extends HttpServlet {
	private final ManageExternalAPIsService externalapiservice;

	public UpdateExternalAPIKeyServlet(ManageExternalAPIsService externalapiservice) {
		this.externalapiservice = externalapiservice;
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		JsonResponseBody responsebody;
		try {
			String idParam = request.getParameter("id");
			String newApiKey = request.getParameter("apiKey");
			if (idParam == null || idParam.trim().isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responsebody = new JsonResponseBody(false, "ID parameter is required");
			} else if (newApiKey == null || newApiKey.trim().isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responsebody = new JsonResponseBody(false, "API key is required");
			} else {
				int id = Integer.parseInt(idParam);
				boolean updated = externalapiservice.updateApiKey(id, newApiKey);
				JSONObject data = new JSONObject();
				data.put("updated", updated);
				responsebody = new JsonResponseBody(updated, updated ? "Success" : "Failed to update API key", data);
			}
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responsebody = new JsonResponseBody(false, "Invalid ID format");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			responsebody = new JsonResponseBody(false, "Error processing request");
		}
		response.getWriter().write(responsebody.toJson());
	}

}
