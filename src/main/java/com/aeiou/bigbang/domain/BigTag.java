package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private int authority;

    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByPublisher(String pUserAccount, int firstResult, int maxResults) {
    	TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type ORDER BY o.id DESC", BigTag.class);
    	tQuery = tQuery.setParameter("type", pUserAccount).setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByOwner(String pUserAccount) {
    	List<BigTag> tListFR = new ArrayList<BigTag>();
    	tListFR.addAll(entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", "admin").getResultList());
        if (pUserAccount == null || "admin".equals(pUserAccount)) {
            return tListFR;
        } else {
        	//remove the other from the commontags. or some content will appear under both other and private tags.
        	for(int i = tListFR.size() - 1; i >= 0; i--){
        		if("other".equals(tListFR.get(i).getTagName())){
        			tListFR.remove(i);
        			break;
        		}
        	}
            //add tags of himself.
            List<BigTag> tTagListOfPublisher = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", pUserAccount).getResultList();
            
            List<String> tListOfTagNames = new ArrayList<String>();
            for(int i = 0; i < tTagListOfPublisher.size(); i++){
            	tListFR.add(tTagListOfPublisher.get(i));
            	tListOfTagNames.add(tTagListOfPublisher.get(i).getTagName());
            }
            //add tags from the publishers he listens to
            UserAccount tPublisher = UserAccount.findUserAccountByName(pUserAccount);
            Object[] tPublishers = tPublisher.getListento().toArray();
            for(int i = 0; i < tPublishers.length; i++){
            	tPublisher = (UserAccount)tPublishers[i];
            	if("admin".equals(tPublisher.getName())){
            		continue;
            	}
            	List<BigTag> tTagListOfListenedPublisher = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", tPublisher.getName()).getResultList();
            	//remove the duplicated ones.
            	for(int i2 = 0; i2 < tTagListOfListenedPublisher.size(); i2++){
            		String tTagName = tTagListOfListenedPublisher.get(i2).getTagName();
            		if(!tListOfTagNames.contains(tTagName)){
            			tListFR.add(tTagListOfListenedPublisher.get(i2));
            			tListOfTagNames.add(tTagName);
            		}
            	}
            }
            return tListFR;
        }
    }

    public static long countTagsByPublisher(String pPublisher) {
    	TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM BigTag o WHERE o.type = :pPublisher", Long.class);
        //TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.tagName = :pTagName And o.type = :pOwnerName", BigTag.class);
        tQuery = tQuery.setParameter("pPublisher", pPublisher);
        return tQuery.getSingleResult();
    }


    public String toString() {
        return tagName;
    }

    public static List<com.aeiou.bigbang.domain.BigTag> findBigTagEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM BigTag o ORDER BY o.id DESC", BigTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
