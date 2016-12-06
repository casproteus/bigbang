package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooJson
public class Customize {

    @NotNull
    @Column
    private String cusKey;

    @NotNull
    @Column
    private String cusValue;

    public static Customize findCustomizeByKey(
            String pCusKey) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Customize> tQuery =
                tEntityManager.createQuery("SELECT o FROM Customize AS o WHERE o.cusKey = :pCusKey", Customize.class);
        tQuery = tQuery.setParameter("pCusKey", pCusKey);
        try {
            return tQuery.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Customize> findCustomizesByOwner(
            UserAccount pOwner) {
        if (pOwner == null) {
            pOwner = UserAccount.findUserAccountByName("admin");
        }
        EntityManager tEntityManager = entityManager();
        TypedQuery<Customize> tQuery =
                tEntityManager.createQuery(
                        "SELECT o FROM Customize AS o WHERE o.useraccount = :owner ORDER BY o.cusKey", Customize.class);
        tQuery = tQuery.setParameter("owner", pOwner);
        try {
            return tQuery.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Customize> findCustomizeEntriesByOwner(
            int firstResult,
            int maxResults,
            String sortFieldName,
            String sortOrder,
            UserAccount userAccount) {

        String jpaQuery = "SELECT o FROM Customize o WHERE o.useraccount = " + userAccount.getName();
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<Customize> typedQuery =
                entityManager().createQuery(jpaQuery, Customize.class).setFirstResult(firstResult)
                        .setMaxResults(maxResults);
        List<Customize> customizes = new ArrayList<Customize>();
        try {
            customizes = typedQuery.getResultList();
        } catch (Exception e) {
            // there's no matching record, just ignore it, return empty arrayList.
        } finally {
            return customizes;
        }
    }

    public static List<Customize> findCustomizeEntries(
            int firstResult,
            int maxResults,
            String sortFieldName,
            String sortOrder) {
        String jpaQuery = "SELECT o FROM Customize o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Customize.class).setFirstResult(firstResult)
                .setMaxResults(maxResults).getResultList();
    }

    public static List<Customize> findAllCustomizes(
            String sortFieldName,
            String sortOrder) {
        String jpaQuery = "SELECT o FROM Customize o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Customize.class).getResultList();
    }

    /**
     */
    @ManyToOne
    private UserAccount useraccount;

}
