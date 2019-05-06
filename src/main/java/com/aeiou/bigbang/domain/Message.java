package com.aeiou.bigbang.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class Message {

    @NotNull
    @ManyToOne
    private UserAccount receiver;

    @NotNull
    @ManyToOne
    private UserAccount publisher;

    @NotNull
    private String content;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date postTime;

    private int status;

    /**
     * @called from MessageController->list when not admin as logged user. for checking if the new submitted message is
     *         a duplicated one.
     * @param pPublisher
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<com.aeiou.bigbang.domain.Message> findMessageByPublisher(
            UserAccount pReceiver,
            UserAccount pSender,
            int firstResult,
            int maxResults) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Message> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Message AS o WHERE o.receiver = :pReceiver and o.publisher = :pPublisher ORDER BY o.id DESC",
                Message.class);
        tQuery = tQuery.setParameter("pReceiver", pReceiver);
        tQuery = tQuery.setParameter("pPublisher", pSender);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Message> findMessageByReceiver(
            UserAccount pReceiver,
            int firstResult,
            int maxResults,
            String sortExpression) {
        EntityManager tEntityManager = entityManager();
        TypedQuery<Message> tQuery = tEntityManager.createQuery(
                "SELECT o FROM Message AS o WHERE o.receiver = :pReceiver ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Message.class);
        tQuery = tQuery.setParameter("pReceiver", pReceiver);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.Message> findMessageEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<Message> tQuery = entityManager().createQuery(
                "SELECT o FROM Message o ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                Message.class);
        if (firstResult > -1 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static long countMessagesByReceiver(
            UserAccount pPublisher) {
        if (pPublisher == null) {
            LogFactory.getLog(Content.class).error(
                    "------received a null as param!(pPublisher is null)------Message.countMessagesByReceiver()");
            Thread.dumpStack();
            return 0;
        } else {
            TypedQuery<Long> tQuery = entityManager()
                    .createQuery("SELECT COUNT(o) FROM Message AS o WHERE o.receiver = :pPublisher", Long.class);
            tQuery = tQuery.setParameter("pPublisher", pPublisher);
            return tQuery.getSingleResult();
        }
    }

	public UserAccount getReceiver() {
        return this.receiver;
    }

	public void setReceiver(UserAccount receiver) {
        this.receiver = receiver;
    }

	public UserAccount getPublisher() {
        return this.publisher;
    }

	public void setPublisher(UserAccount publisher) {
        this.publisher = publisher;
    }

	public String getContent() {
        return this.content;
    }

	public void setContent(String content) {
        this.content = content;
    }

	public Date getPostTime() {
        return this.postTime;
    }

	public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

	public int getStatus() {
        return this.status;
    }

	public void setStatus(int status) {
        this.status = status;
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

	public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }

	public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }

	public static Message fromJsonToMessage(String json) {
        return new JSONDeserializer<Message>()
        .use(null, Message.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Message> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }

	public static String toJsonArray(Collection<Message> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }

	public static Collection<Message> fromJsonArrayToMessages(String json) {
        return new JSONDeserializer<List<Message>>()
        .use("values", Message.class).deserialize(json);
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("receiver", "publisher", "content", "postTime", "status");

	public static final EntityManager entityManager() {
        EntityManager em = new Message().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countMessages() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Message o", Long.class).getSingleResult();
    }

	public static List<Message> findAllMessages() {
        return entityManager().createQuery("SELECT o FROM Message o", Message.class).getResultList();
    }

	public static List<Message> findAllMessages(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Message o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Message.class).getResultList();
    }

	public static Message findMessage(Long id) {
        if (id == null) return null;
        return entityManager().find(Message.class, id);
    }

	public static List<Message> findMessageEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Message o", Message.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<Message> findMessageEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Message o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Message.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Message attached = Message.findMessage(this.id);
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
    public Message merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Message merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
