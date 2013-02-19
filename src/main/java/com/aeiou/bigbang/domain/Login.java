package com.aeiou.bigbang.domain;

import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Configurable;

@Entity
@Configurable
public class Login{
	private boolean rememberMe;

	public boolean getRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}
}
