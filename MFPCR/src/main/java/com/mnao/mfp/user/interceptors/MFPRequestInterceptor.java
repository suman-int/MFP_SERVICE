package com.mnao.mfp.user.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.list.emp.AllEmployeesCache;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@Configuration
@Component
public class MFPRequestInterceptor implements HandlerInterceptor {
	//
	private static final Logger log = LoggerFactory.getLogger(MFPRequestInterceptor.class);
	private static final String USERID_REQUEST_HEADER = "IV-USER";
	private boolean useDBDomain = false;
	//
	@Autowired
	AllEmployeesCache allEmployeesCache;
	//
	public MFPRequestInterceptor() {
		String useDB = Utils.getAppProperty(AppConstants.EMP_USE_DB_RGN_ZONE_DSTR, "false");
		useDBDomain = useDB.equalsIgnoreCase("true");
		log.debug("Interceptor Created. Use DB for USer Domain:" + useDBDomain );
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		boolean rv = true;
		String userID = request.getHeader(USERID_REQUEST_HEADER);
		log.debug("UserID=" + userID);
		if (userID == null || userID.trim().length() == 0) {
			response.sendError(401, "UNAUTHORISED");
			log.error("Unauthorised Access");
			rv = false;
		} else {
			UserDetailsService ud = new UserDetailsService();
			MFPUser u = ud.getMFPUser(userID);
			if (u == null) {
				response.sendError(401, "UNAUTHORISED");
				log.error("Unauthorised Access");
				rv = false;
			} else {
				HttpSession session = request.getSession(true);
				if( useDBDomain && allEmployeesCache != null) {
					allEmployeesCache.updateDomain(u);
				}
				session.setAttribute("mfpUser", u);
			}
		}
		return rv;
	}


}