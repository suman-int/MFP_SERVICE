package com.mnao.mfp.user.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.apache.catalina.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.user.interceptors.MFPRequestInterceptor;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {
	//
	private static final Logger log = LoggerFactory.getLogger(MvcConfig.class);
	//
	@Autowired
	ApplicationContext mfpContext;
	//
	@Value("${spring.mvc.view.prefix}")
	private String viewPrefix;
	@Value("${spring.mvc.view.suffix}")
	private String viewSuffix;
	//
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
				AppConstants.RS_SEC_HDR_TOKEN_NAME, AppConstants.RS_SEC_HDR_VENDOR_ID, AppConstants.AUTH_HEADER));
		source.registerCorsConfiguration("/**", config);
		log.debug("new corsFilter() ...");
		return new CorsFilter(source);
	}
	
	  @Bean
	  public ViewResolver viewResolver() {
	    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
	    viewResolver.setViewClass(JstlView.class);
//	    viewResolver.setPrefix("/pages/");
//	    viewResolver.setSuffix(".jsp");
	    viewResolver.setPrefix(viewPrefix);
	    viewResolver.setSuffix(viewSuffix);
	    return viewResolver;
	  }
	  @ConditionalOnProperty(name="precompile.startup.jsp")
	  @Bean
	  public ServletContextInitializer preCompileJspsAtStartup() {
	      return servletContext -> {
	          getDeepResourcePaths(servletContext, viewPrefix).forEach(jspPath -> {
	              log.info("Registering JSP: {}", jspPath);
	              ServletRegistration.Dynamic reg = servletContext.addServlet(jspPath, Constants.JSP_SERVLET_CLASS);
	              reg.setInitParameter("jspFile", jspPath);
	              reg.setLoadOnStartup(99);
	              reg.addMapping(jspPath);
	          });
	      };
	  }

	  private static Stream<String> getDeepResourcePaths(ServletContext servletContext, String path) {
	      return (path.endsWith("/")) ? servletContext.getResourcePaths(path).stream().flatMap(p -> getDeepResourcePaths(servletContext, p))
	              : Stream.of(path);
	  }
}
