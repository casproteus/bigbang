package com.aeiou.bigbang.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.theme.CookieThemeResolver;

public class MyCookieThemeResolver extends CookieThemeResolver{
	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
		if (themeName != null) {
			// Set request attribute and add cookie.
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
//			addCookie(response, themeName);
			
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
