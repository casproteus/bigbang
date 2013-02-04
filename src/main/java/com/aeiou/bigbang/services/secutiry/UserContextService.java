package com.aeiou.bigbang.services.secutiry;

import org.springframework.security.core.userdetails.User;

import com.aeiou.bigbang.backend.security.TokenBean;

public interface UserContextService {

	/**
	 * @return the current user if logged in, or null
	 */
	User getCurrentUser();

	String getCurrentUserName();

}