package com.aeiou.bigbang.backend.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

public class BigAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private static final String DEFAULT_FILTER_PROCESSES_URL = "/resources/j_spring_security_check";

	@Inject
	private MessageSource messageSource;

	public BigAuthenticationProcessingFilter() {
		super(DEFAULT_FILTER_PROCESSES_URL);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

		if (!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String login_name = request.getParameter("j_username");
		String login_password = request.getParameter("j_password");
		login_name = login_name == null ? "" : login_name.trim();
		login_password = login_password == null ? "" : login_password;
		
		final boolean rememberMe = "on".equals(request.getParameter("j_rememberme"));

		if (!rememberMe) {
			final Cookie cookie = new Cookie("login_name", "");
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
			
			final Cookie cookie2 = new Cookie("login_password", "");
			cookie2.setMaxAge(0);
			cookie2.setPath("/");
			response.addCookie(cookie2);
		}

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(login_name, login_password);
		final Authentication authentication = this.getAuthenticationManager().authenticate(authRequest);

		if (rememberMe) {
			try { 
				login_name = URLEncoder.encode(login_name,"UTF-8");
				final Cookie cookie = new Cookie("login_name", login_name);
				cookie.setMaxAge(31536000); // One year
				cookie.setPath("/");
				response.addCookie(cookie);
				
				login_password = URLEncoder.encode(login_password,"UTF-8");
				final Cookie cookie2 = new Cookie("login_password", login_password);
				cookie2.setMaxAge(31536000); // One year
				cookie2.setPath("/");
				response.addCookie(cookie2);
		    } catch (UnsupportedEncodingException e) {
		    } 
		}
		
		return authentication;
	}
}
