package com.aeiou.bigbang.backend.security;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * An authenticated user of the Casino, associated with his login token.
 */
public class BigEditor extends User {
	private static final long serialVersionUID = 4974361813077634130L;

	public BigEditor(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,  Collection<? extends GrantedAuthority> authorities) {

		super(username, password, enabled, accountNonExpired,
				credentialsNonExpired, accountNonLocked, authorities);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("User ");
		sb.append(getUsername());
		return sb.toString();
	}
}