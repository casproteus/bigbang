// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.RssTwitter;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import java.util.Set;

privileged aspect RssTwitter_Roo_JavaBean {
    
    public Set<UserAccount> RssTwitter.getUseraccount() {
        return this.useraccount;
    }
    
    public void RssTwitter.setUseraccount(Set<UserAccount> useraccount) {
        this.useraccount = useraccount;
    }
    
    public Set<Twitter> RssTwitter.getTwitter() {
        return this.twitter;
    }
    
    public void RssTwitter.setTwitter(Set<Twitter> twitter) {
        this.twitter = twitter;
    }
    
}
