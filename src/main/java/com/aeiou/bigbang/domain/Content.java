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
public class Content {

    @NotNull
    private String title;

    @NotNull
    private String sourceURL;

    private String conentCache;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @ManyToOne
    private BigTag commonBigTag;

    private Integer authority;

    @ManyToOne
    private BigTag uncommonBigTag;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date markDate;

    @Transient
    private String addingTagFlag;

    public static List<com.aeiou.bigbang.domain.Content> findContentsByTag(
            BigTag pBigTag,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery =
                tEntityManager
                        .createQuery(
                                "SELECT o FROM Content AS o WHERE o.commonBigTag = :pBigTag and o.authority = 0 ORDER BY "
                                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                : sortExpression), Content.class);
        tQuery = tQuery.setParameter("pBigTag", pBigTag);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByTag(
            BigTag pTag) {
        if (pTag == null) {
            LogFactory.getLog(Content.class).error("------received a null as param!------Content.countContentsByTag()");
            Thread.dumpStack();
            return 0;
        } else {
            return entityManager()
                    .createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :pTag", Long.class)
                    .setParameter("pTag", pTag).getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<Content> tQuery =
                entityManager()
                        .createQuery(
                                "SELECT o FROM Content o ORDER BY "
                                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                : sortExpression), Content.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentsByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery =
                tEntityManager
                        .createQuery(
                                "SELECT o FROM Content AS o WHERE o.publisher = :publisher and (o.authority in :pAuthSet) ORDER BY "
                                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                : sortExpression), Content.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param! pPublisher is null------Content.countContentsByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery =
                    entityManager()
                            .createQuery(
                                    "SELECT COUNT(o) FROM Content AS o WHERE o.publisher = :pPublisher and (o.authority in :pAuthSet)",
                                    Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
            return tQuery.getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentsByTagAndSpaceOwner(
            BigTag pTag,
            UserAccount pOwner,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tTeamSet = pOwner.getListento();
        TypedQuery<Content> tQuery = null;
        if ("admin".equals(pTag.getType())) {
            if (tTeamSet.isEmpty()) {
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT o FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner) and (o.authority in :pAuthSet) ORDER BY "
                                                + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                        : sortExpression), Content.class);
                tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner);
            } else {
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT o FROM Content AS o WHERE (o.commonBigTag = :pTag and o.publisher = :pOwner and o.authority in :pAuthSet) or "
                                                + "(o.commonBigTag = :pTag and o.publisher in :tTeamSet and o.authority = 0) ORDER BY "
                                                + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                        : sortExpression), Content.class);
                tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
            }
        } else {
            if (tTeamSet.isEmpty()) {
                String tTagName = pTag.getTagName();
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT o FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in :pAuthSet) ORDER BY "
                                                + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                        : sortExpression), Content.class);
                tQuery = tQuery.setParameter("tTagName", tTagName);
                tQuery = tQuery.setParameter("pOwner", pOwner);
            } else {
                String tTagName = pTag.getTagName();
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT o FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName and o.publisher = :pOwner and o.authority in :pAuthSet) or "
                                                + "(o.uncommonBigTag.tagName = :tTagName and o.publisher in :tTeamSet and o.authority = 0) ORDER BY "
                                                + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC"
                                                        : sortExpression), Content.class);
                tQuery = tQuery.setParameter("tTagName", tTagName);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
            }
        }
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByTagAndSpaceOwner(
            BigTag pTag,
            UserAccount pOwner,
            Set<java.lang.Integer> pAuthSet) {
        if (pTag == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param! pTag is null------Content.countContentsByTagAndSpaceOwner()");
            Thread.dumpStack();
            return 0;
        }
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tTeamSet = pOwner.getListento();
        TypedQuery<Long> tQuery = null;
        if ("admin".equals(pTag.getType())) {
            if (tTeamSet.isEmpty()) {
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner) and (o.authority in :pAuthSet)",
                                        Long.class);
                tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner);
            } else {
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner or o.publisher in :tTeamSet) and (o.authority in :pAuthSet)",
                                        Long.class);
                tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
            }
        } else {
            if (tTeamSet.isEmpty()) {
                String tTagName = pTag.getTagName();
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in :pAuthSet)",
                                        Long.class);
                tQuery = tQuery.setParameter("tTagName", tTagName);
                tQuery = tQuery.setParameter("pOwner", pOwner);
            } else {
                String tTagName = pTag.getTagName();
                tQuery =
                        tEntityManager
                                .createQuery(
                                        "SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner or o.publisher in :tTeamSet) and (o.authority in :pAuthSet)",
                                        Long.class);
                tQuery = tQuery.setParameter("tTagName", tTagName);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
            }
        }
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
}
