package fr.eni.enchere.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import fr.eni.enchere.bll.CategoryManager;
import fr.eni.enchere.bo.Category;
import fr.eni.enchere.exceptions.BusinessException;

/**
 * Servlet implementation class ServletNewArticle
 */
@WebServlet("/ServletNewArticle")
public class ServletNewArticle extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServletNewArticle() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CategoryManager categoryManager = new CategoryManager();
		List<Category> categoryList = null;
		
		try {
			
			categoryList = categoryManager.selectAll();
			request.setAttribute("categoryList", categoryList);
			
			request.getRequestDispatcher("/WEB-INF/jsp/newArticlePage.jsp").forward(request, response);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
