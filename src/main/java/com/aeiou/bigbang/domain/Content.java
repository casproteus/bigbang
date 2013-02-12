package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Content {

    @NotNull
    private String title;

    @NotNull
    private String sourceURL;

    private String conentCache;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<BigTag> tags = new HashSet<BigTag>();

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @ManyToOne
    private BigTag commonBigTag;

    private Short authority;

    public static List<com.aeiou.bigbang.domain.Content> findContentsByTag(BigTag pBigTag, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :pBigTag ORDER BY o.id DESC", Content.class);
        return tQuery.setParameter("pBigTag", pBigTag).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static long countContentsByTag(BigTag pBigTag) {
        if (pBigTag == null) {
            System.out.println("Content l79 is called!");
            Thread.dumpStack();
            return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult();
        } else {
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :commonBigTag", Long.class).setParameter("commonBigTag", pBigTag).getSingleResult();
        }
    }
    
    /**
     * Called from PersonalController, fetch the contents only published by the owner and his editors.
     * 1.when users are adding a content, he has to tell in which tag it will display in public space and in which space it will be in personal space.
     * 2.in private space,  all public tags will also displayed in private space. unless owner select to hide them.
     * 3.but, if someone give his content a public tag, but want it displayed only in his private space, he can create a tag with same name (like "local")
     * 4.the owner of the BigTag is not necessary to be the space owner.
     * 5.every space display admin's tags by default. 
     * @param pTag
     * @param pOwner
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Content> findContentsByTagAndSpaceOwner(BigTag pTag, UserAccount pOwner, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tSet = pOwner.getListento();
        TypedQuery<Content> tQuery = null;
        if ("admin".equals(pTag.getType())) {
            tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner or o.publisher in :tSet) ORDER BY o.id DESC", Content.class);
        } else {
            String tTagName = pTag.getTagName();
            tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE (o.tags = :pTag) and (o.publisher = :pOwner or o.publisher in :tSet) ORDER BY o.id DESC", Content.class);
        }
        tQuery = tQuery.setParameter("pTag", pTag).setParameter("pOwner", pOwner);
        tQuery = tQuery.setParameter("tSet", tSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByTagAndSpaceOwner(BigTag pBigTag, UserAccount pOwner) {
        if (pBigTag == null) {
            System.out.println("Content l79 is called!");
            Thread.dumpStack();
            return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult();
        } else {
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :commonBigTag", Long.class).setParameter("commonBigTag", pBigTag).getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentsByPublisher(UserAccount pPublisher, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE o.publisher = :publisher ORDER BY o.id DESC", Content.class);
        return tQuery.setParameter("publisher", pPublisher).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static long countContentsByPublisher(UserAccount publisher) {
        if (publisher == null) {
            System.out.println("Content l89 is called!");
            Thread.dumpStack();
            return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult();
        } else {
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.publisher = :publisher", Long.class).setParameter("publisher", publisher).getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Content o ORDER BY o.id DESC", Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
