// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.domain.UserAccountDataOnDemand;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;

privileged aspect UserAccountDataOnDemand_Roo_DataOnDemand {
    
    declare @type: UserAccountDataOnDemand: @Component;
    
    private Random UserAccountDataOnDemand.rnd = new SecureRandom();
    
    private List<UserAccount> UserAccountDataOnDemand.data;
    
    public UserAccount UserAccountDataOnDemand.getNewTransientUserAccount(int index) {
        UserAccount obj = new UserAccount();
        setBalance(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setLastLoginTime(obj, index);
        setLastReadMessage(obj, index);
        setLayout(obj, index);
        setName(obj, index);
        setPassword(obj, index);
        setPrice(obj, index);
        setStatus(obj, index);
        setTheme(obj, index);
        return obj;
    }
    
    public void UserAccountDataOnDemand.setBalance(UserAccount obj, int index) {
        int balance = index;
        obj.setBalance(balance);
    }
    
    public void UserAccountDataOnDemand.setDescription(UserAccount obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }
    
    public void UserAccountDataOnDemand.setEmail(UserAccount obj, int index) {
        String email = "foo" + index + "@bar.com";
        obj.setEmail(email);
    }
    
    public void UserAccountDataOnDemand.setLastLoginTime(UserAccount obj, int index) {
        Date lastLoginTime = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setLastLoginTime(lastLoginTime);
    }
    
    public void UserAccountDataOnDemand.setLastReadMessage(UserAccount obj, int index) {
        Date lastReadMessage = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setLastReadMessage(lastReadMessage);
    }
    
    public void UserAccountDataOnDemand.setLayout(UserAccount obj, int index) {
        String layout = "layout_" + index;
        obj.setLayout(layout);
    }
    
    public void UserAccountDataOnDemand.setName(UserAccount obj, int index) {
        String name = "name_" + index;
        obj.setName(name);
    }
    
    public void UserAccountDataOnDemand.setPassword(UserAccount obj, int index) {
        String password = "password_" + index;
        obj.setPassword(password);
    }
    
    public void UserAccountDataOnDemand.setPrice(UserAccount obj, int index) {
        int price = index;
        if (price < 0 || price > 9) {
            price = 9;
        }
        obj.setPrice(price);
    }
    
    public void UserAccountDataOnDemand.setStatus(UserAccount obj, int index) {
        int status = index;
        obj.setStatus(status);
    }
    
    public void UserAccountDataOnDemand.setTheme(UserAccount obj, int index) {
        int theme = index;
        obj.setTheme(theme);
    }
    
    public UserAccount UserAccountDataOnDemand.getSpecificUserAccount(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        UserAccount obj = data.get(index);
        Long id = obj.getId();
        return UserAccount.findUserAccount(id);
    }
    
    public UserAccount UserAccountDataOnDemand.getRandomUserAccount() {
        init();
        UserAccount obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return UserAccount.findUserAccount(id);
    }
    
    public boolean UserAccountDataOnDemand.modifyUserAccount(UserAccount obj) {
        return false;
    }
    
    public void UserAccountDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = UserAccount.findUserAccountEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'UserAccount' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<UserAccount>();
        for (int i = 0; i < 10; i++) {
            UserAccount obj = getNewTransientUserAccount(i);
            try {
                obj.persist();
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
    
}
