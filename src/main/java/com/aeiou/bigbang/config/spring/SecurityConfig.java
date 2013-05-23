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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.aeiou.bigbang.backend.security.BigAuthenticationFailureHandler;
import com.aeiou.bigbang.backend.security.BigAuthenticationProcessingFilter;
import com.aeiou.bigbang.backend.security.BigAuthenticationSuccessHandler;
import com.aeiou.bigbang.backend.security.UserDetailsAuthenticationProvider;
import com.aeiou.bigbang.util.SpringApplicationContext;
import com.aeiou.bigbang.web.PersonalController;
import com.aeiou.bigbang.web.PublicController;
import com.aeiou.bigbang.web.RemarkController;

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
		filter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
		return filter;
	}
	
	@Bean
	@Scope("singleton")
	// We just need to set the default failure url here
	AuthenticationFailureHandler authenticationFailureHandler() {
		return new BigAuthenticationFailureHandler();
	}
	
	@Bean
	@Scope("singleton")
	// We just need to set the default success url here
	AuthenticationSuccessHandler authenticationSuccessHandler() {
		BigAuthenticationSuccessHandler handler = new BigAuthenticationSuccessHandler();
		return handler;
	}

	@Bean
	@Scope("singleton")
	SimpleUrlLogoutSuccessHandler logoutSuccessHandler() {
		SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
		handler.setUseReferer(true);
		return handler;
	}
	
	@Bean(autowire = Autowire.BY_NAME)
	@Scope("singleton")
	SpringApplicationContext springApplicationContext() {
		return new SpringApplicationContext();
	}
	
	@Bean(autowire = Autowire.BY_NAME)
	@Scope("singleton")
	RemarkController remarkController() {
		return new RemarkController();
	}

	@Bean(autowire = Autowire.BY_NAME)
	@Scope("singleton")
	PersonalController personalController() {
		return new PersonalController();
	}
	
	@Bean(autowire = Autowire.BY_NAME)
	@Scope("singleton")
	PublicController publicController() {
		return new PublicController();
	}
}
