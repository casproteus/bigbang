// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Circle;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Circle_Roo_Jpa_ActiveRecord{

@PersistenceContext transient EntityManager Circle.entityManager;

public static final List<String>Circle.fieldNames4OrderClauseFilter=java.util.Arrays.asList("circleName","description","owner","createdDate","members");

public static final EntityManager Circle.entityManager(){EntityManager em=new Circle().entityManager;if(em==null)throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");return em;}

public static long Circle.countCircles(){return entityManager().createQuery("SELECT COUNT(o) FROM Circle o",Long.class).getSingleResult();}

public static List<Circle>Circle.findAllCircles(){return entityManager().createQuery("SELECT o FROM Circle o",Circle.class).getResultList();}

public static List<Circle>Circle.findAllCircles(String sortFieldName,String sortOrder){String jpaQuery="SELECT o FROM Circle o";if(fieldNames4OrderClauseFilter.contains(sortFieldName)){jpaQuery=jpaQuery+" ORDER BY "+sortFieldName;if("ASC".equalsIgnoreCase(sortOrder)||"DESC".equalsIgnoreCase(sortOrder)){jpaQuery=jpaQuery+" "+sortOrder;}}return entityManager().createQuery(jpaQuery,Circle.class).getResultList();}

public static Circle Circle.findCircle(Long id){if(id==null)return null;return entityManager().find(Circle.class,id);}

public static List<Circle>Circle.findCircleEntries(int firstResult,int maxResults){return entityManager().createQuery("SELECT o FROM Circle o",Circle.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();}

public static List<Circle>Circle.findCircleEntries(int firstResult,int maxResults,String sortFieldName,String sortOrder){String jpaQuery="SELECT o FROM Circle o";if(fieldNames4OrderClauseFilter.contains(sortFieldName)){jpaQuery=jpaQuery+" ORDER BY "+sortFieldName;if("ASC".equalsIgnoreCase(sortOrder)||"DESC".equalsIgnoreCase(sortOrder)){jpaQuery=jpaQuery+" "+sortOrder;}}return entityManager().createQuery(jpaQuery,Circle.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();}

@Transactional public void Circle.persist(){if(this.entityManager==null)this.entityManager=entityManager();this.entityManager.persist(this);}

@Transactional public void Circle.remove(){if(this.entityManager==null)this.entityManager=entityManager();if(this.entityManager.contains(this)){this.entityManager.remove(this);}else{Circle attached=Circle.findCircle(this.id);this.entityManager.remove(attached);}}

@Transactional public void Circle.flush(){if(this.entityManager==null)this.entityManager=entityManager();this.entityManager.flush();}

@Transactional public void Circle.clear(){if(this.entityManager==null)this.entityManager=entityManager();this.entityManager.clear();}

@Transactional public Circle Circle.merge(){if(this.entityManager==null)this.entityManager=entityManager();Circle merged=this.entityManager.merge(this);this.entityManager.flush();return merged;}

}
