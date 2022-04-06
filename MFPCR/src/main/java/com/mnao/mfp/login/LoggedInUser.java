package com.mnao.mfp.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;

@WebServlet(urlPatterns = "/login/*", loadOnStartup = 1)
public class LoggedInUser extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String USERID_REQUEST_HEADER = "IV-USER";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Login Servlet");
		String defUrl = "https://portaltest.mazdausa.com/m320/mfpwebui";
		String mfpUrl = Utils.getAppProperty(AppConstants.CR_URL_KEY);
		if (mfpUrl == null || mfpUrl.trim().length() == 0)
			mfpUrl = defUrl;
		String userID = req.getHeader(USERID_REQUEST_HEADER);
		String redUrl = req.getParameter("redirect");
		String dom = req.getParameter("domain");
		if( redUrl != null && redUrl.trim().length() > 0 )
			mfpUrl = redUrl;
		if( dom == null || dom.trim().length() == 0 )
			dom = "mazdausa.com";
		if (userID != null && userID.trim().length() > 0) {
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			printHTML(out, mfpUrl, userID);
			resp.setStatus(HttpServletResponse.SC_OK);
			Cookie userCookie = new Cookie("IV-USER", userID);
			userCookie.setMaxAge(3000);
			userCookie.setPath("/");
			userCookie.setSecure(false);
			userCookie.setDomain(dom);
			resp.addCookie(userCookie);
		} else {
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private void printHTML(PrintWriter out, String mfpUiUrl, String userID) {
		String url = mfpUiUrl;
		//url += "?" + userID;
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Contact Reports</title>");
		out.println("<script type=\"text/javascript\">");
		out.println("document.cookie = \"USER="+ userID + "; SameSite=None; path=/;\"");
		out.println("window.location=\"" + url + "\";");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
		out.println("</body>");
		out.println("</html>");
	}

}
