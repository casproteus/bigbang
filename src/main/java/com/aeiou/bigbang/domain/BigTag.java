package com.aeiou.bigbang.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
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
    private Long twitterID;

    @Transient
    private String twitterTitle;

    @Transient
    private String twitterContent;

    @Transient
    private Long contentID;

    @Transient
    private String contentTitle;

    @Transient
    private String contentURL;

    @Transient
    private String commonTagName;

    public Long getContentID() {
        return contentID;
    }

    public void setContentID(
            Long contentID) {
        this.contentID = contentID;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(
            String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentURL() {
        return contentURL;
    }

    public void setContentURL(
            String contentURL) {
        this.contentURL = contentURL;
    }

    public Long getTwitterID() {
        return twitterID;
    }

    public void setTwitterID(
            Long twitterID) {
        this.twitterID = twitterID;
    }

    public String getTwitterTitle() {
        return twitterTitle;
    }

    public void setTwitterTitle(
            String twitterTitle) {
        this.twitterTitle = twitterTitle;
    }

    public String getTwitterContent() {
        return twitterContent;
    }

    public void setTwitterContent(
            String twitterContent) {
        this.twitterContent = twitterContent;
    }

    /**
     * called when listing all created tags from bigtagController. called when the userlogin name changes from
     * UserAccountController. called when deleting a user account, all his tag should be also deleted, this can happen
     * only when running test script.
     * 
     * @param pUserAccount
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByPublisher(
            String pUserAccount,
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery(
                "SELECT o FROM BigTag AS o WHERE UPPER(o.type) = :type ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                BigTag.class);
        tQuery = tQuery.setParameter("type", pUserAccount.toUpperCase());
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    /**
     * called when listing all created tags from bigtagController.
     * 
     * @param pPublisher
     * @return
     */
    public static long countTagsByPublisher(
            String pPublisher) {
        TypedQuery<Long> tQuery = entityManager()
                .createQuery("SELECT COUNT(o) FROM BigTag o WHERE UPPER(o.type) = :pPublisher", Long.class);
        tQuery = tQuery.setParameter("pPublisher", pPublisher.toUpperCase());
        return tQuery.getSingleResult();
    }

    /**
     * called only by contentController.populateEditForm().
     * 
     * @param pUserAccount
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findBMTagsByPublisher(
            String pUserAccount,
            int firstResult,
            int maxResults) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery(
                "SELECT o FROM BigTag AS o WHERE LOWER(o.type) = :type and o.owner = 0 ORDER BY o.id DESC",
                BigTag.class);
        tQuery = tQuery.setParameter("type", pUserAccount.toLowerCase());
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    /**
     * called only by twitterController.populateEditForm().
     * 
     * @param userName
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findTWTagsByPublisher(
            String userName) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery(
                "SELECT o FROM BigTag AS o WHERE UPPER(o.type) = :type and o.owner = :owner ORDER BY o.id DESC",
                BigTag.class);
        tQuery = tQuery.setParameter("type", userName.toUpperCase()).setParameter("owner", 1).setFirstResult(0)
                .setMaxResults(1000);
        return tQuery.getResultList();
    }

    /**
     * @called from PublicController, personal Controller when they found anything wrong in the layout string,
     * @called from BigUtil.resetLayoutString
     * 
     * @param ownerName
     * @return common tags (which created by admin) or uncommon tags (which created by normal user and his friends)
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findTagsFromOwnerAndFriend(
            String ownerName,
            int type) {
        List<BigTag> listFR = new ArrayList<BigTag>();
        if (ownerName == null) {
            return null;
        }

        List<BigTag> tagListOfOwner = entityManager()
                .createQuery("SELECT o FROM BigTag AS o WHERE UPPER(o.type) = :type and o.owner = :owner", BigTag.class)
                .setParameter("type", ownerName.toUpperCase()).setParameter("owner", type).getResultList();

        List<String> listOfTagNames = new ArrayList<String>(); // used for checking if the tags name from friends
                                                               // are duplicated with tag of current user.
        for (BigTag bigTag : tagListOfOwner) {
            listFR.add(bigTag);
            listOfTagNames.add(bigTag.getTagName());
        }

        UserAccount owner = UserAccount.findUserAccountByName(ownerName);
        Object[] friends = owner.getListento().toArray();
        for (int i = 0; i < friends.length; i++) {
            owner = (UserAccount) friends[i];
            if ("admin".equals(owner.getName()))
                continue;
            List<BigTag> tagListOfFriend = entityManager().createQuery(
                    "SELECT o FROM BigTag AS o WHERE UPPER(o.type) = :type and o.owner = :owner AND o.authority = 0",
                    BigTag.class).setParameter("type", owner.getName().toUpperCase()).setParameter("owner", type)
                    .getResultList();
            for (BigTag bigTag : tagListOfFriend) {
                String tagName = bigTag.getTagName();
                if (!listOfTagNames.contains(tagName)) {
                    listFR.add(bigTag);
                    listOfTagNames.add(tagName);
                }
            }
        }
        return listFR;
    }

    // used by no one for now,
    private static List<BigTag> findBMAllTagsByOwner(
            String ownerName) {
        List<BigTag> tListFR = new ArrayList<BigTag>();
        tListFR.addAll(entityManager()
                .createQuery("SELECT o FROM BigTag AS o WHERE LOWER(o.type) = :type and o.owner = 0", BigTag.class)
                .setParameter("type", "admin").getResultList());
        if (ownerName == null || "admin".equals(ownerName)) {
            return tListFR;
        } else {
            List<BigTag> tTagListOfPublisher = entityManager()
                    .createQuery("SELECT o FROM BigTag AS o WHERE UPPER(o.type) = :type and o.owner = 0", BigTag.class)
                    .setParameter("type", ownerName.toUpperCase()).getResultList();
            List<String> tListOfTagNames = new ArrayList<String>(); // used for checking if the tags name from friends
                                                                    // are duplicated with tag of current user.
            for (int i = 0; i < tTagListOfPublisher.size(); i++) {
                tListFR.add(tTagListOfPublisher.get(i));
                tListOfTagNames.add(tTagListOfPublisher.get(i).getTagName());
            }
            UserAccount publisher = UserAccount.findUserAccountByName(ownerName);
            Object[] tPublishers = publisher.getListento().toArray();
            for (int i = 0; i < tPublishers.length; i++) {
                publisher = (UserAccount) tPublishers[i];
                if ("admin".equals(publisher.getName()))
                    continue;
                List<BigTag> tTagListOfListenedPublisher = entityManager().createQuery(
                        "SELECT o FROM BigTag AS o WHERE UPPER(o.type) = :type and o.owner = 0 AND o.authority = 0",
                        BigTag.class).setParameter("type", publisher.getName().toUpperCase()).getResultList();
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

    // used by no one for now, becuase when we genereate a string to save to layout
    // string, we need a BigTag Obect, use
    // it's authority and owner properties to know what marks we should add to front
    // and end.
    // while this is a good lession to show that we can use �select o.tagName ...."
    // to return a string list.
    private static List<String> findBMAllTagsStringByOwner(
            String pOwnerName) {
        List<String> tListFR = new ArrayList<String>();
        TypedQuery<String> tQ = entityManager().createQuery(
                "SELECT o.tagName FROM BigTag AS o WHERE LOWER(o.type) = :type and o.owner = 0", String.class);
        tQ.setParameter("type", "admin");
        tListFR.addAll(tQ.getResultList());

        List<String> tTagListOfPublisher = entityManager()
                .createQuery("SELECT o.tagName FROM BigTag AS o WHERE LOWER(o.type) = :type and o.owner = 0",
                        String.class)
                .setParameter("type", pOwnerName.toLowerCase()).getResultList();
        List<String> tListOfTagNames = new ArrayList<String>(); // used for checking if the tags name from friends are
                                                                // duplicated with tag of current user.
        for (int i = 0; i < tTagListOfPublisher.size(); i++) {
            tListFR.add(tTagListOfPublisher.get(i));
            tListOfTagNames.add(tTagListOfPublisher.get(i));
        }
        UserAccount tPublisher = UserAccount.findUserAccountByName(pOwnerName);
        Object[] tPublishers = tPublisher.getListento().toArray();
        for (int i = 0; i < tPublishers.length; i++) {
            tPublisher = (UserAccount) tPublishers[i];
            if ("admin".equals(tPublisher.getName()))
                continue;
            List<String> tTagListOfListenedPublisher = entityManager().createQuery(
                    "SELECT o.tagName FROM BigTag AS o WHERE LOWER(o.type) = :type and o.owner = 0 AND o.authority = 0",
                    String.class).setParameter("type", tPublisher.getName().toLowerCase()).getResultList();
            for (int j = 0; j < tTagListOfListenedPublisher.size(); j++) {
                String tTagName = tTagListOfListenedPublisher.get(j);
                if (!tListOfTagNames.contains(tTagName)) {
                    tListFR.add(tTagListOfListenedPublisher.get(j));
                    tListOfTagNames.add(tTagName);
                }
            }
        }
        return tListFR;
    }

    /**
     * will return owner's tag and owner's friends' tags, will not return admin's tags.
     * 
     * @param tagName
     * @param ownerName
     * @return
     */
    public static com.aeiou.bigbang.domain.BigTag findTagByNameAndOwner(
            String tagName,
            String ownerName) {
        tagName = tagName.trim();
        ownerName = ownerName.trim();
        if (tagName.length() < 1 || ownerName.length() < 1)
            return null;

        BigTag tBigTag = null;
        TypedQuery<BigTag> query = entityManager().createQuery(
                "SELECT o FROM BigTag AS o WHERE o.tagName = :tagName and LOWER(o.type) = :type", BigTag.class);
        query = query.setParameter("tagName", tagName);
        query = query.setParameter("type", ownerName.toLowerCase());

        try {
            tBigTag = query.getSingleResult();
        } catch (Exception e) {
            Set<String> tOwnerNameSet = new HashSet<String>();
            UserAccount tOwner = UserAccount.findUserAccountByName(ownerName);
            if (tOwner.getListento() != null) {
                Iterator<UserAccount> tList = tOwner.getListento().iterator();
                while (tList.hasNext())
                    tOwnerNameSet.add(tList.next().getName().toLowerCase());
                query = entityManager().createQuery(
                        "SELECT o FROM BigTag AS o WHERE o.tagName = :pTagName and LOWER(o.type) in (:tOwnerNameSet)",
                        BigTag.class);
                query = query.setParameter("pTagName", tagName).setParameter("tOwnerNameSet", tOwnerNameSet);
                try {
                    tBigTag = query.getSingleResult();
                } catch (Exception eeee) {
                    // do nothing.
                }
            }
        }
        return tBigTag;
    }

    @Override
    public String toString() {
        return tagName;
    }

    /**
     * called only from list function, so return both BM tags and TW tags
     * 
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findOrderedBigTagEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery(
                "SELECT o FROM BigTag o ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                BigTag.class);
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    /**
     * called only from list function, so return both BM tags and TW tags
     * 
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.BigTag> findBigTagEntries(
            int firstResult,
            int maxResults) {
        TypedQuery<BigTag> tQuery =
                entityManager().createQuery("SELECT o FROM BigTag o ORDER BY o.id DESC", BigTag.class);
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public String getCommonTagName() {
        return commonTagName;
    }

    public void setCommonTagName(
            String commonTagName) {
        this.commonTagName = commonTagName;
    }

	public String getTagName() {
        return this.tagName;
    }

	public void setTagName(String tagName) {
        this.tagName = tagName;
    }

	public String getType() {
        return this.type;
    }

	public void setType(String type) {
        this.type = type;
    }

	public Integer getAuthority() {
        return this.authority;
    }

	public void setAuthority(Integer authority) {
        this.authority = authority;
    }

	public Integer getOwner() {
        return this.owner;
    }

	public void setOwner(Integer owner) {
        this.owner = owner;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("tagName", "type", "authority", "owner", "twitterID", "twitterTitle", "twitterContent", "contentID", "contentTitle", "contentURL", "commonTagName");

	public static final EntityManager entityManager() {
        EntityManager em = new BigTag().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countBigTags() {
        return entityManager().createQuery("SELECT COUNT(o) FROM BigTag o", Long.class).getSingleResult();
    }

	public static List<BigTag> findAllBigTags() {
        return entityManager().createQuery("SELECT o FROM BigTag o", BigTag.class).getResultList();
    }

	public static List<BigTag> findAllBigTags(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM BigTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, BigTag.class).getResultList();
    }

	public static BigTag findBigTag(Long id) {
        if (id == null) return null;
        return entityManager().find(BigTag.class, id);
    }

	public static List<BigTag> findBigTagEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM BigTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, BigTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            BigTag attached = BigTag.findBigTag(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public BigTag merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        BigTag merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }

	public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }

	public static BigTag fromJsonToBigTag(String json) {
        return new JSONDeserializer<BigTag>()
        .use(null, BigTag.class).deserialize(json);
    }

	public static String toJsonArray(Collection<BigTag> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<BigTag> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<BigTag> fromJsonArrayToBigTags(String json) {
        return new JSONDeserializer<List<BigTag>>()
        .use("values", BigTag.class).deserialize(json);
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }
}
