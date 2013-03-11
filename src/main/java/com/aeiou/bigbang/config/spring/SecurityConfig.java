package com.aeiou.bigbang.config.spring;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.aeiou.bigbang.backend.security.BigAuthenticationFailureHandler;
import com.aeiou.bigbang.backend.security.BigAuthenticationProcessingFilter;
import com.aeiou.bigbang.backend.security.UserDetailsAuthenticationProvider;
import com.aeiou.bigbang.util.SpringApplicationContext;
import com.aeiou.bigbang.web.PersonalController;

@Configuration
public class SecurityConfig {

	@Inject
	AuthenticationManager authenticationManager;

	@Bean
	@Scope("singleton")
	AuthenticationProvider backendAuthenticationProvider() {
		return new UserDetailsAuthenticationProvider();
	}
	
	@Bean
	@Scope("singleton")
	LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
		return new LoginUrlAuthenticationEntryPoint("/login");
	}
	
	@Bean
	@Scope("singleton")
	AbstractAuthenticationProcessingFilter bigAuthenticationProcessingFilter() {
		assert (authenticationManager != null) : "AuthenticationManager should not be null";
		BigAuthenticationProcessingFilter filter = new BigAuthenticationProcessingFilter();
		filter.setAuthenticationManager(authenticationManager);
		filter.setAuthenticationFailureHandler(authenticationFailureHandler());
//		filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
		return filter;
	}
	
	@Bean
	@Scope("singleton")
	// We just need to set the default failure url here
	AuthenticationFailureHandler authenticationFailureHandler() {
		return new BigAuthenticationFailureHandler();
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
