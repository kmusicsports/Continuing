package com.example.continuing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.continuing.zoom.ZoomApiIntegration;

@Configuration
public class MainConfig {

	@Bean
	public ZoomApiIntegration zoomApiIntegration() {
		return new ZoomApiIntegration();
	}
}
