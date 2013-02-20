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

    private Integer authority;

    @ManyToOne
    private BigTag uncommonBigTag;

    /**
     * @called from PublicController->index (to show public contents), 
     * and called when click the show more from Public space 
     * (when click show more of personal space, will call findContentsByTagAndSpaceOwner)
     * @note: in public space, we always show only public contents, no matter user have logged in or not.
     * @param pBigTag
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Content> findContentsByTag(BigTag pBigTag, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :pBigTag and o.authority = 0 ORDER BY o.id DESC", Content.class);
        return tQuery.setParameter("pBigTag", pBigTag).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * @called from PublicController->show more, (of public space only)
     * @param pTag
     * @return
     */
    public static long countContentsByTag(BigTag pTag) {
        if (pTag == null) {
            System.out.println("Content l79 is called!");
            Thread.dumpStack();
            return 0;
        } else {
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :pTag", Long.class).setParameter("pTag", pTag).getSingleResult();
        }
    }

    /**
     * @called from ContentController by only admin as logged user. (if logged in user is not admin, will call findContentsByPublisher())
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Content> findContentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Content o ORDER BY o.id DESC", Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    /**
     * @called from ContentController->list when not admin as logged user. and PublicController->listContentByPublisher
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Content> findContentsByPublisher(UserAccount pPublisher, Set<Integer> pAuthSet, int firstResult, int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery("SELECT o FROM Content AS o WHERE o.publisher = :publisher and (o.authority in :pAuthSet) ORDER BY o.id DESC", Content.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList(); 
    }

    public static long countContentsByPublisher(UserAccount pPublisher, Set<Integer> pAuthSet) {
        if (pPublisher == null) {
            System.out.println("Content l89 is called!");
            Thread.dumpStack();
            return 0;
        } else {
        	TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.publisher = :pPublisher and (o.authority in :pAuthSet)", Long.class);
        	tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        	return tQuery.getSingleResult();
        }
    }

    /**
     * @called times from PersonalController, and once from PublicController ->show more(if in public space, show more will call "findContentsByTag")
     * Fetch the contents only published by the owner and his editors.
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
    public static List<com.aeiou.bigbang.domain.Content> findContentsByTagAndSpaceOwner(BigTag pTag, UserAccount pOwner, Set<Integer> pAuthSet, int firstResult, int maxResults) {
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

    /**
     * @called from PublicController->show more, (of personal space only)
     * @param pTag
     * @param pOwner
     * @return
     */
    public static long countContentsByTagAndSpaceOwner(BigTag pTag, UserAccount pOwner, Set<Integer> pAuthSet) {
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
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner) and (o.authority in :pAuthSet)", Long.class);
        		tQuery = tQuery.setParameter("pTag", pTag);
        		tQuery = tQuery.setParameter("pOwner", pOwner);
        	}else{
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner or o.publisher in :tSet) and (o.authority in :pAuthSet)", Long.class);
        		tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tSet", tSet);
        	}
        } else {								//for uncommon tags.
        	if(tSet.isEmpty()){
        		String tTagName = pTag.getTagName();
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in :pAuthSet)", Long.class);
        		tQuery = tQuery.setParameter("tTagName", tTagName);
        		tQuery = tQuery.setParameter("pOwner", pOwner);
        	}else{
        		String tTagName = pTag.getTagName();
        		tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner or o.publisher in :tSet) and (o.authority in :pAuthSet)", Long.class);
        		tQuery = tQuery.setParameter("tTagName", tTagName);
        		tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tSet", tSet);
        	}
        }
		tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        return tQuery.getSingleResult();
    }

}
