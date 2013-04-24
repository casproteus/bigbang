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
import org.apache.commons.logging.LogFactory;
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
    @ManyToOne
    private UserAccount publisher;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date remarkTime;

    private Integer authority;

    @ManyToOne
    private Twitter remarkto;

    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByTwitter(Twitter pTwitter, Set<java.lang.Integer> pAuthSet, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery("SELECT o FROM Remark AS o WHERE o.remarkto = :pTwitter and (o.authority in :pAuthSet) ORDER BY o.id DESC", Remark.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countRemarksByTwitter(Twitter pTwitter, Set<java.lang.Integer> pAuthSet) {
        TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Remark AS o WHERE o.remarkto = :pTwitter and (o.authority in :pAuthSet)", Long.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        return tQuery.getSingleResult();
    }
    
    /**
     * @called from RemarkController->list when not admin as logged user.
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByPublisher(UserAccount pPublisher, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery("SELECT o FROM Remark AS o WHERE o.publisher = :publisher ORDER BY o.id DESC", Remark.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList(); 
    }

    public static long countRemarkByPublisher(UserAccount pPublisher) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error("------received a null as param!(pPublisher is null)------Remark.countRemarkByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
        	TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Remark AS o WHERE o.publisher = :pPublisher", Long.class);
        	tQuery = tQuery.setParameter("pPublisher", pPublisher);
        	return tQuery.getSingleResult();
        }
    }
    
	public String toString() {
        //return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		return content;
//		String tContent = content;
//    	 int tIdx = tContent.indexOf("<br />");
//    	 if (tIdx > 0)
//    		 tContent = tContent.substring(0, tIdx);
//    	 tContent = tContent.length() > 30 ? tContent.substring(0, 30) : tContent;
//    	 return tContent;
    }
}
