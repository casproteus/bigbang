package com.aeiou.bigbang.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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

    @Min(0L)
    @Max(9L)
    private int price;

    private String layout;

    @NotNull
    private int balance;

    public static com.aeiou.bigbang.domain.UserAccount findUserAccountByName(String pUserName) {
        List tList = entityManager().createQuery("SELECT o FROM UserAccount AS o WHERE UPPER(o.name) = UPPER(:tname)", UserAccount.class).setParameter("tname", pUserName).getResultList();
        if (tList != null && tList.size() == 1)
        	return (UserAccount) tList.get(0);
        else
        	return null;
    }

    public String toString() {
        return this.getName();
    }
}
