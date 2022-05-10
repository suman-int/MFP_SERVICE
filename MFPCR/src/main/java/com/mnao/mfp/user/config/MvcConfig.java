package com.mnao.mfp.user.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.user.interceptors.MFPRequestInterceptor;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {
	@Autowired
	ApplicationContext mfpContext;

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(new MFPRequestInterceptor());
	}

	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		// Don't do this in production, use a proper list of allowed origins
		config.setAllowedOrigins(Collections.singletonList("*"));
		config.setAllowedHeaders(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH", "Origin",
				"Content-Type", "Accept", "IV-USER", "Authorization", AppConstants.RS_SEC_HDR_IV_NAME,
				AppConstants.RS_SEC_HDR_TOKEN_NAME, AppConstants.RS_SEC_HDR_VENDOR_ID));
		source.registerCorsConfiguration("/**", config);
		System.out.println("new corsFilter() ...");
		return new CorsFilter(source);
	}
}
