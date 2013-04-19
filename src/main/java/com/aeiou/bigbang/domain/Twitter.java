package com.aeiou.bigbang.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Twitter {

    @NotNull
    private String twitent;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date twitDate;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @ManyToOne
    private BigTag twittertag;

    private Integer authority;

    private String twtitle;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date lastupdate;

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Twitter o ORDER BY o.id DESC", Twitter.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByPublisher(UserAccount pPublisher, Set<java.lang.Integer> pAuthSet, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = tEntityManager.createQuery("SELECT o FROM Twitter AS o WHERE o.publisher = :publisher and (o.authority in :pAuthSet) ORDER BY o.lastupdate DESC", Twitter.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countTwitterByPublisher(UserAccount pPublisher, Set<java.lang.Integer> pAuthSet) {
        if (pPublisher == null) {
            System.out.println("Twitter 64 is called!");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Twitter AS o WHERE o.publisher = :pPublisher and (o.authority in :pAuthSet)", Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
            return tQuery.getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByOwner(UserAccount pOwner, Set<java.lang.Integer> pAuthSet, int firstResult, int maxResults) {
        Set<UserAccount> tTeamSet = pOwner.getListento();
        if (tTeamSet.isEmpty()) return null;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = tEntityManager.createQuery("SELECT o FROM Twitter AS o WHERE (o.publisher in :tTeamSet and o.authority = 0) ORDER BY o.lastupdate DESC", Twitter.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countTwittersByOwner(UserAccount pOwner, Set<java.lang.Integer> pAuthSet) {
        Set<UserAccount> tTeamSet = pOwner.getListento();
        if (tTeamSet.isEmpty()) return 0;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Long> tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Twitter AS o WHERE (o.publisher in :tTeamSet) and (o.authority = 0)", Long.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        return tQuery.getSingleResult();
    }

    public String toString() {
        return twtitle;
    }
}
