package com.aeiou.bigbang.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.LogFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooJson
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

    @Transient
    private int refresh_time;

    public int getRefresh_time() {
        return refresh_time;
    }

    public void setRefresh_time(
            int refresh_time) {
        this.refresh_time = refresh_time;
    }

    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByTwitter(
            Twitter pTwitter,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Remark AS o WHERE o.remarkto = :pTwitter and (o.authority in :pAuthSet) ORDER BY o.id DESC",
                Remark.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countRemarksByTwitter(
            Twitter pTwitter,
            Set<java.lang.Integer> pAuthSet) {
        TypedQuery<Long> tQuery = entityManager().createQuery(
                "SELECT COUNT(o) FROM Remark AS o WHERE o.remarkto = :pTwitter and (o.authority in :pAuthSet)",
                Long.class);
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
    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByPublisher(
            UserAccount pPublisher,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Remark AS o WHERE o.publisher = :publisher ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Remark.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countRemarkByPublisher(
            UserAccount pPublisher) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class)
                    .error("------received a null as param!(pPublisher is null)------Remark.countRemarkByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager()
                    .createQuery("SELECT COUNT(o) FROM Remark AS o WHERE o.publisher = :pPublisher", Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            return tQuery.getSingleResult();
        }
    }

    @Override
    public String toString() {
        return content;
    }

    public static List<Remark> findOrderedRemarkEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        return entityManager().createQuery(
                "SELECT o FROM Remark o ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Remark.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<Remark> findRemarkEntries(
            int firstResult,
            int maxResults) {
        return entityManager().createQuery("SELECT o FROM Remark o ORDER BY o.id DESC", Remark.class)
                .setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
