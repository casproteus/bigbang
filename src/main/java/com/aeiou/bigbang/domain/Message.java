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
public class Message {

    @NotNull
    @ManyToOne
    private UserAccount receiver;

    @NotNull
    @ManyToOne
    private UserAccount sender;

    @NotNull
    private String content;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date postTime;

    private int status;
    
    /**
     * @called from MessageController->list when not admin as logged user.
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Message> findMessageByPublisher(UserAccount pReceiver, UserAccount pSender, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Message> tQuery = tEntityManager.createQuery("SELECT o FROM Message AS o WHERE o.receiver = :pReceiver and o.sender = :pSender ORDER BY o.id DESC", Message.class);
        tQuery = tQuery.setParameter("pReceiver", pReceiver);
        tQuery = tQuery.setParameter("pSender", pSender);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList(); 
    }
    
    public static List<com.aeiou.bigbang.domain.Message> findMessageByReceiver(UserAccount pReceiver, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Message> tQuery = tEntityManager.createQuery("SELECT o FROM Message AS o WHERE o.receiver = :pReceiver ORDER BY o.id DESC", Message.class);
        tQuery = tQuery.setParameter("pReceiver", pReceiver);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countMessagesByReceiver(UserAccount pPublisher) {
        if (pPublisher == null) {
            System.out.println("Message 67 is called!");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Message AS o WHERE o.receiver = :pPublisher", Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            return tQuery.getSingleResult();
        }
    }
}
