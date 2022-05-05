package com.mnao.mfp;

import com.mnao.mfp.config.CrConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ServletComponentScan
@SpringBootApplication
public class MFPContactReportsApplication {

	private static Logger logger = LoggerFactory.getLogger(MFPContactReportsApplication.class);

	@Autowired
	private CrConfig crConfig;

	public static void main(String[] args) {
		checkWarnEnv();
		ApplicationContext applicationContext = SpringApplication.run(MFPContactReportsApplication.class, args);
		for (String name : applicationContext.getBeanDefinitionNames()) {
			logger.debug("{}", name);
		}
	}

	@PostConstruct
	private void init() {
		logger.info("{} - active profile: {}", crConfig.getName(), crConfig.getProfile());
	}

	@Profile({"dev", "default"})
	@Bean
	public String devBean() {
		return "dev";
	}

	@Profile("test")
	@Bean
	public String testBean() {
		return "test";
	}

	@Profile("qa")
	@Bean
	public String qaBean() {
		return "qa";
	}

	@Profile("prod")
	@Bean
	public String prodBean() {
		return "prod";
	}

	private static void checkWarnEnv() {
		String prof = System.getProperty("spring.profiles.active", "");
		if (prof.length() == 0 || (!prof.equalsIgnoreCase("dev")) ) {
		 try {  
		      InetAddress id = InetAddress.getLocalHost(); 
		      String hName = id.getHostName();
		      System.out.println( id.getHostName()); 
		      if( hName.startsWith("VDIX-DEV")) {
		    	  System.out.println("***** WARNING *****");
		    	  System.out.println("*Profile " + prof + " is set in " + hName);
		    	  System.out.println("*ALL EMAILS WILL GO TO ORIGINAL RECEPIENTS");
		    	  System.out.println("*******************");
		      }
		    } catch (UnknownHostException e) {  
		    	e.printStackTrace();
		    }  
		}
	}

}
