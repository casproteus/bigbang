package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;
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
    @Size(min = 2)
    private String tagName;

    @NotNull
    @Size(min = 2)
    private String type;

    private Integer authority;

    private Integer owner;
    
    @Transient
    private String twitterTitle;
    @Transient
	private String twitterContent;
    @Transient
    private String contentTitle;
    @Transient
	private String contentURL;
    @Transient
	private String commonTagName;
	
	public String getContentTitle() {
		return contentTitle;
	}

	public void setContentTitle(String contentTitle) {
		this.contentTitle = contentTitle;
	}

	public String getContentURL() {
		return contentURL;
	}

	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}
	
    public String getTwitterTitle() {
		return twitterTitle;
	}

	public void setTwitterTitle(String twitterTitle) {
		this.twitterTitle = twitterTitle;
	}
	
	public String getTwitterContent() {
		return twitterContent;
	}

	public void setTwitterContent(String twitterContent) {
		this.twitterContent = twitterContent;
	}

    /**
     * called when listing all created tags from bigtagController.
     * called when the userlogin name changes from UserAccountController.
     * called when deleting a user account, all his tag should be also deleted, this can happen only when running test script.
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
     * called when listing all created tags from bigtagController.
     * @param pPublisher
     * @return
     */
    public static long countTagsByPublisher(String pPublisher) {
        TypedQuery<Long> tQuery = entityManager().createQuery("SELECT COUNT(o) FROM BigTag o WHERE o.type = :pPublisher", Long.class);
        tQuery = tQuery.setParameter("pPublisher", pPublisher);
        return tQuery.getSingleResult();
    }

    /**
     * called only by contentController.populateEditForm().
     * @param pUserAccount
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findBMTagsByPublisher(String pUserAccount, int firstResult, int maxResults) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type and o.owner = 0 ORDER BY o.id DESC", BigTag.class);
        tQuery = tQuery.setParameter("type", pUserAccount).setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    /**
     * called only by twitterController.populateEditForm().
     * @param pUserAccount
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findTWTagsByPublisher(String pUserAccount) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type and o.owner = :owner ORDER BY o.id DESC", BigTag.class);
        tQuery = tQuery.setParameter("type", pUserAccount).setParameter("owner", 1).setFirstResult(0).setMaxResults(1000);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.BigTag> findBMTagsByOwner(String pOwnerName) {
        List<BigTag> tListFR = new ArrayList<BigTag>();
        tListFR.addAll(entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type and o.owner = 0", BigTag.class).setParameter("type", "admin").getResultList());
        tListFR.addAll(entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type and o.owner = 0", BigTag.class).setParameter("type", "administrator").getResultList());
        if (pOwnerName == null || "admin".equals(pOwnerName)) return tListFR; else {
            List<BigTag> tTagListOfPublisher = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type and o.owner = 0", BigTag.class).setParameter("type", pOwnerName).getResultList();
            List<String> tListOfTagNames = new ArrayList<String>();
            for (int i = 0; i < tTagListOfPublisher.size(); i++) {
                tListFR.add(tTagListOfPublisher.get(i));
                tListOfTagNames.add(tTagListOfPublisher.get(i).getTagName());
            }
            UserAccount tPublisher = UserAccount.findUserAccountByName(pOwnerName);
            Object[] tPublishers = tPublisher.getListento().toArray();
            for (int i = 0; i < tPublishers.length; i++) {
                tPublisher = (UserAccount) tPublishers[i];
                if ("admin".equals(tPublisher.getName())) continue;
                List<BigTag> tTagListOfListenedPublisher = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type and o.owner = 0 AND o.authority = 0", BigTag.class).setParameter("type", tPublisher.getName()).getResultList();
                for (int j = 0; j < tTagListOfListenedPublisher.size(); j++) {
                    String tTagName = tTagListOfListenedPublisher.get(j).getTagName();
                    if (!tListOfTagNames.contains(tTagName)) {
                        tListFR.add(tTagListOfListenedPublisher.get(j));
                        tListOfTagNames.add(tTagName);
                    }
                }
            }
            return tListFR;
        }
    }

    public static com.aeiou.bigbang.domain.BigTag findBMTagByNameAndOwner(String pTagName, String pOwnerName) {
        BigTag tBigTag = null;
        TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.tagName = :pTagName and o.type = :pOwnerName and o.owner = 0", BigTag.class);
        tQuery = tQuery.setParameter("pTagName", pTagName).setParameter("pOwnerName", pOwnerName);
        try {
            tBigTag = tQuery.getSingleResult();
        } catch (Exception e) {
        }
        if (tBigTag == null) {
            Set<String> tOwnerNameSet = new HashSet<String>();
            UserAccount tOwner = UserAccount.findUserAccountByName(pOwnerName);
            Iterator<UserAccount> tList = tOwner.getListento().iterator();
            while (tList.hasNext()) tOwnerNameSet.add(tList.next().getName());
            tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.tagName = :pTagName and o.owner = 0 and o.type in :tOwnerNameSet", BigTag.class);
            tQuery = tQuery.setParameter("pTagName", pTagName).setParameter("tOwnerNameSet", tOwnerNameSet);
            try {
                tBigTag = tQuery.getSingleResult();
            } catch (Exception e) {
            }
        }
        return tBigTag;
    }

    public String toString() {
        return tagName;
    }

    /**
     * called only from list function, so return both BM tags and TW tags
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findBigTagEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM BigTag o ORDER BY o.id DESC", BigTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String getCommonTagName() {
		return commonTagName;
	}

	public void setCommonTagName(String commonTagName) {
		this.commonTagName = commonTagName;
	}
}
