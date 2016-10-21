package com.aeiou.bigbang.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class RssTwitter {

    @NotNull
    @ManyToOne
    private UserAccount useraccount = new UserAccount();

    @NotNull
    @ManyToOne
    private Twitter twitter = new Twitter();

    public static boolean isAllreadyExist(
            UserAccount pUserAccount,
            Twitter pTwitter) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<RssTwitter> tQuery =
                tEntityManager.createQuery(
                        "SELECT o FROM RssTwitter AS o WHERE o.useraccount = :pUserAccount and o.twitter = :pTwitter",
                        RssTwitter.class);
        tQuery = tQuery.setParameter("pUserAccount", pUserAccount);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        List<RssTwitter> tList = tQuery.getResultList();
        return tList != null && tList.size() > 0;
    }

    public static List<RssTwitter> findAllListenersByTwitter(
            Twitter pTwitter) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<RssTwitter> tQuery =
                tEntityManager.createQuery("SELECT o FROM RssTwitter AS o WHERE o.twitter = :pTwitter",
                        RssTwitter.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        return tQuery.getResultList();
    }

    public static boolean deleteRssTwitterByTwitterAndUserAcount(
            Twitter pTwitter,
            UserAccount pUserAccount) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<RssTwitter> tQuery =
                tEntityManager.createQuery(
                        "SELECT o FROM RssTwitter AS o WHERE o.useraccount = :pUserAccount and o.twitter = :pTwitter",
                        RssTwitter.class);
        tQuery = tQuery.setParameter("pUserAccount", pUserAccount);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        try {
            RssTwitter tRssTwitter = tQuery.getSingleResult();
            tRssTwitter.remove();
        } catch (Exception e) {
            // do nothing.
        }

        return true;
    }
}
