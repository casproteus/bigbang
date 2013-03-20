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
public class Remark {

    @NotNull
    private String content;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date remartTime;

    @NotNull
    private int privilege;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @NotNull
    @ManyToOne
    private Content replyTo;
    
	
	/**
     * @called from RemarkController->list when not admin as logged user. and PublicController->listRemarkByPublisher
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByTwitter(Long id, Set<Integer> pAuthSet, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery("SELECT o FROM Remark AS o WHERE o.publisher = :publisher and (o.authority in :pAuthSet) ORDER BY o.id DESC", Remark.class);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList(); 
    }

    public static long countRemarksByTwitter(Long id, Set<Integer> pAuthSet) {
    	TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Remark AS o WHERE o.publisher = :pPublisher and (o.authority in :pAuthSet)", Long.class);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
    	return tQuery.getSingleResult();
    }
}
