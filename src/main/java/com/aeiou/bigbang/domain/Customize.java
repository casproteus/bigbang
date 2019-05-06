package com.aeiou.bigbang.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class Customize {

    @NotNull
    @Column
    private String cusKey;

    @NotNull
    @Column
    private String cusValue;

    public static Customize findCustomizeByKey(
            String pCusKey) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Customize> tQuery =
                tEntityManager.createQuery("SELECT o FROM Customize AS o WHERE o.cusKey = :pCusKey", Customize.class);
        tQuery = tQuery.setParameter("pCusKey", pCusKey);
        try {
            return tQuery.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Customize> findCustomizesByOwner(
            UserAccount pOwner) {
        if (pOwner == null) {
            pOwner = UserAccount.findUserAccountByName("admin");
        }
        EntityManager tEntityManager = entityManager();
        TypedQuery<Customize> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Customize AS o WHERE o.useraccount = :owner ORDER BY o.cusKey", Customize.class);
        tQuery = tQuery.setParameter("owner", pOwner);
        try {
            return tQuery.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Customize> findCustomizeEntriesByOwner(
            int firstResult,
            int maxResults,
            String sortFieldName,
            String sortOrder,
            UserAccount userAccount) {

        String jpaQuery = "SELECT o FROM Customize o WHERE o.useraccount = " + userAccount.getName();
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<Customize> typedQuery = entityManager().createQuery(jpaQuery, Customize.class)
                .setFirstResult(firstResult).setMaxResults(maxResults);
        List<Customize> customizes = new ArrayList<Customize>();
        try {
            customizes = typedQuery.getResultList();
        } catch (Exception e) {
            // there's no matching record, just ignore it, return empty arrayList.
        } finally {
            return customizes;
        }
    }

    public static List<Customize> findCustomizeEntries(
            int firstResult,
            int maxResults,
            String sortFieldName,
            String sortOrder) {
        String jpaQuery = "SELECT o FROM Customize o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Customize.class).setFirstResult(firstResult)
                .setMaxResults(maxResults).getResultList();
    }

    public static List<Customize> findAllCustomizes(
            String sortFieldName,
            String sortOrder) {
        String jpaQuery = "SELECT o FROM Customize o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY o." + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Customize.class).getResultList();
    }

    /**
     */
    @ManyToOne
    private UserAccount useraccount;


	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("cusKey", "cusValue", "useraccount");

	public static final EntityManager entityManager() {
        EntityManager em = new Customize().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countCustomizes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Customize o", Long.class).getSingleResult();
    }

	public static List<Customize> findAllCustomizes() {
        return entityManager().createQuery("SELECT o FROM Customize o", Customize.class).getResultList();
    }

	public static Customize findCustomize(Long id) {
        if (id == null) return null;
        return entityManager().find(Customize.class, id);
    }

	public static List<Customize> findCustomizeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Customize o", Customize.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Customize attached = Customize.findCustomize(this.id);
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
    public Customize merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Customize merged = this.entityManager.merge(this);
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

	public static Customize fromJsonToCustomize(String json) {
        return new JSONDeserializer<Customize>()
        .use(null, Customize.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Customize> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<Customize> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<Customize> fromJsonArrayToCustomizes(String json) {
        return new JSONDeserializer<List<Customize>>()
        .use("values", Customize.class).deserialize(json);
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getCusKey() {
        return this.cusKey;
    }

	public void setCusKey(String cusKey) {
        this.cusKey = cusKey;
    }

	public String getCusValue() {
        return this.cusValue;
    }

	public void setCusValue(String cusValue) {
        this.cusValue = cusValue;
    }

	public UserAccount getUseraccount() {
        return this.useraccount;
    }

	public void setUseraccount(UserAccount useraccount) {
        this.useraccount = useraccount;
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
