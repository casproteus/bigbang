package com.aeiou.bigbang.domain;

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

    /**
     */
    @ManyToOne
    private UserAccount useraccount;
}
