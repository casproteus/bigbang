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
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
	}

	@Override
	public UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

		log.debug("Authenticating {} against server", userName);

		@SuppressWarnings("unchecked")
		String password = authentication.getCredentials().toString();

		if (StringUtils.isEmpty(password)) {
			throw new BadCredentialsException("Please enter password");
		}
		
		//check if the username and password match
		UserAccount tUserAccount = UserAccount.findUserAccountByName(userName);
		if(tUserAccount == null)
			throw new BadCredentialsException("The user dose not exist. Please check the input and try again.");
		if(!password.equals(tUserAccount.getPassword()))
			throw new BadCredentialsException("The password is not correct. Please try again.");
		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		if("admin".equals(userName))
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		else
			authorities.add(new SimpleGrantedAuthority("user"));

		User tUser = new User(userName, password, true, true, true, true, authorities);

		log.debug("Login user: {}", userName);

		return tUser;
	}

	private TokenBean connect(final String userName, final String password) {
		TokenBean token = null;
//		try {
//			token = webServiceContextHandler.runAsAdmin("userAccountFacade.connect()", new Callable<TokenBean>() {
//						@Override
//						public TokenBean call() throws Exception {
//							log.debug(WS_MARKER, "[WS] Connect user {} on realm {}", userName, authRealm);
//							return userAccountFacade.connect(authRealm, userName, password);
//						};
//					});
//			log.info("User {} is now logged in", userName);
//		} catch (BigUserException e) {
//			final int errorId = e.getFaultInfo().getID();
//			if (log.isDebugEnabled()) {
//				log.debug("Cannot login user " + userName + " born on "
//						+ birthDateStr + ", cause: #"
//						+ errorId + " "
//						+ e.getFaultInfo().getText(), e);
//			}
//			switch (errorId) {
//			case BigErrorCause.BigSecurityError.ACCOUNT_NOT_ACTIVATED:
//				throw new AccountNotActivatedException(e.getFaultInfo().getText());
//			case BigErrorCause.AccountManagementError.OPERATION_NOT_ALLOWED_BY_GEOLOC:
//				throw new GeolockedException(e.getFaultInfo().getText());
//			}
//			throw new BadCredentialsException(e.getFaultInfo().getText(), e);
//		} catch (Exception e) {
//			log.error("Cannot login user " + userName, e);
//			String msg = applicationContext.getMessage("internal_error",
//					new Object[0], LocaleContextHolder.getLocale());
//			throw new AuthenticationServiceException(msg, e);
//		}
		return token;
	}
}