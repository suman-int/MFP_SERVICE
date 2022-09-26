package com.mnao.mfp.user.interceptors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.list.cache.AllActiveEmployeesCache;
import com.mnao.mfp.sec.jwt.JwtTokenUtil;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@Configuration
@Component
public class MFPRequestInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(MFPRequestInterceptor.class);
	private static final String USERID_REQUEST_HEADER = "IV-USER";
	private static final String AUTH_REQUEST_URI = "/Auth/";
	private static final String LANDING_PAGE_URI = "/LandingPage/";
	private final boolean useDBDomain;
	private final boolean useSecJWT;
	private final boolean useSecJWTCookie;
	private JwtTokenUtil jwtTokenUtil = null;
	private AllActiveEmployeesCache allEmployeesCache = null;
	private UserDetailsService uds = null;

	//
	public MFPRequestInterceptor() {
		String propStr = Utils.getAppProperty(AppConstants.EMP_USE_DB_RGN_ZONE_DSTR, "false");
		useDBDomain = propStr.equalsIgnoreCase("true");
		log.debug("Interceptor Created. Use DB for USer Domain: {}", useDBDomain);
		propStr = Utils.getAppProperty(AppConstants.USE_JWT_TOKEN_AUTH, "true");
		useSecJWT = propStr.equalsIgnoreCase("true");
		propStr = Utils.getAppProperty(AppConstants.USE_JWT_TOKEN_AUTH_COOKIE, "true");
		useSecJWTCookie = propStr.equalsIgnoreCase("true");
		log.debug("Using JWT Token for authentication: {} use cookie: {}", useSecJWT, useSecJWTCookie);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		if (useSecJWT)
			return preHandleJWT(request, response, arg2);
		else
			return preHandlePlain(request, response, arg2);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		log.info("COMPLETED REQUEST: " + request.getRequestURI());
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

	public boolean preHandlePlain(HttpServletRequest request, HttpServletResponse response, Object arg2)
			throws Exception {
		boolean rv = true;
		final String requestURI = request.getRequestURI();
		log.debug("URI: " + request.getRequestURI());
		log.debug("USERID_REQUEST_HEADER: " + request.getHeader(USERID_REQUEST_HEADER));
		if (allEmployeesCache == null) {
			ServletContext servletContext = request.getServletContext();
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			allEmployeesCache = wac.getBean(AllActiveEmployeesCache.class);
			uds = wac.getBean(UserDetailsService.class);
		}
		if (requestURI.equals("/error")) {
			return false;
		}
		String userID = request.getHeader(USERID_REQUEST_HEADER);
		if (userID == null) {
			userID = uds.getUserFromFile();
		}
		log.debug("UserID= {}", userID);
		if (userID == null || userID.trim().length() == 0) {
			response.sendError(401, "UNAUTHORISED");
			log.error("Unauthorised Access");
			rv = false;
		} else {
			MFPUser u = uds.getMFPUser(userID);
			if (u == null) {
				response.sendError(403, "FORBIDDEN");
				log.error("Unauthorised (Forbidden) Access");
				rv = false;
			} else {
				if (useDBDomain) {
					u.setUseDBDomain(true);
					try {
						if (allEmployeesCache != null) {
							allEmployeesCache.updateDomain(u);
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
				HttpSession session = request.getSession();
				session.setAttribute("mfpUser", u);
			}
		}
		if (rv) {
			log.info("RETURNING TRUE");
		} else {
			log.info("RETURNING FALSE");
		}
		return rv;
	}

	public boolean preHandleJWT(HttpServletRequest request, HttpServletResponse response, Object arg2)
			throws Exception {
		if (jwtTokenUtil == null || allEmployeesCache == null) {
			ServletContext servletContext = request.getServletContext();
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			jwtTokenUtil = wac.getBean(JwtTokenUtil.class);
			allEmployeesCache = wac.getBean(AllActiveEmployeesCache.class);
			uds = wac.getBean(UserDetailsService.class);
		}
		boolean rv = true;
		final String requestURI = request.getRequestURI();
		log.debug("URI: " + request.getRequestURI());
		log.debug("AUTH_HEADER: " + request.getHeader(AppConstants.AUTH_HEADER));
//		String tokInCookie = null;
//		if (useSecJWTCookie) {
//			Cookie[] cks = request.getCookies();
//			if (cks != null) {
//				for (Cookie ck : cks) {
//					if (AppConstants.AUTH_COOKIE.equalsIgnoreCase(ck.getName())) {
//						tokInCookie = ck.getValue();
//						break;
//					}
//				}
//			}
//			log.debug("Cookie MFPUSRTOK: " + tokInCookie);
//		}
		log.debug("USERID_REQUEST_HEADER: " + request.getHeader(USERID_REQUEST_HEADER));
		if (requestURI.equals("/error")) {
			return false;
		}
		final String requestTokenHeader = request.getHeader(AppConstants.AUTH_HEADER);
		String userID = request.getHeader(USERID_REQUEST_HEADER);
		if (userID == null) {
			userID = uds.getUserFromFile();
		}
		log.debug("UserID= {}", userID);
		if (userID == null || userID.trim().length() == 0) {
			response.sendError(401, "UNAUTHORISED");
			log.error("Unauthorised Access - 401");
			rv = false;
		} else {
			MFPUser u = uds.getMFPUser(userID);
			if (u == null || jwtTokenUtil == null) {
				response.sendError(403, "FORBIDDEN");
				log.error("Unauthorised (Forbidden) Access");
				rv = false;
			} else {
				if (useDBDomain) {
					u.setUseDBDomain(true);
					try {
						if (allEmployeesCache != null) {
							allEmployeesCache.updateDomain(u);
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
				HttpSession session = request.getSession();
				session.setAttribute("mfpUser", u);
				/*
				 * 1. Authorization Request 2. Invalid token
				 */
				if (requestURI.startsWith(AUTH_REQUEST_URI) || requestURI.startsWith(LANDING_PAGE_URI)) {
					rv = true;
					log.info("Forwarding to: " + requestURI);
				} 
				else if ((requestTokenHeader != null) && (!validateJwtToken(requestTokenHeader, u))) {
					response.sendError(408, "REQUEST TIMEOUT");
					log.error("Unauthorised Access - 408");
					rv = false;
				} else {
					rv = true;
					log.info("Forwarding to: " + requestURI);
				}
			}
		}
		if (rv) {
			log.info("RETURNING TRUE");
		} else {
			log.info("RETURNING FALSE");
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
		boolean rv = false;
		if (jwtToken != null) {
			try {
				rv = jwtTokenUtil.validateToken(jwtToken, u);
			} catch (Exception e) {
				log.error("Error parsing JWTToken: " + e.getMessage());
			}
		}
		return rv;
	}

}