// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import java.util.Date;

privileged aspect Twitter_Roo_JavaBean {
    
    public String Twitter.getTwitent() {
        return this.twitent;
    }
    
    public void Twitter.setTwitent(String twitent) {
        this.twitent = twitent;
    }
    
    public Date Twitter.getTwitDate() {
        return this.twitDate;
    }
    
    public void Twitter.setTwitDate(Date twitDate) {
        this.twitDate = twitDate;
    }
    
    public UserAccount Twitter.getPublisher() {
        return this.publisher;
    }
    
    public void Twitter.setPublisher(UserAccount publisher) {
        this.publisher = publisher;
    }
    
}
