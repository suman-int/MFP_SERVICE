package com.mnao.mfp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;

import com.mnao.mfp.config.ApplicationProperties;
import com.mnao.mfp.config.CrConfiguration;

@ServletComponentScan
@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class MFPContactReportsApplication {

	 private static Logger logger = LoggerFactory.getLogger(MFPContactReportsApplication.class);


	@Autowired
	private ApplicationProperties properties;

	@Autowired
	private CrConfiguration configuration;

	public static void main(String... args) throws Exception {
		checkWarnEnv();
		SpringApplication.run(MFPContactReportsApplication.class, args);
	}

	@PostConstruct
	private void init() {
		logger.info("{} - active profile: {}", properties.getAppName(), configuration.getName());
		logger.info(properties.toString());

	}

	private static void checkWarnEnv() {
		String prof = System.getProperty("spring.profiles.active", "");
		if (prof.length() == 0 || (!prof.equalsIgnoreCase("dev"))) {
			try {
				InetAddress id = InetAddress.getLocalHost();
				String hName = id.getHostName();
				System.out.println(id.getHostName());
				if (hName.startsWith("VDIX-DEV")) {
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
