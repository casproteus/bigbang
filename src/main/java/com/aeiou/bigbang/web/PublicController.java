package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
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
public class PublicController {

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("go to his big uncle's!");
    }

    
    @RequestMapping(value = "/{tag}", produces = "text/html")
    public String show(@PathVariable("tag") String tag, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	List<BigTag> tBigTags = BigTag.findTagsByTypeName(tag);//tag can not be useraccount name, so if user are clicking a user name, the tBigtags must be null here.
    	if(tBigTags.isEmpty()){
    		UserAccount tUser = UserAccount.findUserAccountByName(tag);
	    	if (page != null || size != null) {
	            int sizeNo = size == null ? 20 : size.intValue();
	            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	            uiModel.addAttribute("contents", Content.findContentsByPublisher(tUser, sizeNo));
	            float nrOfPages = (float) Content.countContentsByPublisher(tUser) / sizeNo;
	            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        } else {
	            uiModel.addAttribute("contents", Content.findContentsByPublisher(tUser, -1));
	        }
    	}else{
	    	BigTag tBigTag = null;
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
        	List<BigTag> tBigTags = BigTag.findTagsByType("admin"); 
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
