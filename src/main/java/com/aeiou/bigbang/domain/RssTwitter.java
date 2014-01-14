package com.aeiou.bigbang.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class RssTwitter {

    @NotNull
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<UserAccount> useraccount = new HashSet<UserAccount>();

    @NotNull
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Twitter> twitter = new HashSet<Twitter>();
}
