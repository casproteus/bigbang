// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Content;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Content_Roo_Jpa_ActiveRecord{

@PersistenceContext transient EntityManager Content.entityManager;

public static final List<String>Content.fieldNames4OrderClauseFilter=java.util.Arrays.asList("title","sourceURL","conentCache","publisher","commonBigTag","authority","uncommonBigTag","markDate","addingTagFlag");

public static final EntityManager Content.entityManager(){EntityManager em=new Content().entityManager;if(em==null)throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");return em;}

public static long Content.countContents(){return entityManager().createQuery("SELECT COUNT(o) FROM Content o",Long.class).getSingleResult();}

public static List<Content>Content.findAllContents(){return entityManager().createQuery("SELECT o FROM Content o",Content.class).getResultList();}

public static List<Content>Content.findAllContents(String sortFieldName,String sortOrder){String jpaQuery="SELECT o FROM Content o";if(fieldNames4OrderClauseFilter.contains(sortFieldName)){jpaQuery=jpaQuery+" ORDER BY "+sortFieldName;if("ASC".equalsIgnoreCase(sortOrder)||"DESC".equalsIgnoreCase(sortOrder)){jpaQuery=jpaQuery+" "+sortOrder;}}return entityManager().createQuery(jpaQuery,Content.class).getResultList();}

public static Content Content.findContent(Long id){if(id==null)return null;return entityManager().find(Content.class,id);}

public static List<Content>Content.findContentEntries(int firstResult,int maxResults){return entityManager().createQuery("SELECT o FROM Content o",Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();}

public static List<Content>Content.findContentEntries(int firstResult,int maxResults,String sortFieldName,String sortOrder){String jpaQuery="SELECT o FROM Content o";if(fieldNames4OrderClauseFilter.contains(sortFieldName)){jpaQuery=jpaQuery+" ORDER BY "+sortFieldName;if("ASC".equalsIgnoreCase(sortOrder)||"DESC".equalsIgnoreCase(sortOrder)){jpaQuery=jpaQuery+" "+sortOrder;}}return entityManager().createQuery(jpaQuery,Content.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();}

@Transactional public void Content.persist(){if(this.entityManager==null)this.entityManager=entityManager();this.entityManager.persist(this);}

@Transactional public void Content.remove(){if(this.entityManager==null)this.entityManager=entityManager();if(this.entityManager.contains(this)){this.entityManager.remove(this);}else{Content attached=Content.findContent(this.id);this.entityManager.remove(attached);}}

@Transactional public void Content.flush(){if(this.entityManager==null)this.entityManager=entityManager();this.entityManager.flush();}

@Transactional public void Content.clear(){if(this.entityManager==null)this.entityManager=entityManager();this.entityManager.clear();}

@Transactional public Content Content.merge(){if(this.entityManager==null)this.entityManager=entityManager();Content merged=this.entityManager.merge(this);this.entityManager.flush();return merged;}

}
