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

	/**
     * @called from PersonalController ->index,
	 * @called once from PublicController ->show more(if in public space, show more will not be displayed, neither do the twitter area for now.)
     * Fetch the twitter only published by the owner and his editors.
     * 1.when users are adding a twitter, he has to tell in which tag it belongs.
     * 2.the owner of the BigTag is not necessary to be the space owner. all team's 0 tag will also be showed.
     * 3.every space display admin's tags by default. but user can hide it. 
     * @param pTag
     * @param pOwner
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByOwner(UserAccount pOwner, Set<Integer> pAuthSet, int firstResult, int maxResults) {
        Set<UserAccount> tTeamSet = pOwner.getListento();	//if tTeamSet is empty, then can not use it in parameter. will cause jpql exception.
        if(tTeamSet.isEmpty())
    		return null;
        
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = tEntityManager.createQuery(
        		"SELECT o FROM Twitter AS o WHERE (o.publisher in :tTeamSet and o.authority = 0) ORDER BY o.id DESC", Twitter.class);
    	tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    /**
     * @called from PublicController->show more, (of personal space only)
     * @param pTag
     * @param pOwner
     * @return
     */
    public static long countTwittersByOwner(UserAccount pOwner, Set<Integer> pAuthSet) {
        Set<UserAccount> tTeamSet = pOwner.getListento();	//if tTeamSet is empty, then can not use it in parameter. will cause jpql exception.
        if(tTeamSet.isEmpty())
        	return 0;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Long> tQuery = tEntityManager.createQuery(
        		"SELECT COUNT(o) FROM Twitter AS o WHERE (o.publisher in :tTeamSet) and (o.authority = 0)", Long.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        return tQuery.getSingleResult();
    }
}
