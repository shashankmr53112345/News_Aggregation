package Servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/HiServlet")
public class HiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public HiServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Map<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("message", "Hi! This is a JSON response.");
		jsonResponse.put("status", "success");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		objectMapper.writeValue(response.getWriter(), jsonResponse);
	}

}
