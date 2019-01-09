// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.model;

import com.aeiou.bigbang.model.MediaUpload;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect MediaUpload_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager MediaUpload.entityManager;
    
    public static final List<String> MediaUpload.fieldNames4OrderClauseFilter = java.util.Arrays.asList("filepath", "filesize", "contentType", "content");
    
    public static final EntityManager MediaUpload.entityManager() {
        EntityManager em = new MediaUpload().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long MediaUpload.countMediaUploads() {
        return entityManager().createQuery("SELECT COUNT(o) FROM MediaUpload o", Long.class).getSingleResult();
    }
    
    public static List<MediaUpload> MediaUpload.findAllMediaUploads() {
        return entityManager().createQuery("SELECT o FROM MediaUpload o", MediaUpload.class).getResultList();
    }
    
    public static List<MediaUpload> MediaUpload.findAllMediaUploads(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM MediaUpload o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, MediaUpload.class).getResultList();
    }
    
    public static MediaUpload MediaUpload.findMediaUpload(Long id) {
        if (id == null) return null;
        return entityManager().find(MediaUpload.class, id);
    }
    
    public static List<MediaUpload> MediaUpload.findMediaUploadEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM MediaUpload o", MediaUpload.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<MediaUpload> MediaUpload.findMediaUploadEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
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
    public void MediaUpload.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void MediaUpload.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            MediaUpload attached = MediaUpload.findMediaUpload(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void MediaUpload.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void MediaUpload.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public MediaUpload MediaUpload.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        MediaUpload merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
