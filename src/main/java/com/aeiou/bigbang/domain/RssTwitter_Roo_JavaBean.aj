// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.RssTwitter;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;

privileged aspect RssTwitter_Roo_JavaBean {
    
    public UserAccount RssTwitter.getUseraccount() {
        return this.useraccount;
    }
    
    public void RssTwitter.setUseraccount(UserAccount useraccount) {
        this.useraccount = useraccount;
    }
    
    public Twitter RssTwitter.getTwitter() {
        return this.twitter;
    }
    
    public void RssTwitter.setTwitter(Twitter twitter) {
        this.twitter = twitter;
    }
    
}
