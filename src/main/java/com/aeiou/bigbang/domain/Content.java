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

    /**
     * Called from publicController.java the link from that page need no considering about the space woner, just list all matching the tag.
     * @param pBigTag
     * @param maxResults
     * @return
     */
    public static List<Content> findContentsByTag(BigTag pBigTag, int maxResults) {
        if (pBigTag == null){ 		//no one should use this, it can cause trouble in future. 
        	System.out.println("Content l41 is called!");
        	Thread.dumpStack();
        	return entityManager().createQuery("SELECT o FROM Content o", Content.class).getResultList(); 
        }else if(maxResults < 0){	//no one should use this, it can cause trouble in future.
        	System.out.println("Content l45 is called!");
        	Thread.dumpStack();
            return entityManager().createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :commonBigTag", Content.class).setParameter("commonBigTag", pBigTag).getResultList();
        }else{
        	List<Content> tListFR = new ArrayList<Content>();
        	tListFR.addAll(entityManager().createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :commonBigTag ORDER BY o.id DESC", Content.class).setParameter("commonBigTag", pBigTag).setFirstResult(0).setMaxResults(maxResults).getResultList());
        	//1.when users are adding a content, he has to tell in which tag it will display in public space and in which space it will be in personal space.
        	//2.in private space, user can include all public tags, so when telling in which tag on main page, will also displayed in private space with same tag.
        	//3.with a given tag, it can appear both on commonBigTag and tags field, so we should check both field to make sure the content will be fetchout so it can be displayed on the webpage.
        	//4.but, if someone give his content a public tag, but want it displayed only in his private space, it's not allowed for now. if we support, we'll probably use two method, one match only the commontag field.
        	if(tListFR.isEmpty()){	
        		//TODO:how to write the contains relationship? why seems that "=" can do the work???
            	tListFR.addAll(entityManager().createQuery("SELECT o FROM Content AS o WHERE o.tags = :commonBigTag ORDER BY o.id DESC", Content.class).setParameter("commonBigTag", pBigTag).setFirstResult(0).setMaxResults(maxResults).getResultList());
            }
        	return tListFR;
        }
    }
    
    /**
     * Called from PersonalController, fetch the contents only published by the owner and his editors.
     * @note the owner of the BigTag is not necessary to be same with the owner, every space display admin's tags by default. 
     * @param pTag
     * @param pOwner
     * @param maxResults
     * @return
     */
    public static List<Content> findContentsByTagAndSpaceOwner(BigTag pTag, UserAccount pOwner, int maxResults) {
    	List<Content> tListFR = new ArrayList<Content>();
		//TODO:how to write the contains relationship? why seems that "=" can do the work???
    	TypedQuery<Content> tQuery = entityManager().createQuery("SELECT o FROM Content AS o WHERE (o.commonBigTag = :pTag or o.tags = :pTag) and (o.publisher = :pOwner or o.publisher.listento = :pOwner) ORDER BY o.id DESC", Content.class);
    	tQuery = tQuery.setParameter("pTag", pTag).setParameter("pOwner", pOwner).setFirstResult(0).setMaxResults(maxResults);
    	tListFR.addAll(tQuery.getResultList());
//    	//1.when users are adding a content, he has to tell in which tag it will display in public space and in which space it will be in personal space.
//    	//2.in private space,  all public tags will also displayed in private space. unless owner select to hide them.
//    	//3.but, if someone give his content a public tag, but want it displayed only in his private space, he can create a tag with same name (like "local")
    	return tListFR;
    }

    public static List<Content> findContentsByPublisher(UserAccount pPublisher, int maxResults) {
        if (pPublisher == null) {
        	System.out.println("Content l65 is called!");
        	Thread.dumpStack();
        	return entityManager().createQuery("SELECT o FROM Content o", Content.class).getResultList(); 
        }else if(maxResults < 0){
        	System.out.println("Content l69 is called!");
        	Thread.dumpStack();
            return entityManager().createQuery("SELECT o FROM Content AS o WHERE o.publisher = :publisher", Content.class).setParameter("publisher", pPublisher).getResultList();
        }else{
            return entityManager().createQuery("SELECT o FROM Content AS o WHERE o.publisher = :publisher ORDER BY o.id DESC", Content.class).setParameter("publisher", pPublisher).setFirstResult(0).setMaxResults(maxResults).getResultList();
        }
    }
    
    public static long countContentsByTag(BigTag pBigTag) {
        if (pBigTag == null) {
        	System.out.println("Content l79 is called!");
        	Thread.dumpStack();
        	return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult(); 
        }else{
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :commonBigTag", Long.class).setParameter("commonBigTag", pBigTag).getSingleResult();
        }
    }
    
    public static long countContentsByTagAndSpaceOwner(BigTag pBigTag, UserAccount pOwner) {
        if (pBigTag == null) {
        	System.out.println("Content l79 is called!");
        	Thread.dumpStack();
        	return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult(); 
        }else{
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :commonBigTag", Long.class).setParameter("commonBigTag", pBigTag).getSingleResult();
        }
    }    
    
    
    public static long countContentsByPublisher(UserAccount publisher) {
        if (publisher == null) {
        	System.out.println("Content l89 is called!");
        	Thread.dumpStack();
        	return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult(); 
        }else{
            return entityManager().createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.publisher = :publisher", Long.class).setParameter("publisher", publisher).getSingleResult();
        }
    }

	public static List<Content> findContentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Content o ORDER BY o.id DESC", Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
