package com.mnao.mfp;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
public class MFPContactReportsApplication extends SpringBootServletInitializer {
	//
	private static final Logger log = LoggerFactory.getLogger(MFPContactReportsApplication.class);
	//
	public static void main(String[] args) {
		checkWarnEnv();
		SpringApplication.run(MFPContactReportsApplication.class, args);
	}
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MFPContactReportsApplication.class);
    }


	private static void checkWarnEnv() {
		String prof = getActiveProfile();
		if (prof.length() == 0 || (!prof.equalsIgnoreCase("dev")) ) {
		 try {  
		      InetAddress id = InetAddress.getLocalHost(); 
		      String hName = id.getHostName();
		      log.debug( id.getHostName()); 
		      if( hName.startsWith("VDIX-DEV")) {
		    	  log.debug("***** WARNING *****");
		    	  log.debug("*Profile " + prof + " is set in " + hName);
		    	  log.debug("*ALL EMAILS WILL GO TO ORIGINAL RECEPIENTS");
		    	  log.debug("*******************");
		      }
		    } catch (UnknownHostException e) {  
		    	log.error("", e);
		    }  
		}
	}

	private static String getActiveProfile() {
		String rProf = System.getProperty("spring.profiles.active");
		if( rProf == null ) {
			String addProfName = System.getProperty("spring.config.additional-location"); 
			if( addProfName != null ) {
				Properties prop = new Properties();
				try (FileInputStream fis = new FileInputStream(addProfName)) {
					prop.load(fis);
					rProf = prop.getProperty("spring.profiles.active");
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		return rProf;
	}

}
