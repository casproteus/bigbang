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

    @ManyToOne
    private BigTag commonBigTag;

    public static List<Content> findContentsByTag(BigTag pBigTag, int maxResults) {
        if (pBigTag == null) 
        	return entityManager().createQuery("SELECT o FROM Content o", Content.class).getResultList(); 
        else if(maxResults < 0){
            return entityManager().createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :commonBigTag", Content.class).setParameter("commonBigTag", pBigTag).getResultList();
        }else{
            return entityManager().createQuery("SELECT o FROM Content AS o WHERE o.commonBigTag = :commonBigTag ORDER BY o.id DESC", Content.class).setParameter("commonBigTag", pBigTag).setFirstResult(0).setMaxResults(maxResults).getResultList();
        }
    }
}
