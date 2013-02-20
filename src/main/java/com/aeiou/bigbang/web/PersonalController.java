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
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;

@RequestMapping("/")
@Controller
public class PersonalController{

	@Inject
	private UserContextService userContextService;
	
    @RequestMapping(value = "/{spaceOwner}", produces = "text/html")
    public String index(@PathVariable("spaceOwner") String spaceOwner, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	if(BigUtil.isSystemCommand(spaceOwner))										//secrete commands goes here.
    		 return "public/index";

    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);			//make sure the owner exist, and set the name on title
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner); //bet it might still not UTF8 encoded.
    		if(tOwner == null)
    			return null;//TODO: add a page showing something like this account does not exist!
    	}

		String tCurUserName = userContextService.getCurrentUserName();				//the current user.
		UserAccount tCurUser = UserAccount.findUserAccountByName(tCurUserName);
				
    	List<BigTag> tBigTags = new ArrayList<BigTag>();
    	tBigTags.addAll(BigTag.findTagsByOwner(spaceOwner)); 						//fetch out all tags of admin's, owner's and his team's.
    	List<Long> tTagIds = new ArrayList<Long>();									//then adjust it. @note: don't know if we can use AthenSet, because 
    	for(int i = 0; i < tBigTags.size(); i++){									//here, we need to compare the tag names, to avoid duplication.
    		BigTag tTag = tBigTags.get(i);
    		int tAuthority = tTag.getAuthority() == null ? 0 : tTag.getAuthority().intValue();
    		if(tCurUserName == null){								//not logged in
    			if(tAuthority == 0){
    				tTagIds.add(tTag.getId());
    			}else{
    				tBigTags.remove(i);
    			}
    		}else{													//has logged in
    			if(tCurUserName.equals(spaceOwner)){					//it's owner himself
    				tTagIds.add(tTag.getId());
    			}else if(tOwner.getListento().contains(tCurUser)){		//it's team member
	    			if(tAuthority == 2 || tAuthority == 0){
	    				tTagIds.add(tTag.getId());
	    			}else{	//TODO when we support "visible to specific person, should go on here.
	    				tBigTags.remove(i);
	    			}
    			}else{													//it's someone else
    				if(tAuthority == 0){
    					tTagIds.add(tTag.getId());
    				}else{
	    				tBigTags.remove(i);
	    			}
    			}
    		}
    	}

        List<List> tContentLists = new ArrayList<List>();							//prepare the contentList for each tag.
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTagAndSpaceOwner(tBigTags.get(i), tOwner, BigAuthority.getAuthSet(tCurUserName, tOwner), 0, 8));
    	}
    	
        uiModel.addAttribute("contents", tContentLists);
        uiModel.addAttribute("spaceOwner", spaceOwner);
        uiModel.addAttribute("description", tOwner.getDescription());
        uiModel.addAttribute("bigTags", tBigTags);
        uiModel.addAttribute("tagIds", tTagIds);
        
        return "public/index";
    }
}
