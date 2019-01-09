package com.aeiou.bigbang.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CookieHelperImpl implements CookieHelper {

    private static final Logger log = LoggerFactory.getLogger(CookieHelperImpl.class);

    @Override
    public String getCookie(
            String key,
            HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getMaxAge() > 0 && key.equals(cookie.getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found cookie {}={} having maxAge={}",
                                new Object[] { cookie.getName(), cookie.getValue(), cookie.getMaxAge() });
                    }
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isOlder(
            String cookie1Key,
            String cookie2Key,
            boolean strict,
            HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            int age1 = 0;
            int age2 = 0;
            for (Cookie cookie : cookies) {
                if (cookie1Key.equals(cookie.getName()) && !isEmpty(cookie.getValue())) {
                    age1 = cookie.getMaxAge();
                }
                if (cookie2Key.equals(cookie.getName()) && !isEmpty(cookie.getValue())) {
                    age2 = cookie.getMaxAge();
                }
            }
            if (age1 == 0 && age2 == -1) {
                return false;
            }
            if (age1 == age2) {
                return !strict;
            }
            return age1 >= age2;
        }
        return true;
    }

    @Override
    public void addCookie(
            String key,
            String value,
            HttpServletResponse response) {
        Cookie cookie = new Cookie(key, value);
        // Expires within 60 days
        cookie.setMaxAge(60 * 24 * 60 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public void deleteCookie(
            String key,
            HttpServletRequest request,
            HttpServletResponse response) {
        Cookie cookie = null;
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(key)) {
                cookie = c;
                cookie.setValue(null);
            }
        }
        if (cookie == null) {
            cookie = new Cookie(key, null);
        }
        // Expires immediately
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}
