package com.aeiou.bigbang.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import com.aeiou.bigbang.util.BigUtil;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Entity
@Configurable
public class UserAccount {

    @NotNull
    @Column(unique = true)
    @Size(min = 2)
    private String name;

    private String email;

    @Size(min = 4)
    private String password;

    private String description;

    @Min(0L)
    @Max(9L)
    private int price;

    private String layout;

    @NotNull
    private int balance;

    private int theme;

    private int status;

    private int newMessageAmount;

    public static com.aeiou.bigbang.domain.UserAccount findUserAccountByName(
            String pUserName) {
        TypedQuery<UserAccount> tQuery = entityManager()
                .createQuery("SELECT o FROM UserAccount AS o WHERE UPPER(o.name) = UPPER(:tname)", UserAccount.class);
        tQuery = tQuery.setParameter("tname", pUserName);
        List<UserAccount> tList = tQuery.getResultList();
        if (tList != null && tList.size() == 1)
            return tList.get(0);
        else
            return null;
    }

    public static com.aeiou.bigbang.domain.UserAccount findUserAccountByNameAndPassword(
            String pUserNameAndPassword) {
        int p = pUserNameAndPassword.indexOf(BigUtil.SEP_ITEM);
        if (p < 0)
            return null;
        String pUserName = pUserNameAndPassword.substring(0, p);
        String pPassword = pUserNameAndPassword.substring(p);
        TypedQuery<UserAccount> tQuery = entityManager()
                .createQuery("SELECT o FROM UserAccount AS o WHERE UPPER(o.name) = UPPER(:tname)", UserAccount.class);
        tQuery = tQuery.setParameter("tname", pUserName);
        List<UserAccount> tList = tQuery.getResultList();
        if (tList != null && tList.size() == 1) {
            UserAccount tUserAccount = tList.get(0);
            if (tUserAccount.getPassword().equals(pPassword))
                return tUserAccount;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static List<com.aeiou.bigbang.domain.UserAccount> findOrderedUserAccountEntries(
            int firstResult,
            int maxResults,
            String sortExpression) {
        TypedQuery<UserAccount> tQuery = entityManager().createQuery(
                "SELECT o FROM UserAccount o ORDER BY "
                        + (sortExpression == null || sortExpression.length() < 1 ? "o.id DESC" : sortExpression),
                UserAccount.class);
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static List<com.aeiou.bigbang.domain.UserAccount> findUserAccountEntries(
            int firstResult,
            int maxResults) {
        TypedQuery<UserAccount> tQuery =
                entityManager().createQuery("SELECT o FROM UserAccount o ORDER BY o.id DESC", UserAccount.class);
        if (firstResult >= 0 && maxResults > 0)
            tQuery = tQuery.setFirstResult(firstResult).setMaxResults(maxResults);
        return tQuery.getResultList();
    }

    public static String toJsonArray(
            Collection<UserAccount> collection) {
        return new JSONSerializer().include("listento").exclude("*.class").serialize(collection);
    }

    /**
     */
    private String noteLayout;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<UserAccount> listento = new HashSet<UserAccount>();

	public String getName() {
        return this.name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public String getPassword() {
        return this.password;
    }

	public void setPassword(String password) {
        this.password = password;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public int getPrice() {
        return this.price;
    }

	public void setPrice(int price) {
        this.price = price;
    }

	public String getLayout() {
        return this.layout;
    }

	public void setLayout(String layout) {
        this.layout = layout;
    }

	public int getBalance() {
        return this.balance;
    }

	public void setBalance(int balance) {
        this.balance = balance;
    }

	public int getTheme() {
        return this.theme;
    }

	public void setTheme(int theme) {
        this.theme = theme;
    }

	public int getStatus() {
        return this.status;
    }

	public void setStatus(int status) {
        this.status = status;
    }

	public int getNewMessageAmount() {
        return this.newMessageAmount;
    }

	public void setNewMessageAmount(int newMessageAmount) {
        this.newMessageAmount = newMessageAmount;
    }

	public String getNoteLayout() {
        return this.noteLayout;
    }

	public void setNoteLayout(String noteLayout) {
        this.noteLayout = noteLayout;
    }

	public Set<UserAccount> getListento() {
        return this.listento;
    }

	public void setListento(Set<UserAccount> listento) {
        this.listento = listento;
    }

	public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }

	public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }

	public static UserAccount fromJsonToUserAccount(String json) {
        return new JSONDeserializer<UserAccount>()
        .use(null, UserAccount.class).deserialize(json);
    }

	public static Collection<UserAccount> fromJsonArrayToUserAccounts(String json) {
        return new JSONDeserializer<List<UserAccount>>()
        .use("values", UserAccount.class).deserialize(json);
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

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("name", "email", "password", "description", "price", "layout", "balance", "theme", "status", "newMessageAmount", "noteLayout", "listento");

	public static final EntityManager entityManager() {
        EntityManager em = new UserAccount().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countUserAccounts() {
        return entityManager().createQuery("SELECT COUNT(o) FROM UserAccount o", Long.class).getSingleResult();
    }

	public static List<UserAccount> findAllUserAccounts() {
        return entityManager().createQuery("SELECT o FROM UserAccount o", UserAccount.class).getResultList();
    }

	public static List<UserAccount> findAllUserAccounts(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UserAccount o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UserAccount.class).getResultList();
    }

	public static UserAccount findUserAccount(Long id) {
        if (id == null) return null;
        return entityManager().find(UserAccount.class, id);
    }

	public static List<UserAccount> findUserAccountEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UserAccount o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UserAccount.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            UserAccount attached = UserAccount.findUserAccount(this.id);
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
    public UserAccount merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        UserAccount merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
