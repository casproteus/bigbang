// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import java.util.Date;

privileged aspect Remark_Roo_JavaBean {

    public String Remark.getContent() {
        return this.content;
    }

    public void Remark.setContent(
            String content) {
        this.content = content;
    }

    public UserAccount Remark.getPublisher() {
        return this.publisher;
    }

    public void Remark.setPublisher(
            UserAccount publisher) {
        this.publisher = publisher;
    }

    public Date Remark.getRemarkTime() {
        return this.remarkTime;
    }

    public void Remark.setRemarkTime(
            Date remarkTime) {
        this.remarkTime = remarkTime;
    }

    public Integer Remark.getAuthority() {
        return this.authority;
    }

    public void Remark.setAuthority(
            Integer authority) {
        this.authority = authority;
    }

    public Twitter Remark.getRemarkto() {
        return this.remarkto;
    }

    public void Remark.setRemarkto(
            Twitter remarkto) {
        this.remarkto = remarkto;
    }

}
