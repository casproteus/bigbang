// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.UserAccount;
import java.util.Date;

privileged aspect Message_Roo_JavaBean {

    public UserAccount Message.getReceiver() {
        return this.receiver;
    }

    public void Message.setReceiver(
            UserAccount receiver) {
        this.receiver = receiver;
    }

    public UserAccount Message.getPublisher() {
        return this.publisher;
    }

    public void Message.setPublisher(
            UserAccount publisher) {
        this.publisher = publisher;
    }

    public String Message.getContent() {
        return this.content;
    }

    public void Message.setContent(
            String content) {
        this.content = content;
    }

    public Date Message.getPostTime() {
        return this.postTime;
    }

    public void Message.setPostTime(
            Date postTime) {
        this.postTime = postTime;
    }

    public int Message.getStatus() {
        return this.status;
    }

    public void Message.setStatus(
            int status) {
        this.status = status;
    }

}
