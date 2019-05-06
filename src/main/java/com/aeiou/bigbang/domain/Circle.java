package com.aeiou.bigbang.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
public class Circle {

    @NotNull
    private String circleName;

    private String description;

    @NotNull
    @ManyToOne
    private UserAccount owner;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date createdDate;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<UserAccount> members = new HashSet<UserAccount>();

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }

	public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }

	public static Circle fromJsonToCircle(String json) {
        return new JSONDeserializer<Circle>()
        .use(null, Circle.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Circle> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<Circle> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<Circle> fromJsonArrayToCircles(String json) {
        return new JSONDeserializer<List<Circle>>()
        .use("values", Circle.class).deserialize(json);
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

	public String getCircleName() {
        return this.circleName;
    }

	public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public UserAccount getOwner() {
        return this.owner;
    }

	public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

	public Date getCreatedDate() {
        return this.createdDate;
    }

	public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

	public Set<UserAccount> getMembers() {
        return this.members;
    }

	public void setMembers(Set<UserAccount> members) {
        this.members = members;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("circleName", "description", "owner", "createdDate", "members");

	public static final EntityManager entityManager() {
        EntityManager em = new Circle().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countCircles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Circle o", Long.class).getSingleResult();
    }

	public static List<Circle> findAllCircles() {
        return entityManager().createQuery("SELECT o FROM Circle o", Circle.class).getResultList();
    }

	public static List<Circle> findAllCircles(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Circle o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Circle.class).getResultList();
    }

	public static Circle findCircle(Long id) {
        if (id == null) return null;
        return entityManager().find(Circle.class, id);
    }

	public static List<Circle> findCircleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Circle o", Circle.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<Circle> findCircleEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Circle o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Circle.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Circle attached = Circle.findCircle(this.id);
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
    public Circle merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Circle merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
