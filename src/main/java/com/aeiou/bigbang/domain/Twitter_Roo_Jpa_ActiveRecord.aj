// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Twitter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Twitter_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager Twitter.entityManager;
    
    public static final List<String> Twitter.fieldNames4OrderClauseFilter = java.util.Arrays.asList("twitent", "twitDate", "publisher", "twittertag", "authority", "twtitle", "lastupdate", "addingTagFlag");
    
    public static final EntityManager Twitter.entityManager() {
        EntityManager em = new Twitter().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Twitter.countTwitters() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Twitter o", Long.class).getSingleResult();
    }
    
    public static List<Twitter> Twitter.findAllTwitters() {
        return entityManager().createQuery("SELECT o FROM Twitter o", Twitter.class).getResultList();
    }
    
    public static List<Twitter> Twitter.findAllTwitters(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Twitter o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Twitter.class).getResultList();
    }
    
    public static Twitter Twitter.findTwitter(Long id) {
        if (id == null) return null;
        return entityManager().find(Twitter.class, id);
    }
    
    public static List<Twitter> Twitter.findTwitterEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
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
    public void Twitter.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void Twitter.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Twitter attached = Twitter.findTwitter(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void Twitter.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void Twitter.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public Twitter Twitter.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Twitter merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
