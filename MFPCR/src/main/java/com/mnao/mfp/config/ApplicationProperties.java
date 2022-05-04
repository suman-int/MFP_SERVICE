package com.mnao.mfp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties("app")
@Getter
@Setter
@ToString
public class ApplicationProperties {

	private String name;
	private String email;
	private String appName;
}
