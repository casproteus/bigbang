// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.UserAccount;

privileged aspect Customize_Roo_JavaBean {

    public String Customize.getCusKey() {
        return this.cusKey;
    }

    public void Customize.setCusKey(
            String cusKey) {
        this.cusKey = cusKey;
    }

    public String Customize.getCusValue() {
        return this.cusValue;
    }

    public void Customize.setCusValue(
            String cusValue) {
        this.cusValue = cusValue;
    }

    public UserAccount Customize.getUseraccount() {
        return this.useraccount;
    }

    public void Customize.setUseraccount(
            UserAccount useraccount) {
        this.useraccount = useraccount;
    }

}
