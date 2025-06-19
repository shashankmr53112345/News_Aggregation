package Servlets;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Service.ManageExternalAPIsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ExternalAPIs;
import util.JsonResponseBody;

@WebServlet("/ViewExternalAPIDetails")
public class ViewExternalAPIDetailsServlet extends HttpServlet {

	private final ManageExternalAPIsService externalapiservice;

	public ViewExternalAPIDetailsServlet() {
		this.externalapiservice = new ManageExternalAPIsService();
	}

	public ViewExternalAPIDetailsServlet(ManageExternalAPIsService externalapiservice) {
		this.externalapiservice = externalapiservice;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		JsonResponseBody responsebody;
		try {
			List<ExternalAPIs> apis = externalapiservice.getAllApis();
			if (apis != null && !apis.isEmpty()) {
				JSONArray apisJson = new JSONArray();
				for (ExternalAPIs api : apis) {
					apisJson.put(new JSONObject(api)); // Convert each ExternalApi to JSONObject
				}
				responsebody = new JsonResponseBody(true, "Success", new JSONObject().put("apis", apisJson));
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				responsebody = new JsonResponseBody(false, "No APIs found");
			}
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responsebody = new JsonResponseBody(false, "Invalid ID format");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			responsebody = new JsonResponseBody(false, "Error serializing API data");
		}
		response.getWriter().write(responsebody.toJson());
	}

}
