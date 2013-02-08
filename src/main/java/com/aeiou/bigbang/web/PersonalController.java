package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.UserAccount;

@RequestMapping("/")
@Controller
public class PersonalController{

    @RequestMapping(value = "/{accountName}", produces = "text/html")
    public String show(@PathVariable("accountName") String accountName, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	UserAccount tUser = UserAccount.findUserAccountByName(accountName);
    	if(tUser == null)
    		return null;//TODO: add a page showing something like this account does not exist!
    	
    	List<BigTag> tBigTags = BigTag.findTagsByType(accountName); //will fetch not also the public tags.
    	for(int i = 0; i < tBigTags.size(); i++){
    		BigTag a = tBigTags.get(i);
    		if("admin".equals(a.getType()))						//because view will distinguish if a tag is public one or private one.
    			a.setTagName("Tag_Admin_" + a.getTagName()); 	//if it's public one, then will go to resource file look for String to disp.
    	}
        uiModel.addAttribute("bigTags", tBigTags);
        uiModel.addAttribute("spaceOwner", accountName);
        uiModel.addAttribute("description", tUser.getDescription());
        
        List<List> tContentLists = new ArrayList<List>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTagAndPublisher(tBigTags.get(i), tUser, 8));
    	}
        uiModel.addAttribute("contents", tContentLists);
        
        return "public/index";
    }
}
