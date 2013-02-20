package com.aeiou.bigbang.config.spring;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;

import com.aeiou.bigbang.backend.security.UserDetailsAuthenticationProvider;
import com.aeiou.bigbang.util.SpringApplicationContext;
import com.aeiou.bigbang.web.PersonalController;

@Configuration
public class SecurityConfig {

	@Bean
	@Scope("singleton")
	AuthenticationProvider backendAuthenticationProvider() {
		return new UserDetailsAuthenticationProvider();
	}
	
	@Bean(autowire = Autowire.BY_NAME)
	@Scope("singleton")
	SpringApplicationContext springApplicationContext() {
		return new SpringApplicationContext();
	}
	
	@Bean(autowire = Autowire.BY_NAME)
	@Scope("singleton")
	PersonalController personalController() {
		return new PersonalController();
	}
}
