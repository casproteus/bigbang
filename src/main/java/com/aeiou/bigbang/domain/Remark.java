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

@Entity
@Configurable
public class Remark {

    @NotNull
    private String content;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date remarkTime;

    private Integer authority;

    @ManyToOne
    private Twitter remarkto;

    @Transient
    private int refresh_time;

    public int getRefresh_time() {
        return refresh_time;
    }

    public void setRefresh_time(
            int refresh_time) {
        this.refresh_time = refresh_time;
    }

    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByTwitter(
            Twitter pTwitter,
            Set<java.lang.Integer> pAuthSet,
            int firstResult,
            int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Remark AS o WHERE o.remarkto = :pTwitter and (o.authority in (:pAuthSet)) ORDER BY o.id DESC",
                Remark.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countRemarksByTwitter(
            Twitter pTwitter,
            Set<java.lang.Integer> pAuthSet) {
        TypedQuery<Long> tQuery = entityManager().createQuery(
                "SELECT COUNT(o) FROM Remark AS o WHERE o.remarkto = :pTwitter and (o.authority in (:pAuthSet))",
                Long.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        tQuery = tQuery.setParameter("pAuthSet", pAuthSet);
        return tQuery.getSingleResult();
    }

    /**
     * @called from RemarkController->list when not admin as logged user.
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Remark> findRemarkByPublisher(
            UserAccount pPublisher,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Remark> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Remark AS o WHERE o.publisher = :publisher ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Remark.class);
        tQuery = tQuery.setParameter("publisher", pPublisher);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countRemarkByPublisher(
            UserAccount pPublisher) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class)
                    .error("------received a null as param!(pPublisher is null)------Remark.countRemarkByPublisher()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager()
                    .createQuery("SELECT COUNT(o) FROM Remark AS o WHERE o.publisher = :pPublisher", Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            return tQuery.getSingleResult();
        }
    }

    @Override
    public String toString() {
        return content;
    }

    public static List<Remark> findOrderedRemarkEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        return entityManager().createQuery(
                "SELECT o FROM Remark o ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Remark.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<Remark> findRemarkEntries(
            int firstResult,
            int maxResults) {
        return entityManager().createQuery("SELECT o FROM Remark o ORDER BY o.id DESC", Remark.class)
                .setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }

	public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }

	public static Remark fromJsonToRemark(String json) {
        return new JSONDeserializer<Remark>()
        .use(null, Remark.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Remark> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<Remark> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<Remark> fromJsonArrayToRemarks(String json) {
        return new JSONDeserializer<List<Remark>>()
        .use("values", Remark.class).deserialize(json);
    }

	public String getContent() {
        return this.content;
    }

	public void setContent(String content) {
        this.content = content;
    }

	public UserAccount getPublisher() {
        return this.publisher;
    }

	public void setPublisher(UserAccount publisher) {
        this.publisher = publisher;
    }

	public Date getRemarkTime() {
        return this.remarkTime;
    }

	public void setRemarkTime(Date remarkTime) {
        this.remarkTime = remarkTime;
    }

	public Integer getAuthority() {
        return this.authority;
    }

	public void setAuthority(Integer authority) {
        this.authority = authority;
    }

	public Twitter getRemarkto() {
        return this.remarkto;
    }

	public void setRemarkto(Twitter remarkto) {
        this.remarkto = remarkto;
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

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("content", "publisher", "remarkTime", "authority", "remarkto", "refresh_time");

	public static final EntityManager entityManager() {
        EntityManager em = new Remark().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countRemarks() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Remark o", Long.class).getSingleResult();
    }

	public static List<Remark> findAllRemarks() {
        return entityManager().createQuery("SELECT o FROM Remark o", Remark.class).getResultList();
    }

	public static List<Remark> findAllRemarks(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Remark o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Remark.class).getResultList();
    }

	public static Remark findRemark(Long id) {
        if (id == null) return null;
        return entityManager().find(Remark.class, id);
    }

	public static List<Remark> findRemarkEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Remark o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Remark.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Remark attached = Remark.findRemark(this.id);
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
    public Remark merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Remark merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
