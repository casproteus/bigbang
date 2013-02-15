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
import com.aeiou.bigbang.util.BigUtil;

@RequestMapping("/public")
@Controller
public class PublicController{

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
        return "public/list";
    }

    @RequestMapping(params = "publisher", produces = "text/html")
    public String listContentByPublisher(@RequestParam(value = "publisher", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
		UserAccount tUser = UserAccount.findUserAccountByName(publisher);
		if(tUser == null){
			publisher = BigUtil.getUTFString(publisher);
			tUser = UserAccount.findUserAccountByName(publisher);
			if(tUser == null)
				return "";
		}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tUser, firstResult, sizeNo));
            uiModel.addAttribute("publisher", publisher);
            float nrOfPages = (float) Content.countContentsByPublisher(tUser) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        }
        return "public/list";
    }
    
}
