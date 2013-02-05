package com.aeiou.bigbang.domain;

import java.util.List;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class BigTag {

    @NotNull
    @Column(unique = true)
    @Size(min = 2)
    private String tagName;

    @NotNull
    @Size(min = 2)
    private String type;
    
    public static List<BigTag> findTagsByType(String pType){
	    return entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", pType).getResultList();
    }
    
	public String toString() {
        return tagName;
    }
}
