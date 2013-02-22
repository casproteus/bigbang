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
import com.aeiou.bigbang.util.SpringApplicationContext;

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
    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner);
    		if(tOwner == null){
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
            	Set<Integer> tAuthSet = BigAuthority.getAuthSet(userContextService.getCurrentUserName(), tOwner);
                uiModel.addAttribute("spaceOwner", spaceOwner);
            	uiModel.addAttribute("contents", Content.findContentsByTagAndSpaceOwner(tBigTag, tOwner, tAuthSet, firstResult, sizeNo));
            	float nrOfPages = (float) Content.countContentsByTagAndSpaceOwner(tBigTag, tOwner, tAuthSet) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }
        }

        uiModel.addAttribute("tag", tBigTag.getTagName());
        uiModel.addAttribute("tagId", tagId);
        return "public/list_more";
    }

    @RequestMapping(params = "publisher", produces = "text/html")
    public String listContentByPublisher(@RequestParam(value = "publisher", required = false) String pPublisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
		UserAccount tPublisher = UserAccount.findUserAccountByName(pPublisher);
		if(tPublisher == null){
			pPublisher = BigUtil.getUTFString(pPublisher);
			tPublisher = UserAccount.findUserAccountByName(pPublisher);
			if(tPublisher == null)
				return "";
		}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            Set<Integer> tAuthSet = BigAuthority.getAuthSet(userContextService.getCurrentUserName(), tPublisher);
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, tAuthSet, firstResult, sizeNo));
            uiModel.addAttribute("publisher", pPublisher);
            uiModel.addAttribute("price",tPublisher.getPrice());
            String tCurUserName = userContextService.getCurrentUserName();
            if(tCurUserName != null){
            	UserAccount tCurUser = UserAccount.findUserAccountByName(tCurUserName);
                uiModel.addAttribute("balance", tCurUser.getBalance());
            	if(pPublisher.equals(tCurUserName)){
        			uiModel.addAttribute("nothireable", "true");
        			uiModel.addAttribute("notfireable", "true");
            	}else if(tCurUser.getListento().contains(tPublisher)){
        			uiModel.addAttribute("nothireable", "true");
        		}else{
        			uiModel.addAttribute("notfireable", "true");
        		}
            }
            float nrOfPages = (float) Content.countContentsByPublisher(tPublisher, tAuthSet) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        }
        return "public/list_publisher";
    }
    
    /**
     * will give 1 month salary when click the hire button.
     * @param publisher
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "hire", produces = "text/html")
    public String hirePublisher(@RequestParam(value = "hire", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	
    	String tOwnerName = userContextService.getCurrentUserName();				// not logged in? to login page.
		if(tOwnerName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);		//make sure the publisher still exist.
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		int tSalary = tPublisher.getPrice();
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);			//in who's space right now?
		tOwner.getListento().add(tPublisher);
		tOwner.setBalance(tOwner.getBalance() - tSalary);
		tPublisher.setBalance(tPublisher.getBalance() + tSalary);
		//TODO: better should put them in a transaction.
		tOwner.persist();
		tPublisher.persist();
		
		PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
		return(tController.index(tOwnerName, page, size, uiModel));
    }
    
    /**
     * will give the guy one month salary to fire.
     * @param publisher
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
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
		
		int tSalary = tPublisher.getPrice();
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);			//in who's space right now?
		tOwner.getListento().remove(tPublisher);
		tOwner.setBalance(tOwner.getBalance() - tSalary);
		tPublisher.setBalance(tPublisher.getBalance() + tSalary);
		//TODO: better should put them in a transaction.
		tOwner.persist();
		tPublisher.persist();

		return(SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tOwnerName, page, size, uiModel));
    }
    
    /**
     * adjust the layout
     * @param publisher
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "relayouttype", produces = "text/html")
    public String relayout(@RequestParam(value = "relayouttype", required = true) String relayouttype, 
    		@RequestParam(value = "tagId", required = true) Long tagId, Model uiModel) {
    	
    	String tOwnerName = userContextService.getCurrentUserName();
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);
		//TODO:
		String tLayout = tOwner.getLayout();
		
		return (SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tOwnerName, 0, 8, uiModel));
    }
}
