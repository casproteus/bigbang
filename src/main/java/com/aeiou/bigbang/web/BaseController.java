package com.aeiou.bigbang.web;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.aeiou.bigbang.services.secutiry.UserContextService;

@Controller
public class BaseController {			

	@Inject
    protected UserContextService userContextService;
    
    @ModelAttribute("user")
	public User getUser() {
		return userContextService.getCurrentUser();
	}
    
    @ModelAttribute("userName")
    protected String getUserName() {
		return userContextService.getCurrentUserName();
	}
}