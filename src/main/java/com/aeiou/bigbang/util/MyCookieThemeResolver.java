package com.aeiou.bigbang.util;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.theme.CookieThemeResolver;

import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;

public class MyCookieThemeResolver extends CookieThemeResolver{
    @Inject
    private UserContextService userContextService;
    
	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
		if (themeName != null) {
			// Set request attribute and add cookie.
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
//			addCookie(response, themeName);
			//todo:logged in and is in his personal space
			String tURLPath = request.getServletPath();
			String tCurName = userContextService.getCurrentUserName();
			if(tURLPath != null && tCurName != null && tURLPath.equals("/" + tCurName)){
				UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
				tCurName = tUserAccount.getName();
				tUserAccount.setTheme(Integer.parseInt(themeName));
				tUserAccount.persist();				//save the theme into account info.
			}
			//save the theme into cookie
			final Cookie cookie = new Cookie("theme", themeName);
			cookie.setMaxAge(31536000); // One year
			cookie.setPath("/");
			response.addCookie(cookie);
		}

		else {
			// Set request attribute to fallback theme and remove cookie.
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, getDefaultThemeName());
			removeCookie(response);
		}
	}
}
