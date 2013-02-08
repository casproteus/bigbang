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

@RequestMapping("/public")
@Controller
public class PublicController{

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("go to his big uncle's!");
    }
    
    /**
     * We have to use tag's ID, because different user can create tags with same name. 
     * if we match content with tag name, will cause mistake when clicking the "more" button from personal space. 
     * so we have to use tag's ID to match content.
     * @param tag
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(value = "/{tag}", produces = "text/html")
    public String showMore(@PathVariable("tag") String tag, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	List<BigTag> tBigTags = BigTag.findTagsByTypeName(tag);	//different tags can exist with same name but different owner.
    	
    	BigTag tBigTag = null;									//if tBigTag is set, that means we are listing all
    	for(int i = 0; i < tBigTags.size(); i++){
    		if("admin".equals(tBigTags.get(i).getType())){
    			tBigTag = tBigTags.get(i);
    			break;
    		}
    	}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contents", Content.findContentsByTag(tBigTag, sizeNo));
            float nrOfPages = (float) Content.countContentsByTag(tBigTag) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("contents", Content.findContentsByTag(tBigTag, -1));
        }
        return "public/list";
    }

    @RequestMapping(params = "publisher", produces = "text/html")
    public String listPublishedContentByPublisher(@RequestParam(value = "publisher", required = false) String publisher, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
		UserAccount tUser = UserAccount.findUserAccountByName(publisher);
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tUser, sizeNo));
            float nrOfPages = (float) Content.countContentsByPublisher(tUser) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        }
        return "public/list";
    }
    
    @RequestMapping(produces = "text/html")
    public String index(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel){
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contents", Content.findContentEntries(firstResult, sizeNo));
            float nrOfPages = (float) Content.countContents() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
        	List<BigTag> tBigTags = BigTag.findTagsByType(null); 
        	for(int i = 0; i < tBigTags.size(); i++){
        		BigTag a = tBigTags.get(i);
        		a.setTagName("Tag_Admin_" + a.getTagName());
        	}
            uiModel.addAttribute("bigTags", tBigTags);

            List<List> tContentLists = new ArrayList<List>();
        	for(int i = 0; i < tBigTags.size(); i++){
        		tContentLists.add(Content.findContentsByTag(tBigTags.get(i), 8));
        	}
            uiModel.addAttribute("contents", tContentLists);
        }
        return "public/index";
    }
}
