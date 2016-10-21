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
public class Message {

    @NotNull
    @ManyToOne
    private UserAccount receiver;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @NotNull
    private String content;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date postTime;

    private int status;

    /**
     * @called from MessageController->list when not admin as logged user. for checking if the new submitted message is
     *         a duplicated one.
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Message> findMessageByPublisher(
            UserAccount pReceiver,
            UserAccount pSender,
            int firstResult,
            int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Message> tQuery =
                tEntityManager
                        .createQuery(
                                "SELECT o FROM Message AS o WHERE o.receiver = :pReceiver and o.publisher = :pPublisher ORDER BY o.id DESC",
                                Message.class);
        tQuery = tQuery.setParameter("pReceiver", pReceiver);
        tQuery = tQuery.setParameter("pPublisher", pSender);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Message> findMessageByReceiver(
            UserAccount pReceiver,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Message> tQuery =
                tEntityManager.createQuery("SELECT o FROM Message AS o WHERE o.receiver = :pReceiver ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                        Message.class);
        tQuery = tQuery.setParameter("pReceiver", pReceiver);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Message> findMessageEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<Message> tQuery =
                entityManager()
                        .createQuery(
                                "SELECT o FROM Message o ORDER BY "
                                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                : sortExpression), Message.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countMessagesByReceiver(
            UserAccount pPublisher) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param!(pPublisher is null)------Message.countMessagesByReceiver()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery =
                    entityManager().createQuery("SELECT COUNT(o) FROM Message AS o WHERE o.receiver = :pPublisher",
                            Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            return tQuery.getSingleResult();
        }
    }
}
