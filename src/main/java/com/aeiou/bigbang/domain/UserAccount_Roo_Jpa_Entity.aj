// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.UserAccount;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

privileged aspect UserAccount_Roo_Jpa_Entity {
    
    declare @type: UserAccount: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long UserAccount.id;
    
    @Version
    @Column(name = "version")
    private Integer UserAccount.version;
    
    public Long UserAccount.getId() {
        return this.id;
    }
    
    public void UserAccount.setId(Long id) {
        this.id = id;
    }
    
    public Integer UserAccount.getVersion() {
        return this.version;
    }
    
    public void UserAccount.setVersion(Integer version) {
        this.version = version;
    }
    
}
