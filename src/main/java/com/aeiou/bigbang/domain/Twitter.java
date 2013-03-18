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

	public static List<Twitter> findTwitterEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Twitter o ORDER BY o.id DESC", Twitter.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
	
	/**
     * @called from TwitterController->list when not admin as logged user. and PublicController->listTwitterByPublisher
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByPublisher(UserAccount pPublisher, Set<Integer> pAuthSet, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = tEntityManager.createQuery("SELECT o FROM Twitter AS o WHERE o.publisher = :publisher and (o.authority in :pAuthSet) ORDER BY o.id DESC", Twitter.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList(); 
    }

    public static long countTwitterByPublisher(UserAccount pPublisher, Set<Integer> pAuthSet) {
        if (pPublisher == null) {
            System.out.println("Content l89 is called!");
            Thread.dumpStack();
            return 0;
        } else {
        	TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Twitter AS o WHERE o.publisher = :pPublisher and (o.authority in :pAuthSet)", Long.class);
        	tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        	return tQuery.getSingleResult();
        }
    }

}
