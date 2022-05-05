package com.mnao.contactreport;

import com.mnao.contactreport.config.CrConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class MFPContactReportsApplication {

    private static Logger logger = LoggerFactory.getLogger(MFPContactReportsApplication.class);

    @Autowired
    private CrConfig crConfig;

    public static void main(String[] args) {
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

}
