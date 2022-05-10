package com.mnao.mfp.user.interceptors;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.list.emp.AllEmployeesCache;
import com.mnao.mfp.sec.jwt.JwtTokenUtil;
import com.mnao.mfp.sec.service.MNAOSecurityService;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

@Configuration
@Component
public class MFPRequestInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(MFPRequestInterceptor.class);
	private static final String USERID_REQUEST_HEADER = "IV-USER";
	private static final String AUTH_HEADER = "Authorization";
	private static final String AUTH_REQUEST_URI = "Authorize";
	private final boolean useDBDomain;
	private JwtTokenUtil jwtTokenUtil = null;
	private AllEmployeesCache allEmployeesCache = null;
	private UserDetailsService uds = null;
	//
	public MFPRequestInterceptor() {
		String useDB = Utils.getAppProperty(AppConstants.EMP_USE_DB_RGN_ZONE_DSTR, "false");
		useDBDomain = useDB.equalsIgnoreCase("true");
		log.debug("Interceptor Created. Use DB for USer Domain: {}", useDBDomain);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		if (jwtTokenUtil == null || allEmployeesCache == null) {
			ServletContext servletContext = request.getServletContext();
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			jwtTokenUtil = wac.getBean(JwtTokenUtil.class);
			allEmployeesCache = wac.getBean(AllEmployeesCache.class);
			uds = wac.getBean(UserDetailsService.class);
		}
		boolean rv = true;
		final String requestURI = request.getRequestURI();
		System.out.println(request.getRequestURI());
		final String requestTokenHeader = request.getHeader(AUTH_HEADER);
		String userID = request.getHeader(USERID_REQUEST_HEADER);
		if( userID == null ) {
			userID = uds.getUserFromFile();
		}
		log.debug("UserID= {}", userID);
		if (userID == null || userID.trim().length() == 0) {
			response.sendError(401, "UNAUTHORISED");
			log.error("Unauthorised Access");
			rv = false;
		} else {
//			UserDetailsService ud = new UserDetailsService();
			MFPUser u = uds.getMFPUser(userID);
			if (u == null || jwtTokenUtil == null) {
				response.sendError(401, "UNAUTHORISED");
				log.error("Unauthorised Access");
				rv = false;
			} else {
				if (useDBDomain) {
					u.setUseDBDomain(true);
					try {
						if (allEmployeesCache != null) {
							allEmployeesCache.updateDomain(u);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				HttpSession session = request.getSession();
				session.setAttribute("mfpUser", u);
				/*
				 * 1. Authorization Request 
				 * 2. Invalid token
				 */
				if (requestURI.endsWith(AUTH_REQUEST_URI)) {
					rv = true;
				} else if (!validateJwtToken(requestTokenHeader, u) ) {
					response.sendError(401, "UNAUTHORISED");
					log.error("Unauthorised Access");
					rv = false;
				}
			}
		}

		return rv;
	}

	private boolean validateJwtToken(String requestTokenHeader, MFPUser u) {
	    String jwtToken = null;
	    // JWT Token is in the form "Bearer token". Remove Bearer word and get
	    // only the Token
	    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
	        jwtToken = requestTokenHeader.substring(7);
	    } else {
	    	log.warn("JWT Token does not begin with Bearer String");
	    	jwtToken = requestTokenHeader;
	    }
		return jwtTokenUtil.validateToken(jwtToken, u);
	}


}