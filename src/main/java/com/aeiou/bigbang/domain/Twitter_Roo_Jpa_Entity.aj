// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Twitter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

privileged aspect Twitter_Roo_Jpa_Entity {
    
    declare @type: Twitter: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long Twitter.id;
    
    @Version
    @Column(name = "version")
    private Integer Twitter.version;
    
    public Long Twitter.getId() {
        return this.id;
    }
    
    public void Twitter.setId(Long id) {
        this.id = id;
    }
    
    public Integer Twitter.getVersion() {
        return this.version;
    }
    
    public void Twitter.setVersion(Integer version) {
        this.version = version;
    }
    
}
