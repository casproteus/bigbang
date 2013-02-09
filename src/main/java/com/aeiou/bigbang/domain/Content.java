package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
    
    public static List<Content> findContentsByTagAndSpaceOwner(BigTag pBigTag, UserAccount pPublisher, int maxResults) {
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

    public static long countContentsByTag(BigTag pBigTag) {
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
}
