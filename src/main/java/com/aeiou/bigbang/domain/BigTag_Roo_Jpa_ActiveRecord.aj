// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.BigTag;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect BigTag_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager BigTag.entityManager;
    
    public static final List<String> BigTag.fieldNames4OrderClauseFilter = java.util.Arrays.asList("tagName", "type", "authority", "owner", "twitterID", "twitterTitle", "twitterContent", "contentID", "contentTitle", "contentURL", "commonTagName");
    
    public static final EntityManager BigTag.entityManager() {
        EntityManager em = new BigTag().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long BigTag.countBigTags() {
        return entityManager().createQuery("SELECT COUNT(o) FROM BigTag o", Long.class).getSingleResult();
    }
    
    public static List<BigTag> BigTag.findAllBigTags() {
        return entityManager().createQuery("SELECT o FROM BigTag o", BigTag.class).getResultList();
    }
    
    public static List<BigTag> BigTag.findAllBigTags(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM BigTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, BigTag.class).getResultList();
    }
    
    public static BigTag BigTag.findBigTag(Long id) {
        if (id == null) return null;
        return entityManager().find(BigTag.class, id);
    }
    
    public static List<BigTag> BigTag.findBigTagEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
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
    public void BigTag.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void BigTag.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            BigTag attached = BigTag.findBigTag(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void BigTag.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void BigTag.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public BigTag BigTag.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        BigTag merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
