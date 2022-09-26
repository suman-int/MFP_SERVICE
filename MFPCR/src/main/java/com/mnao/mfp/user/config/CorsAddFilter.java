package com.mnao.mfp.user.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mnao.mfp.common.util.AppConstants;

@Configuration
public class CorsAddFilter extends OncePerRequestFilter {
	//
	private static final Logger log = LoggerFactory.getLogger(CorsAddFilter.class);
	//

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// CORS "pre-flight" request
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
		response.addHeader("Access-Control-Allow-Headers",
				"Content-type,X-Requested-With,Origin,accept,IV-USER,iv-user,Authorization,"
						+ String.format("%s,%s,%s,%s", AppConstants.RS_SEC_HDR_IV_NAME, AppConstants.RS_SEC_HDR_TOKEN_NAME,
								AppConstants.RS_SEC_HDR_VENDOR_ID,
				AppConstants.AUTH_HEADER));
		response.addHeader("x-access_token",
				"Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, IV-USER,"
						+ String.format("%s,%s,%s,%s", AppConstants.RS_SEC_HDR_IV_NAME, AppConstants.RS_SEC_HDR_TOKEN_NAME,
								AppConstants.RS_SEC_HDR_VENDOR_ID,
				AppConstants.AUTH_HEADER));
		response.addHeader("Access-Control-Max-Age", "1800");// 30 min
		log.debug("doFilterInternal...");
		//
		filterChain.doFilter(request, response);

	}
}