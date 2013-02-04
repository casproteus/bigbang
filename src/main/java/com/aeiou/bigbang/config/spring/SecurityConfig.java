package com.aeiou.bigbang.config.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;

import com.aeiou.bigbang.backend.security.UserDetailsAuthenticationProvider;

@Configuration
public class SecurityConfig {

	@Bean
	@Scope("singleton")
	AuthenticationProvider backendAuthenticationProvider() {
		return new UserDetailsAuthenticationProvider();
	}
}
