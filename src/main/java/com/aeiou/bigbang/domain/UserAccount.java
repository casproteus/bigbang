package com.aeiou.bigbang.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class UserAccount {

    @NotNull
    @Column(unique = true)
    @Size(min = 2)
    private String name;

    private String email;

    @Size(min = 4)
    private String password;

    private String description;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<com.aeiou.bigbang.domain.UserAccount> listento = new HashSet<com.aeiou.bigbang.domain.UserAccount>();

	public String toString() {
        return this.getName();
    }
}
