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

    @RequestMapping(value = "/{spaceOwner}", produces = "text/html")
    public String index(@PathVariable("spaceOwner") String spaceOwner, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	UserAccount tUser = UserAccount.findUserAccountByName(spaceOwner);
    	if(tUser == null)
    		return null;//TODO: add a page showing something like this account does not exist!
    	
        uiModel.addAttribute("spaceOwner", spaceOwner);
        uiModel.addAttribute("description", tUser.getDescription());

    	List<BigTag> tBigTags = BigTag.findTagsByOwner(spaceOwner); //will fetch not also the public tags.
    	//TODO give them ids.
    	for(int i = 0; i < tBigTags.size(); i++){
    		BigTag tTag = tBigTags.get(i);
    		if("admin".equals(tTag.getType()))						//because view will distinguish if a tag is public one or private one.
    			tTag.setTagName("Tag_Admin_" + tTag.getTagName()); 	//if it's public one, then will go to resource file look for String to disp.
    	}
        uiModel.addAttribute("bigTags", tBigTags);
        
        List<List> tContentLists = new ArrayList<List>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTagAndSpaceOwner(tBigTags.get(i), tUser, 8));
    	}
        uiModel.addAttribute("contents", tContentLists);
        
        return "public/index";
    }
}
