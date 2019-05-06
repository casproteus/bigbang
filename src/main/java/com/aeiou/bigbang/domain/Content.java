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
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
public class Content {

    @NotNull
    private String title;

    @NotNull
    private String sourceURL;

    private String conentCache;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @ManyToOne
    private BigTag commonBigTag;

    private Integer authority;

    @ManyToOne
    private BigTag uncommonBigTag;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date markDate;

    @Transient
    private String addingTagFlag;

    public static List<com.aeiou.bigbang.domain.Content> findContentsByTag(
            BigTag pBigTag,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Content AS o WHERE o.commonBigTag = :pBigTag and o.authority = 0 ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Content.class);
        tQuery = tQuery.setParameter("pBigTag", pBigTag);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByTag(
            BigTag pTag) {
        if (pTag == null) {
            LogFactory.getLog(Content.class).error("------received a null as param!------Content.countContentsByTag()");
            Thread.dumpStack();
            return 0;
        } else {
            return entityManager()
                    .createQuery("SELECT COUNT(o) FROM Content AS o WHERE o.commonBigTag = :pTag", Long.class)
                    .setParameter("pTag", pTag).getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<Content> tQuery = entityManager().createQuery(
                "SELECT o FROM Content o ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Content.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentsByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Content> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Content AS o WHERE o.publisher = :publisher and (o.authority in (:pAuthSet)) ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Content.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByPublisher(
            UserAccount pPublisher,
            Set<java.lang.Integer> pAuthSet) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param! pPublisher is null------Content.countContentsByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager().createQuery(
                    "SELECT COUNT(o) FROM Content AS o WHERE o.publisher = :pPublisher and (o.authority in (:pAuthSet))",
                    Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
            return tQuery.getSingleResult();
        }
    }

    public static List<com.aeiou.bigbang.domain.Content> findContentsByTagAndSpaceOwner(
            BigTag pTag,
            UserAccount pOwner,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tTeamSet = pOwner.getListento();
        TypedQuery<Content> tQuery = null;
        if (tTeamSet.isEmpty()) {
            String tTagName = pTag.getTagName();
            tQuery = tEntityManager.createQuery(
                    "SELECT o FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in (:pAuthSet)) ORDER BY "
                            + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                    Content.class);
            tQuery = tQuery.setParameter("tTagName", tTagName);
            tQuery = tQuery.setParameter("pOwner", pOwner);
        } else {
            String tTagName = pTag.getTagName();
            tQuery = tEntityManager.createQuery(
                    "SELECT o FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName and o.publisher = :pOwner and o.authority in (:pAuthSet)) or "
                            + "(o.uncommonBigTag.tagName = :tTagName and o.publisher in (:tTeamSet) and o.authority = 0) ORDER BY "
                            + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                    Content.class);
            tQuery = tQuery.setParameter("tTagName", tTagName);
            tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
        }
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countContentsByTagAndSpaceOwner(
            BigTag pTag,
            UserAccount pOwner,
            Set<java.lang.Integer> pAuthSet) {
        if (pTag == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param! pTag is null------Content.countContentsByTagAndSpaceOwner()");
            Thread.dumpStack();
            return 0;
        }
        EntityManager tEntityManager = entityManager();
        Set<UserAccount> tTeamSet = pOwner.getListento();
        TypedQuery<Long> tQuery = null;
        if ("admin".equals(pTag.getType())) {
            if (tTeamSet.isEmpty()) {
                tQuery = tEntityManager.createQuery(
                        "SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner) and (o.authority in (:pAuthSet))",
                        Long.class);
                tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner);
            } else {
                tQuery = tEntityManager.createQuery(
                        "SELECT COUNT(o) FROM Content AS o WHERE (o.commonBigTag = :pTag) and (o.publisher = :pOwner or o.publisher in (:tTeamSet)) and (o.authority in (:pAuthSet))",
                        Long.class);
                tQuery = tQuery.setParameter("pTag", pTag);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
            }
        } else {
            if (tTeamSet.isEmpty()) {
                String tTagName = pTag.getTagName();
                tQuery = tEntityManager.createQuery(
                        "SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner) and (o.authority in (:pAuthSet))",
                        Long.class);
                tQuery = tQuery.setParameter("tTagName", tTagName);
                tQuery = tQuery.setParameter("pOwner", pOwner);
            } else {
                String tTagName = pTag.getTagName();
                tQuery = tEntityManager.createQuery(
                        "SELECT COUNT(o) FROM Content AS o WHERE (o.uncommonBigTag.tagName = :tTagName) and (o.publisher = :pOwner or o.publisher in (:tTeamSet)) and (o.authority in (:pAuthSet))",
                        Long.class);
                tQuery = tQuery.setParameter("tTagName", tTagName);
                tQuery = tQuery.setParameter("pOwner", pOwner).setParameter("tTeamSet", tTeamSet);
            }
        }
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        return tQuery.getSingleResult();
    }

    public String getAddingTagFlag() {
        return addingTagFlag;
    }

    public void setAddingTagFlag(
            String addingTagFlag) {
        this.addingTagFlag = addingTagFlag;
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

	public static Content fromJsonToContent(String json) {
        return new JSONDeserializer<Content>()
        .use(null, Content.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Content> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<Content> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<Content> fromJsonArrayToContents(String json) {
        return new JSONDeserializer<List<Content>>()
        .use("values", Content.class).deserialize(json);
    }

	public String getTitle() {
        return this.title;
    }

	public void setTitle(String title) {
        this.title = title;
    }

	public String getSourceURL() {
        return this.sourceURL;
    }

	public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

	public String getConentCache() {
        return this.conentCache;
    }

	public void setConentCache(String conentCache) {
        this.conentCache = conentCache;
    }

	public UserAccount getPublisher() {
        return this.publisher;
    }

	public void setPublisher(UserAccount publisher) {
        this.publisher = publisher;
    }

	public BigTag getCommonBigTag() {
        return this.commonBigTag;
    }

	public void setCommonBigTag(BigTag commonBigTag) {
        this.commonBigTag = commonBigTag;
    }

	public Integer getAuthority() {
        return this.authority;
    }

	public void setAuthority(Integer authority) {
        this.authority = authority;
    }

	public BigTag getUncommonBigTag() {
        return this.uncommonBigTag;
    }

	public void setUncommonBigTag(BigTag uncommonBigTag) {
        this.uncommonBigTag = uncommonBigTag;
    }

	public Date getMarkDate() {
        return this.markDate;
    }

	public void setMarkDate(Date markDate) {
        this.markDate = markDate;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("title", "sourceURL", "conentCache", "publisher", "commonBigTag", "authority", "uncommonBigTag", "markDate", "addingTagFlag");

	public static final EntityManager entityManager() {
        EntityManager em = new Content().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countContents() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Content o", Long.class).getSingleResult();
    }

	public static List<Content> findAllContents() {
        return entityManager().createQuery("SELECT o FROM Content o", Content.class).getResultList();
    }

	public static List<Content> findAllContents(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Content o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Content.class).getResultList();
    }

	public static Content findContent(Long id) {
        if (id == null) return null;
        return entityManager().find(Content.class, id);
    }

	public static List<Content> findContentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Content o", Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<Content> findContentEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Content o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Content attached = Content.findContent(this.id);
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
    public Content merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Content merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
