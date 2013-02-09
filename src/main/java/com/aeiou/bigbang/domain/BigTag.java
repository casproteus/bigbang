package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    /**
     * @param pUserAccount
     * @return
     */
    public static List<BigTag> findTagsByType(String pUserAccount){
    	//fetch out all public tags.
    	//check if it's authenticated. and match with the given pType, fetch all tags with his name as type
    	//      if it's not authenticated, or not matching with the given one, then match only the public tags.
    	List<BigTag> tList = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", "admin").getResultList();
    	if(pUserAccount == null || "admin".equals(pUserAccount)){
    		return tList;
    	}else{
    		List<BigTag> tListFR = new ArrayList<BigTag>();
    		tListFR.addAll(entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", pUserAccount).getResultList());
    		tListFR.addAll(tList);
    		return tListFR;
    	}
    }
    
    public static BigTag findTagsByTypeNameAndOwner(String pTagName, String pOwnerName){
    	TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.tagName = :pTagName And o.type = :pOwnerName", BigTag.class);
    	tQuery  = tQuery.setParameter("pTagName", pTagName).setParameter("pOwnerName", pOwnerName);
    	return tQuery.getResultList().get(0);
    }
    
	public String toString() {
        return tagName;
    }
}
