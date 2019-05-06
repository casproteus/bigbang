package com.aeiou.bigbang.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
public class RssTwitter {

    @NotNull
    @ManyToOne
    private UserAccount useraccount = new UserAccount();

    @NotNull
    @ManyToOne
    private Twitter twitter = new Twitter();

    public static boolean isAllreadyExist(
            UserAccount pUserAccount,
            Twitter pTwitter) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<RssTwitter> tQuery = tEntityManager.createQuery(
                "SELECT o FROM RssTwitter AS o WHERE o.useraccount = :pUserAccount and o.twitter = :pTwitter",
                RssTwitter.class);
        tQuery = tQuery.setParameter("pUserAccount", pUserAccount);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        List<RssTwitter> tList = tQuery.getResultList();
        return tList != null && tList.size() > 0;
    }

    public static List<RssTwitter> findAllListenersByTwitter(
            Twitter pTwitter) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<RssTwitter> tQuery = tEntityManager
                .createQuery("SELECT o FROM RssTwitter AS o WHERE o.twitter = :pTwitter", RssTwitter.class);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        return tQuery.getResultList();
    }

    public static boolean deleteRssTwitterByTwitterAndUserAcount(
            Twitter pTwitter,
            UserAccount pUserAccount) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<RssTwitter> tQuery = tEntityManager.createQuery(
                "SELECT o FROM RssTwitter AS o WHERE o.useraccount = :pUserAccount and o.twitter = :pTwitter",
                RssTwitter.class);
        tQuery = tQuery.setParameter("pUserAccount", pUserAccount);
        tQuery = tQuery.setParameter("pTwitter", pTwitter);
        try {
            RssTwitter tRssTwitter = tQuery.getSingleResult();
            tRssTwitter.remove();
        } catch (Exception e) {
            // do nothing.
        }

        return true;
    }

	public UserAccount getUseraccount() {
        return this.useraccount;
    }

	public void setUseraccount(UserAccount useraccount) {
        this.useraccount = useraccount;
    }

	public Twitter getTwitter() {
        return this.twitter;
    }

	public void setTwitter(Twitter twitter) {
        this.twitter = twitter;
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

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("useraccount", "twitter");

	public static final EntityManager entityManager() {
        EntityManager em = new RssTwitter().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countRssTwitters() {
        return entityManager().createQuery("SELECT COUNT(o) FROM RssTwitter o", Long.class).getSingleResult();
    }

	public static List<RssTwitter> findAllRssTwitters() {
        return entityManager().createQuery("SELECT o FROM RssTwitter o", RssTwitter.class).getResultList();
    }

	public static List<RssTwitter> findAllRssTwitters(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM RssTwitter o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, RssTwitter.class).getResultList();
    }

	public static RssTwitter findRssTwitter(Long id) {
        if (id == null) return null;
        return entityManager().find(RssTwitter.class, id);
    }

	public static List<RssTwitter> findRssTwitterEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM RssTwitter o", RssTwitter.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<RssTwitter> findRssTwitterEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM RssTwitter o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, RssTwitter.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            RssTwitter attached = RssTwitter.findRssTwitter(this.id);
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
    public RssTwitter merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        RssTwitter merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
