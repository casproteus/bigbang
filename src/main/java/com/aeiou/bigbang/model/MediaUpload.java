package com.aeiou.bigbang.model;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
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
    
    //to count how many pictures in database are using a filepath starts with this string.
    public static long countMediaUploadsByKey(String pKey) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Long> tQuery = tEntityManager.createQuery("SELECT COUNT(o) FROM MediaUpload AS o WHERE o.filepath LIKE :pKey", Long.class);
        tQuery.setParameter("pKey", pKey + "%");
        long tCount = 0;
        try {
            tCount = tQuery.getSingleResult();
        } catch (Exception e) {
            tCount = 0;
        }
        return tCount;
    }

    public static MediaUpload findMediaByKey(String pKey) {
        EntityManager tEntityManager = entityManager();
        tEntityManager.setFlushMode(FlushModeType.COMMIT);
        MediaUpload tObeFR;
        TypedQuery<MediaUpload> tQuery = tEntityManager.createQuery("SELECT o FROM MediaUpload AS o WHERE o.filepath = :pKey", MediaUpload.class);
        tQuery = tQuery.setParameter("pKey", pKey);
        try {
            tObeFR = tQuery.getSingleResult();
        } catch (Exception e) {
            tObeFR = new MediaUpload();
            tObeFR.setFilepath(pKey);
        }
        return tObeFR;
    }
}
