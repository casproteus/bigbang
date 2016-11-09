package com.aeiou.bigbang.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

import com.aeiou.bigbang.util.BigUtil;

import flexjson.JSONSerializer;

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

    public static com.aeiou.bigbang.domain.UserAccount findUserAccountByName(
            String pUserName) {
        TypedQuery<UserAccount> tQuery =
                entityManager().createQuery("SELECT o FROM UserAccount AS o WHERE UPPER(o.name) = UPPER(:tname)",
                        UserAccount.class);
        tQuery = tQuery.setParameter("tname", pUserName);
        List<UserAccount> tList = tQuery.getResultList();
        if (tList != null && tList.size() == 1)
            return tList.get(0);
        else
            return null;
    }

    public static com.aeiou.bigbang.domain.UserAccount findUserAccountByNameAndPassword(
            String pUserNameAndPassword) {
        int p = pUserNameAndPassword.indexOf(BigUtil.SEP_ITEM);
        if (p < 0)
            return null;

        String pUserName = pUserNameAndPassword.substring(0, p);
        String pPassword = pUserNameAndPassword.substring(p);
        TypedQuery<UserAccount> tQuery =
                entityManager().createQuery("SELECT o FROM UserAccount AS o WHERE UPPER(o.name) = UPPER(:tname)",
                        UserAccount.class);
        tQuery = tQuery.setParameter("tname", pUserName);
        List<UserAccount> tList = tQuery.getResultList();
        if (tList != null && tList.size() == 1) {
            UserAccount tUserAccount = tList.get(0);
            if (tUserAccount.getPassword().equals(pPassword))
                return tUserAccount;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static List<com.aeiou.bigbang.domain.UserAccount> findOrderedUserAccountEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<UserAccount> tQuery =
                entityManager()
                        .createQuery(
                                "SELECT o FROM UserAccount o ORDER BY "
                                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                : sortExpression), UserAccount.class);
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.UserAccount> findUserAccountEntries(
            int firstResult,
            int maxResults) {
        TypedQuery<UserAccount> tQuery =
                entityManager().createQuery("SELECT o FROM UserAccount o ORDER BY o.id DESC", UserAccount.class);
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static String toJsonArray(
            Collection<UserAccount> collection) {
        return new JSONSerializer().include("listento").exclude("*.class").serialize(collection);
    }
}
