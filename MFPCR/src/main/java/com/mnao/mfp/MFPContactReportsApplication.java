package com.mnao.mfp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class MFPContactReportsApplication {

	public static void main(String[] args) {
		checkWarnEnv();
		SpringApplication.run(MFPContactReportsApplication.class, args);
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
