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

    @Transient
    private String addingTagFlag;

    /**
     * return the first twitter of the user, that means when we create a new account, we have to add a default twitter
     * automatically.
     * 
     * @note: didn't use Id, because users who already created twitter, will have trouble. use twitDate, I can create a
     *        twitter and modify it's date easyly to be before every every other twitter:)
     * @param pReceiver
     * @return
     */
    public static com.aeiou.bigbang.domain.Twitter findMessageTwitter(
            UserAccount pReceiver) {
        if (pReceiver == null)
            return null;
        TypedQuery<Twitter> tQuery =
                entityManager().createQuery(
                        "SELECT o FROM Twitter o WHERE o.publisher = :publisher ORDER BY o.twitDate", Twitter.class);
        tQuery = tQuery.setParameter("publisher", pReceiver);
        return tQuery.getSingleResult();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findOrderedTwitterEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<Twitter> tQuery =
                entityManager().createQuery(
                        "SELECT o FROM Twitter o  ORDER BY "
                                + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                        : sortExpression), Twitter.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterEntries(
            int firstResult,
            int maxResults) {
        TypedQuery<Twitter> tQuery =
                entityManager().createQuery("SELECT o FROM Twitter o  ORDER BY o.lastupdate DESC", Twitter.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery =
                tEntityManager.createQuery(
                        "SELECT o FROM Twitter AS o WHERE o.publisher = :publisher and (o.authority in :pAuthSet) ORDER BY "
                                + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                        : sortExpression), Twitter.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countTwitterByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param!(pPublisher is null)------Twitter.countTwitterByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery =
                    entityManager()
                            .createQuery(
                                    "SELECT COUNT(o) FROM Twitter AS o WHERE o.publisher = :pPublisher and (o.authority in :pAuthSet)",
                                    Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
            return tQuery.getSingleResult();
        }
    }

    /**
     * these twitter list will appear on personal space on the top right. it will display the twitter from owner's
     * friend which are setted as public. (while, I hope if the current user is logged in, and is friends of the author
     * of some twitter, the twitter should be displayed? is that possible? )
     */
    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByOwner(
            Set<UserAccount> tTeamSet,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        if (tTeamSet.isEmpty())
            return null;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery =
                tEntityManager.createQuery(
                        "SELECT o FROM Twitter AS o WHERE (o.publisher in :tTeamSet) and (o.authority in :pAuthSet) ORDER BY "
                                + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                        : sortExpression), Twitter.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countTwittersByOwner(
            Set<UserAccount> tTeamSet,
            Set<java.lang.Integer> pAuthSet) {
        if (tTeamSet.isEmpty())
            return 0;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Long> tQuery =
                tEntityManager
                        .createQuery(
                                "SELECT COUNT(o) FROM Twitter AS o WHERE (o.publisher in :tTeamSet) and (o.authority in :pAuthSet)",
                                Long.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        return tQuery.getSingleResult();
    }

    public String getAddingTagFlag() {
        return addingTagFlag;
    }

    public void setAddingTagFlag(
            String addingTagFlag) {
        this.addingTagFlag = addingTagFlag;
    }

    public String toString() {
        return twtitle;
    }
}
