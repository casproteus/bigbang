package com.aeiou.bigbang.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class BigTag {

    @NotNull
    @Column(unique = true)
    @Size(min = 2)
    private String tagName;

    @NotNull
    @Size(min = 2)
    private String type;

    private Short authority;

    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByUser(String pUserAccount) {
        return entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", pUserAccount).getResultList();
    }

    public static List<com.aeiou.bigbang.domain.BigTag> findTagsByOwner(String pUserAccount) {
        List<BigTag> tList = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", "admin").getResultList();
        if (pUserAccount == null || "admin".equals(pUserAccount)) {
            return tList;
        } else {
            List<BigTag> tListFR = new ArrayList<BigTag>();
            tListFR.addAll(tList);
            tListFR.addAll(entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.type = :type", BigTag.class).setParameter("type", pUserAccount).getResultList());
            return tListFR;
        }
    }

    public static com.aeiou.bigbang.domain.BigTag findTagByTypeNameAndOwner(String pTagName, String pOwnerName) {
        TypedQuery<BigTag> tQuery = entityManager().createQuery("SELECT o FROM BigTag AS o WHERE o.tagName = :pTagName And o.type = :pOwnerName", BigTag.class);
        tQuery = tQuery.setParameter("pTagName", pTagName).setParameter("pOwnerName", pOwnerName);
        return tQuery.getSingleResult();
    }

    public String toString() {
        return tagName;
    }

    public static List<com.aeiou.bigbang.domain.BigTag> findBigTagEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM BigTag o ORDER BY o.id DESC", BigTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
