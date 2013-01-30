package com.aeiou.bigbang.domain;

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
    
    public static List<Content> findAllContentsByTag(BigTag pBigTag){
    	if (pBigTag == null)
        	return entityManager().createQuery("SELECT o FROM Content o", Content.class).getResultList();
        else{
        	Map tProps = new HashMap();
        	return entityManager().createQuery("SELECT o FROM Content o", Content.class).getResultList();
        }
    }
}
