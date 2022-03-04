package com.mnao.mfp.user.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class CorsAddFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// CORS "pre-flight" request
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
		response.addHeader("Access-Control-Allow-Headers",
				"Content-type,X-Requested-With,Origin,accept,IV-USER,iv-user");
		response.addHeader("x-access_token",
				"Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, IV-USER");
		response.addHeader("Access-Control-Max-Age", "1800");// 30 min
		System.out.println("doFilterInternal...");
		//
		filterChain.doFilter(request, response);

	}
}