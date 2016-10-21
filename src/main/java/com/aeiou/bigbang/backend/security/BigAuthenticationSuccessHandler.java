package com.aeiou.bigbang.backend.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;
import com.aeiou.bigbang.web.PersonalController;

public class BigAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Inject
    private UserContextService userContextService;

    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        // looks like when clicking log out, the savedRequest will be updated with current page (if the current page
        // need authentication) or with null.
        if (savedRequest == null) {
            // when use Chinese name to login, the userContextService.getCurrentUserName() will be transfered into weird
            // string
            // by getRedirectStrategy().sendRedirect. so we dare not to use the personal space as default login success
            // page.
            String targetUrl = "/" + userContextService.getCurrentUserName();
            logger.debug("Redirecting to psersonal Url: " + targetUrl);
            // to change the code of targetUrl back to "ISO-8859-1", other wise the chinese user name will be lost by
            // method sendRedirect
            byte tByteAry[];
            tByteAry = targetUrl.getBytes("UTF-8");
            targetUrl = new String(tByteAry, "ISO-8859-1");
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;

            // we can not use following way to display the personal page (to avoid the encoding error of targetUrl)
            // because [1] thrown exception when calling tOwner.getListento();
            // [2] even if I can resolve the exception, the following method return a string as url to display the page,
            // while here we don't know how to handle the string.
            // PersonalController tController =
            // SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
            // tController.index(userContextService.getCurrentUserName(), -1, -1, null);
        }
        String targetUrlParameter = getTargetUrlParameter();
        if (isAlwaysUseDefaultTargetUrl()
                || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
            requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }

        clearAuthenticationAttributes(request);

        // Use the DefaultSavedRequest URL
        String targetUrl = savedRequest.getRedirectUrl();
        logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
