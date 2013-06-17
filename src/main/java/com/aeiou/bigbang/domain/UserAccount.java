package com.aeiou.bigbang.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooJson
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

    private int theme;

    private int status;

    private int newMessageAmount;

    public static com.aeiou.bigbang.domain.UserAccount findUserAccountByName(String pUserName) {
        TypedQuery<UserAccount> tQuery = entityManager().createQuery("SELECT o FROM UserAccount AS o WHERE UPPER(o.name) = UPPER(:tname)", UserAccount.class);
        tQuery = tQuery.setParameter("tname", pUserName);
        List<UserAccount> tList = tQuery.getResultList();
        if (tList != null && tList.size() == 1) return (UserAccount) tList.get(0); else return null;
    }

    public String toString() {
        return this.getName();
    }

    public static List<com.aeiou.bigbang.domain.UserAccount> findUserAccountEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM UserAccount o ORDER BY o.id DESC", UserAccount.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
