package com.aeiou.bigbang.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class Twitter {

    @NotNull
    private String twitent;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date twitDate;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @ManyToOne
    private BigTag twittertag;

    private Integer authority;

    private String twtitle;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date lastupdate;

    @Transient
    private String addingTagFlag;

    /**
     * return the first twitter of the user, that means when we create a new account, we have to add a default twitter
     * automatically.
     * 
     * @note: didn't use Id, because users who already created twitter, will have trouble. use twitDate, I can create a
     *        twitter and modify it's date easyly to be before every every other twitter:)
     * @param pReceiver
     * @return
     */
    public static com.aeiou.bigbang.domain.Twitter findMessageTwitter(
            UserAccount pReceiver) {
        if (pReceiver == null)
            return null;
        TypedQuery<Twitter> tQuery = entityManager().createQuery(
                "SELECT o FROM Twitter o WHERE o.publisher = :publisher ORDER BY o.twitDate", Twitter.class);
        tQuery = tQuery.setParameter("publisher", pReceiver);
        return tQuery.getSingleResult();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findOrderedTwitterEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<Twitter> tQuery = entityManager().createQuery("SELECT o FROM Twitter o  ORDER BY "
                + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC" : sortExpression),
                Twitter.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterEntries(
            int firstResult,
            int maxResults) {
        TypedQuery<Twitter> tQuery =
                entityManager().createQuery("SELECT o FROM Twitter o  ORDER BY o.lastupdate DESC", Twitter.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Twitter AS o WHERE o.publisher = :publisher and (o.authority in (:pAuthSet)) ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                : sortExpression),
                Twitter.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countTwitterByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param!(pPublisher is null)------Twitter.countTwitterByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager().createQuery(
                    "SELECT COUNT(o) FROM Twitter AS o WHERE o.publisher = :pPublisher and (o.authority in (:pAuthSet))",
                    Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
            return tQuery.getSingleResult();
        }
    }

    /**
     * these twitter list will appear on personal space on the top right. it will display the twitter from owner's
     * friend which are setted as public. (while, I hope if the current user is logged in, and is friends of the author
     * of some twitter, the twitter should be displayed? is that possible? )
     */
    public static List<com.aeiou.bigbang.domain.Twitter> findTwitterByOwner(
            Set<UserAccount> tTeamSet,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        if (tTeamSet.isEmpty())
            return null;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Twitter AS o WHERE (o.publisher in (:tTeamSet)) and (o.authority in (:pAuthSet)) ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                : sortExpression),
                Twitter.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countTwittersByOwner(
            Set<UserAccount> tTeamSet,
            Set<java.lang.Integer> pAuthSet) {
        if (tTeamSet.isEmpty())
            return 0;
        EntityManager tEntityManager = entityManager();
        TypedQuery<Long> tQuery = tEntityManager.createQuery(
                "SELECT COUNT(o) FROM Twitter AS o WHERE (o.publisher in (:tTeamSet)) and (o.authority in (:pAuthSet))",
                Long.class);
        tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        return tQuery.getSingleResult();
    }

    public static List<com.aeiou.bigbang.domain.Twitter> findTwittersByTagAndSpaceOwner(
            BigTag pTag,
            UserAccount pOwner,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {

        Set<UserAccount> tTeamSet = pOwner.getListento();
        String tTagName = pTag.getTagName();
        EntityManager tEntityManager = entityManager();
        TypedQuery<Twitter> tQuery = null;
        if (tTeamSet.isEmpty()) {
            tQuery = tEntityManager.createQuery(
                    "SELECT o FROM Twitter AS o WHERE (o.twittertag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in (:pAuthSet)) ORDER BY "
                            + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                    : sortExpression),
                    Twitter.class);
            tQuery = tQuery.setParameter("tTagName", tTagName);
            tQuery = tQuery.setParameter("pOwner", pOwner);
        } else {
            tQuery = tEntityManager.createQuery(
                    "SELECT o FROM Twitter AS o WHERE (o.twittertag.tagName = :tTagName and o.publisher = :pOwner and o.authority in (:pAuthSet)) or "
                            + "(o.twittertag.tagName = :tTagName and o.publisher in (:tTeamSet) and o.authority = 0) ORDER BY "
                            + (sortExpression == null || sortExpression.length() < 1 ? "o.lastupdate DESC"
                                    : sortExpression),
                    Twitter.class);
            tQuery = tQuery.setParameter("tTagName", tTagName);
            tQuery = tQuery.setParameter("pOwner", pOwner);
            tQuery = tQuery.setParameter("tTeamSet", tTeamSet);
        }

        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public String getAddingTagFlag() {
        return addingTagFlag;
    }

    public void setAddingTagFlag(
            String addingTagFlag) {
        this.addingTagFlag = addingTagFlag;
    }

    @Override
    public String toString() {
        return twtitle;
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

	public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }

	public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }

	public static Twitter fromJsonToTwitter(String json) {
        return new JSONDeserializer<Twitter>()
        .use(null, Twitter.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Twitter> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<Twitter> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<Twitter> fromJsonArrayToTwitters(String json) {
        return new JSONDeserializer<List<Twitter>>()
        .use("values", Twitter.class).deserialize(json);
    }

	public String getTwitent() {
        return this.twitent;
    }

	public void setTwitent(String twitent) {
        this.twitent = twitent;
    }

	public Date getTwitDate() {
        return this.twitDate;
    }

	public void setTwitDate(Date twitDate) {
        this.twitDate = twitDate;
    }

	public UserAccount getPublisher() {
        return this.publisher;
    }

	public void setPublisher(UserAccount publisher) {
        this.publisher = publisher;
    }

	public BigTag getTwittertag() {
        return this.twittertag;
    }

	public void setTwittertag(BigTag twittertag) {
        this.twittertag = twittertag;
    }

	public Integer getAuthority() {
        return this.authority;
    }

	public void setAuthority(Integer authority) {
        this.authority = authority;
    }

	public String getTwtitle() {
        return this.twtitle;
    }

	public void setTwtitle(String twtitle) {
        this.twtitle = twtitle;
    }

	public Date getLastupdate() {
        return this.lastupdate;
    }

	public void setLastupdate(Date lastupdate) {
        this.lastupdate = lastupdate;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("twitent", "twitDate", "publisher", "twittertag", "authority", "twtitle", "lastupdate", "addingTagFlag");

	public static final EntityManager entityManager() {
        EntityManager em = new Twitter().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countTwitters() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Twitter o", Long.class).getSingleResult();
    }

	public static List<Twitter> findAllTwitters() {
        return entityManager().createQuery("SELECT o FROM Twitter o", Twitter.class).getResultList();
    }

	public static List<Twitter> findAllTwitters(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Twitter o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Twitter.class).getResultList();
    }

	public static Twitter findTwitter(Long id) {
        if (id == null) return null;
        return entityManager().find(Twitter.class, id);
    }

	public static List<Twitter> findTwitterEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Twitter o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Twitter.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Twitter attached = Twitter.findTwitter(this.id);
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
    public Twitter merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Twitter merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
