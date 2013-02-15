package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.userdetails.User;
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

@RequestMapping("/public")
@Controller
public class PublicController{
	
	@Inject
	private UserContextService userContextService;
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("go to his big uncle's!");
    }
    
    @RequestMapping(produces = "text/html")
    public String index( Model uiModel){
    	List<BigTag> tBigTags = BigTag.findTagsByOwner("admin"); 
    	List<Long> tTagIds = new ArrayList<Long>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		BigTag tTag = tBigTags.get(i);
    		tTagIds.add(tTag.getId());
    	}
        uiModel.addAttribute("bigTags", tBigTags);
        uiModel.addAttribute("tagIds", tTagIds);
        uiModel.addAttribute("spaceOwner", "admin");

        List<List> tContentLists = new ArrayList<List>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTag(tBigTags.get(i), 0, 8));
    	}
        uiModel.addAttribute("contents", tContentLists);
        return "public/index";
    }

    /**
     * We have to use both tag's tagname and type to match out a single tag, because different user can create tags with same name. 
     * if we match content with only tag name, will cause mistake when clicking the "more" button from personal space. 
     * so we have to use tag's ID to match content.
     * @param tag
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "spaceOwner", produces = "text/html")
    public String showMore(@RequestParam(value = "tagId", required = false) Long tagId, @RequestParam(value = "spaceOwner", required = false) String spaceOwner,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	BigTag tBigTag = BigTag.findBigTag(tagId);
    	UserAccount tUser = UserAccount.findUserAccountByName(spaceOwner);
    	if(tUser == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tUser = UserAccount.findUserAccountByName(spaceOwner);
    		if(tUser == null){
    			return "";
    		}
    	}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 25 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            if("admin".equals(spaceOwner) || "".equals(spaceOwner)){
                uiModel.addAttribute("spaceOwner", "admin");
            	uiModel.addAttribute("contents", Content.findContentsByTag(tBigTag, firstResult, sizeNo));
            	float nrOfPages = (float) Content.countContentsByTag(tBigTag) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }else{
                uiModel.addAttribute("spaceOwner", spaceOwner);
            	uiModel.addAttribute("contents", Content.findContentsByTagAndSpaceOwner(tBigTag, tUser, firstResult, sizeNo));
            	float nrOfPages = (float) Content.countContentsByTagAndSpaceOwner(tBigTag, tUser) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }
        }

        uiModel.addAttribute("tag", tBigTag.getTagName());
        uiModel.addAttribute("tagId", tagId);
        return "public/list_more";
    }

    @RequestMapping(params = "publisher", produces = "text/html")
    public String listContentByPublisher(@RequestParam(value = "publisher", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, firstResult, sizeNo));
            uiModel.addAttribute("publisher", publisher);
            uiModel.addAttribute("price", String.valueOf(tPublisher.getPrice()));
            String tOwnerName = userContextService.getCurrentUserName();
            if(tOwnerName != null){
            	UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);
        		if(tOwner.getListento().contains(tPublisher)){
        			uiModel.addAttribute("nothireable", "true");
        		}else{
        			uiModel.addAttribute("notfireable", "true");
        		}
            }
            float nrOfPages = (float) Content.countContentsByPublisher(tPublisher) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        }
        return "public/list_publisher";
    }
    
    @RequestMapping(params = "hire", produces = "text/html")
    public String hirePublisher(@RequestParam(value = "hire", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	
    	String tOwnerName = userContextService.getCurrentUserName();
		if(tOwnerName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);
		tOwner.getListento().add(tPublisher);
		tOwner.persist();
		
        uiModel.addAttribute("spaceOwner", tOwnerName);
        uiModel.addAttribute("description", tOwner.getDescription());

    	List<BigTag> tBigTags = BigTag.findTagsByOwner(tOwnerName); //will fetch not also the public tags.
    	List<Long> tTagIds = new ArrayList<Long>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		BigTag tTag = tBigTags.get(i);
    		tTagIds.add(tTag.getId());
    	}
        uiModel.addAttribute("bigTags", tBigTags);
        uiModel.addAttribute("tagIds", tTagIds);
        
        List<List> tContentLists = new ArrayList<List>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTagAndSpaceOwner(tBigTags.get(i), tOwner, 0, 8));
    	}
        uiModel.addAttribute("contents", tContentLists);
        
        return "public/index";
    }    
    
    @RequestMapping(params = "fire", produces = "text/html")
    public String firePublisher(@RequestParam(value = "fire", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	
    	String tOwnerName = userContextService.getCurrentUserName();
		if(tOwnerName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);
		tOwner.getListento().remove(tPublisher);
		tOwner.persist();
		
        uiModel.addAttribute("spaceOwner", tOwnerName);
        uiModel.addAttribute("description", tOwner.getDescription());

    	List<BigTag> tBigTags = BigTag.findTagsByOwner(tOwnerName); //will fetch not also the public tags.
    	List<Long> tTagIds = new ArrayList<Long>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		BigTag tTag = tBigTags.get(i);
    		tTagIds.add(tTag.getId());
    	}
        uiModel.addAttribute("bigTags", tBigTags);
        uiModel.addAttribute("tagIds", tTagIds);
        
        List<List> tContentLists = new ArrayList<List>();
    	for(int i = 0; i < tBigTags.size(); i++){
    		tContentLists.add(Content.findContentsByTagAndSpaceOwner(tBigTags.get(i), tOwner, 0, 8));
    	}
        uiModel.addAttribute("contents", tContentLists);
        
        return "public/index";
    }
}
