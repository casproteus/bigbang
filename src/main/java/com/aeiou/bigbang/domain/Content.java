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

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @ManyToOne
    private BigTag commonBigTag;

    private int authority;

    @ManyToOne
    private BigTag uncommonBigTag;

    public static List<com.aeiou.bigbang.domain.Content> findContentsByTag(BigTag pBigTag, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :pBigTag and o.ORDER BY o.id DESC", Content.class);
        return tQuery.setParameter("pBigTag", pBigTag).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static long countContentsByTag(BigTag pTag) {
        if (pTag == null) {
            System.out.println("Content l79 is called!");
            Thread.dumpStack();
            return 0;
        } else {
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :pTag", Long.class).setParameter("pTag", pTag).getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Content o ORDER BY o.id DESC", Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
    public static List<com.aeiou.bigbang.domain.Content> findContentsByTagAndSpaceOwner(BigTag pTag, UserAccount pOwner, Set<Short> pAuthSet, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tSet = pOwner.getListento();	//if tSet is empty, then can not use it in parameter. will cause jpql exception.
        TypedQuery<Content> tQuery = null;
        if ("admin".equals(pTag.getType())) {	//for common tags.
        	if(tSet.isEmpty()){
        		tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner) and (o.authority in :pAuthSet) ORDER BY o.id DESC", Content.class);
        		tQuery = tQuery.setParameter("pTag", pTag);
        		tQuery = tQuery.setParameter("pOwner", pOwner);
        	}else{
        		tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner or o.publisher in :tSet) and (o.authority in :pAuthSet) ORDER BY o.id DESC", Content.class);
        		tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tSet", tSet);
        	}
        } else {								//for uncommon tags.
        	if(tSet.isEmpty()){
        		String tTagName = pTag.getTagName();
        		tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in :pAuthSet) ORDER BY o.id DESC", Content.class);
        		tQuery = tQuery.setParameter("tTagName", tTagName);
        		tQuery = tQuery.setParameter("pOwner", pOwner);
        	}else{
        		String tTagName = pTag.getTagName();
        		tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner or o.publisher in :tSet) and (o.authority in :pAuthSet) ORDER BY o.id DESC", Content.class);
        		tQuery = tQuery.setParameter("tTagName", tTagName);
        		tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tSet", tSet);
        	}
        }
		tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByTagAndSpaceOwner(BigTag pTag, UserAccount pOwner) {
        if (pTag == null) {
            System.out.println("201302122306");
            Thread.dumpStack();
            return 0;
        }
        
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tSet = pOwner.getListento();	//if tSet is empty, then can not use it in parameter. will cause jpql exception.
        TypedQuery<Long> tQuery = null;
        if ("admin".equals(pTag.getType())) {	//for common tags.
        	if(tSet.isEmpty()){
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner)", Long.class);
        		tQuery = tQuery.setParameter("pTag", pTag);
        		tQuery = tQuery.setParameter("pOwner", pOwner);
        	}else{
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner or o.publisher in :tSet)", Long.class);
        		tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tSet", tSet);
        	}
        } else {								//for uncommon tags.
        	if(tSet.isEmpty()){
        		String tTagName = pTag.getTagName();
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner)", Long.class);
        		tQuery = tQuery.setParameter("tTagName", tTagName);
        		tQuery = tQuery.setParameter("pOwner", pOwner);
        	}else{
        		String tTagName = pTag.getTagName();
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner or o.publisher in :tSet)", Long.class);
        		tQuery = tQuery.setParameter("tTagName", tTagName);
        		tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tSet", tSet);
        	}
        }
        return tQuery.getSingleResult();
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

}
