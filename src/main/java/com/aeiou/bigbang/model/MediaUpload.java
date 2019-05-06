package com.aeiou.bigbang.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class MediaUpload {

    @NotNull
    @Size(max = 128)
    @Column(unique = true)
    private String filepath;

    @NotNull
    private long filesize;

    @NotNull
    private String contentType;

    private byte[] content;

    // to count how many pictures in database are using a filepath starts with this
    // string.
    public static long countMediaUploadsByKey(
            String pKey) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Long> tQuery = tEntityManager
                .createQuery("SELECT COUNT(o) FROM MediaUpload AS o WHERE o.filepath LIKE :pKey", Long.class);
        tQuery.setParameter("pKey", pKey + "%");
        long tCount = 0;
        try {
            tCount = tQuery.getSingleResult();
        } catch (Exception e) {
            tCount = 0;
        }
        return tCount;
    }

    public static MediaUpload findMediaByKey(
            String pKey) {
        EntityManager tEntityManager = entityManager();
        tEntityManager.setFlushMode(FlushModeType.COMMIT);
        MediaUpload tObeFR;
        TypedQuery<MediaUpload> tQuery = tEntityManager
                .createQuery("SELECT o FROM MediaUpload AS o WHERE o.filepath = :pKey", MediaUpload.class);
        tQuery = tQuery.setParameter("pKey", pKey);
        try {
            tObeFR = tQuery.getSingleResult();
        } catch (Exception e) {
            tObeFR = new MediaUpload();
            tObeFR.setFilepath(pKey);
        }
        return tObeFR;
    }

	public String getFilepath() {
        return this.filepath;
    }

	public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

	public long getFilesize() {
        return this.filesize;
    }

	public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

	public String getContentType() {
        return this.contentType;
    }

	public void setContentType(String contentType) {
        this.contentType = contentType;
    }

	public byte[] getContent() {
        return this.content;
    }

	public void setContent(byte[] content) {
        this.content = content;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("filepath", "filesize", "contentType", "content");

	public static final EntityManager entityManager() {
        EntityManager em = new MediaUpload().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countMediaUploads() {
        return entityManager().createQuery("SELECT COUNT(o) FROM MediaUpload o", Long.class).getSingleResult();
    }

	public static List<MediaUpload> findAllMediaUploads() {
        return entityManager().createQuery("SELECT o FROM MediaUpload o", MediaUpload.class).getResultList();
    }

	public static List<MediaUpload> findAllMediaUploads(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM MediaUpload o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, MediaUpload.class).getResultList();
    }

	public static MediaUpload findMediaUpload(Long id) {
        if (id == null) return null;
        return entityManager().find(MediaUpload.class, id);
    }

	public static List<MediaUpload> findMediaUploadEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM MediaUpload o", MediaUpload.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<MediaUpload> findMediaUploadEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM MediaUpload o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, MediaUpload.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            MediaUpload attached = MediaUpload.findMediaUpload(this.id);
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
    public MediaUpload merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        MediaUpload merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
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
