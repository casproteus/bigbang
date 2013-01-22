// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Circle;
import com.aeiou.bigbang.domain.CircleDataOnDemand;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect CircleDataOnDemand_Roo_DataOnDemand {
    
    declare @type: CircleDataOnDemand: @Component;
    
    private Random CircleDataOnDemand.rnd = new SecureRandom();
    
    private List<Circle> CircleDataOnDemand.data;
    
    @Autowired
    private UserAccountDataOnDemand CircleDataOnDemand.userAccountDataOnDemand;
    
    public Circle CircleDataOnDemand.getNewTransientCircle(int index) {
        Circle obj = new Circle();
        setCircleName(obj, index);
        setCreatedDate(obj, index);
        setDescription(obj, index);
        setOwner(obj, index);
        return obj;
    }
    
    public void CircleDataOnDemand.setCircleName(Circle obj, int index) {
        String circleName = "circleName_" + index;
        obj.setCircleName(circleName);
    }
    
    public void CircleDataOnDemand.setCreatedDate(Circle obj, int index) {
        Date createdDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreatedDate(createdDate);
    }
    
    public void CircleDataOnDemand.setDescription(Circle obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }
    
    public void CircleDataOnDemand.setOwner(Circle obj, int index) {
        UserAccount owner = userAccountDataOnDemand.getRandomUserAccount();
        obj.setOwner(owner);
    }
    
    public Circle CircleDataOnDemand.getSpecificCircle(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        Circle obj = data.get(index);
        Long id = obj.getId();
        return Circle.findCircle(id);
    }
    
    public Circle CircleDataOnDemand.getRandomCircle() {
        init();
        Circle obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return Circle.findCircle(id);
    }
    
    public boolean CircleDataOnDemand.modifyCircle(Circle obj) {
        return false;
    }
    
    public void CircleDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = Circle.findCircleEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'Circle' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Circle>();
        for (int i = 0; i < 10; i++) {
            Circle obj = getNewTransientCircle(i);
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
