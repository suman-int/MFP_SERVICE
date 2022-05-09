package com.mnao.mfp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
public class MFPContactReportsApplication {

	public static void main(String[] args) {
		checkWarnEnv();
		SpringApplication.run(MFPContactReportsApplication.class, args);
	}

	private static void checkWarnEnv() {
		String prof = getActiveProfile();
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
					e.printStackTrace();
				}
			}
		}
		return rProf;
	}

}
