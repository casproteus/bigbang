package com.aeiou.bigbang.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * this class looks like is used by no other class, while you can not delete it, because it's use through aspect. don't
 * know by who.
 */
@Entity
@Configurable
public class Login {

    @Id
    private Long id;

    private boolean rememberMe;

    public boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(
            boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
