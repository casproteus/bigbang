package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
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
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;

@RequestMapping("/")
@Controller
public class PersonalController{

	@Inject
	private UserContextService userContextService;
	
    @RequestMapping(value = "/{spaceOwner}", produces = "text/html")
    public String index(@PathVariable("spaceOwner") String spaceOwner, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner); //bet it might still not UTF8 encoded.
    		if(tOwner == null)
    			return null;//TODO: add a page showing something like this account does not exist!
    	}
        uiModel.addAttribute("spaceOwner", spaceOwner);
        uiModel.addAttribute("description", tOwner.getDescription());

		String tCurrentUserName = userContextService.getCurrentUserName();
		UserAccount tCurUser = UserAccount.findUserAccountByName(tCurrentUserName);
				
    	List<BigTag> tBigTags = new ArrayList<BigTag>();
    	tBigTags.addAll(BigTag.findTagsByOwner(spaceOwner)); //will fetch not also the public tags.
    	List<Long> tTagIds = new ArrayList<Long>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		BigTag tTag = tBigTags.get(i);
    		if(tCurrentUserName == null){					//not logged in
    			if(tTag.getAuthority() > 0){
    				tBigTags.remove(i);
    			}else{
    				tTagIds.add(tTag.getId());
    			}
    		}else{
    			if(tCurrentUserName.equals(spaceOwner)){	//it's owner himself
    				tTagIds.add(tTag.getId());
    				
    			}else if(tOwner.getListento().contains(tCurUser)){	//team					//it's team member
	    			if(tTag.getAuthority() == 2 || tTag.getAuthority() == 0){
	    				tTagIds.add(tTag.getId());
	    			}else{							//TODO when we support "visible to specific person, should go on here.
	    				tBigTags.remove(i);
	    			}
    			}else{										//some one else
    				if(tTag.getAuthority() == 0){
    					tTagIds.add(tTag.getId());
    				}else{
	    				tBigTags.remove(i);
	    			}
    			}
    		}
    		
    	}
    	
        uiModel.addAttribute("bigTags", tBigTags);
        uiModel.addAttribute("tagIds", tTagIds);

		Set<Short> tAuthSet = new HashSet<Short>();
    	tAuthSet.add(new Short((short)0));
    	if(tCurrentUserName.equals(spaceOwner)){
    		tAuthSet.add(new Short((short)1));
    		tAuthSet.add(new Short((short)2));
    		tAuthSet.add(new Short((short)3));
    	}else if(tOwner.getListento().contains(tCurUser)){
    		tAuthSet.add(new Short((short)2));
    	}else{//TODO: consider the case that visible to specific person.
    		
    	}
        List<List> tContentLists = new ArrayList<List>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTagAndSpaceOwner(tBigTags.get(i), tOwner, tAuthSet, 0, 8));
    	}
        uiModel.addAttribute("contents", tContentLists);
        
        return "public/index";
    }
}
