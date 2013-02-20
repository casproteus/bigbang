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

    private Integer authority;
    
    /**
     * @called when logged in user listing all his tag he's created to edit. if the logged in user is admin, will list everyone's tag.
     * @called when logged in user creating a content. all available tags will be displayed in the drop box.
     * @param pUserAccount
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByPublisher(String pUserAccount, int firstResult, int maxResults) {
    	TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type ORDER BY o.id DESC", BigTag.class);
    	tQuery = tQuery.setParameter("type", pUserAccount).setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }
    
    /**
     * @called when logged in user listing all he's created tags in a paged list.
     * @param pPublisher
     * @return
     */
    public static long countTagsByPublisher(String pPublisher) {
    	TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM BigTag o WHERE o.type = :pPublisher", Long.class);
        tQuery = tQuery.setParameter("pPublisher", pPublisher);
        return tQuery.getSingleResult();
    }

    /**
     * @called when publicPage and and personalPage are displaying, call this method to fetch out all tags possible be displayed.
     * @note   should work on the returned list base on the login status. 
     * @param pOwnerName
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByOwner(String pOwnerName) {
    	List<BigTag> tListFR = new ArrayList<BigTag>();
    	
    	//first fetch out all admin's tags.
    	tListFR.addAll(entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", "admin").getResultList());
        //if displaying public page, then tListFR is enough for return
    	if (pOwnerName == null || "admin".equals(pOwnerName))
            return tListFR;
    	//if displaying a personal page, then add tags of himself's and his team's.
        else {
            //add tags of himselfs, also keep a name list, to avoid duplicated tags.. 
            List<BigTag> tTagListOfPublisher = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", pOwnerName).getResultList();
            List<String> tListOfTagNames = new ArrayList<String>();
            for(int i = 0; i < tTagListOfPublisher.size(); i++){
            	tListFR.add(tTagListOfPublisher.get(i));
            	tListOfTagNames.add(tTagListOfPublisher.get(i).getTagName());
            }
            
            //add tags from the publishers he listens to
            UserAccount tPublisher = UserAccount.findUserAccountByName(pOwnerName);
            Object[] tPublishers = tPublisher.getListento().toArray();
            for(int i = 0; i < tPublishers.length; i++){
            	tPublisher = (UserAccount)tPublishers[i];
            	if("admin".equals(tPublisher.getName()))
            		continue;
            	
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
    
    public String toString() {
        return tagName;
    }

    public static List<com.aeiou.bigbang.domain.BigTag> findBigTagEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM BigTag o ORDER BY o.id DESC", BigTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
