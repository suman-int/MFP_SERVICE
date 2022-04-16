package com.mnao.mfp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class MFPContactReportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MFPContactReportsApplication.class, args);
    }

}
