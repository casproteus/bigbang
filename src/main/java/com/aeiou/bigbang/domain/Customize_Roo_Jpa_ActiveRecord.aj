// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Customize;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Customize_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager Customize.entityManager;
    
    public static final EntityManager Customize.entityManager() {
        EntityManager em = new Customize().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Customize.countCustomizes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Customize o", Long.class).getSingleResult();
    }
    
    public static List<Customize> Customize.findAllCustomizes() {
        return entityManager().createQuery("SELECT o FROM Customize o", Customize.class).getResultList();
    }
    
    public static Customize Customize.findCustomize(Long id) {
        if (id == null) return null;
        return entityManager().find(Customize.class, id);
    }
    
    public static List<Customize> Customize.findCustomizeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Customize o", Customize.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void Customize.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void Customize.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Customize attached = Customize.findCustomize(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void Customize.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void Customize.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public Customize Customize.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Customize merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
