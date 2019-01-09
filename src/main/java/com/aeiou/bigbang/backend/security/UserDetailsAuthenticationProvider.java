package com.aeiou.bigbang.backend.security;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.aeiou.bigbang.domain.UserAccount;

@Configurable
public class UserDetailsAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    public static final Marker WS_MARKER = MarkerFactory.getMarker("WS");
    private static final Logger log = LoggerFactory.getLogger(UserDetailsAuthenticationProvider.class);

    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");

    private ApplicationContext applicationContext;

    @Override
    protected void additionalAuthenticationChecks(
            UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    public UserDetails retrieveUser(
            String userName,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        log.debug("Authenticating {} against server", userName);

        @SuppressWarnings("unchecked")
        String password = authentication.getCredentials().toString();

        if (StringUtils.isEmpty(password)) {
            throw new BadCredentialsException("Please enter password");
        }

        // check if the username and password match
        UserAccount tUserAccount = UserAccount.findUserAccountByName(userName);
        if (tUserAccount == null)
            throw new BadCredentialsException("The user dose not exist. Please check the input and try again.");
        if (!password.equals(tUserAccount.getPassword()))
            throw new BadCredentialsException("The password is not correct. Please try again.");

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        if ("admin".equals(userName))
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        else
            authorities.add(new SimpleGrantedAuthority("user"));

        User tUser = new User(userName, password, true, true, true, true, authorities);

        log.debug("Login user: {}", userName);

        return tUser;
    }
}
