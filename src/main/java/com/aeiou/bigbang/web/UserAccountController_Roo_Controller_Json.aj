// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Synchronization;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.synchronization.SynchnizationManager;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

privileged aspect UserAccountController_Roo_Controller_Json {
    
    @RequestMapping(value = "/{id}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> UserAccountController.showJson(@PathVariable("id") Long id) {
        UserAccount userAccount = UserAccount.findUserAccount(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (userAccount == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(userAccount.toJson(), headers, HttpStatus.OK);
    }
    
    @RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> UserAccountController.listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        List<UserAccount> result = UserAccount.findAllUserAccounts();
        return new ResponseEntity<String>(UserAccount.toJsonArray(result), headers, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> UserAccountController.createFromJson(@RequestBody String json) {
        UserAccount userAccount = UserAccount.fromJsonToUserAccount(json);
        userAccount.persist();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> UserAccountController.createFromJsonArray(@RequestBody String json) {
    	SynchnizationManager tSyncManager = new SynchnizationManager();
    	if(json != null && json.length() > 0){
	        for (UserAccount userAccount: UserAccount.fromJsonArrayToUserAccounts(json)) {
	            userAccount.persist();
	        }
	        List<String> tList = new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class).deserialize(json);
			if(tList.size() == 6)
				tSyncManager.saveContentIntoLocalDB(tList);
    	}
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent(), headers, HttpStatus.OK);
    }
    

    @RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> UserAccountController.updateFromJson(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        UserAccount userAccount = UserAccount.fromJsonToUserAccount(json);
        if (userAccount.merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> UserAccountController.updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        for (UserAccount userAccount: UserAccount.fromJsonArrayToUserAccounts(json)) {
            if (userAccount.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> UserAccountController.deleteFromJson(@PathVariable("id") Long id) {
        UserAccount userAccount = UserAccount.findUserAccount(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        if (userAccount == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        userAccount.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
    
}
