package com.aeiou.bigbang.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CookieHelper {

    String getCookie(
            String key,
            HttpServletRequest request);

    void addCookie(
            String key,
            String value,
            HttpServletResponse response);

    void deleteCookie(
            String key,
            HttpServletRequest request,
            HttpServletResponse response);

    boolean isOlder(
            String cookie1Key,
            String cookie2Key,
            boolean strict,
            HttpServletRequest request);

}
