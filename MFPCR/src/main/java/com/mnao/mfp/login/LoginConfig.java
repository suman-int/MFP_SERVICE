package com.mnao.mfp.login;

import javax.servlet.http.HttpServlet;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class LoginConfig {
//	   @Bean    
	   public ServletRegistrationBean<HttpServlet> loginServlet() {
	       ServletRegistrationBean<HttpServlet> servRegBean = new ServletRegistrationBean<>();
	       servRegBean.setServlet(new LoggedInUser());
	       servRegBean.addUrlMappings("/login");
	       servRegBean.setLoadOnStartup(1);
	       return servRegBean;
	   }
}
