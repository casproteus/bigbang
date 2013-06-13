package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigType;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/twitters")
@Controller
@RooWebScaffold(path = "twitters", formBackingObject = Twitter.class)
public class TwitterController {

	@Inject
	private UserContextService userContextService;

	@Inject
	private MessageSource messageSource;
	
	void populateEditForm(Model uiModel, Twitter twitter, HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("twitter", twitter);
        //addDateTimeFormatPatterns(uiModel);
        
        //tag can not be leave as null;
        List<BigTag> tList_Tag = new ArrayList<BigTag>();
        String tCurName = UserAccount.findUserAccountByName(userContextService.getCurrentUserName()).getName();
        tList_Tag.addAll(BigTag.findTWTagsByPublisher(tCurName));
        uiModel.addAttribute("mytags", tList_Tag);
        
        List<UserAccount> tList = new ArrayList<UserAccount>();
        tList.add(UserAccount.findUserAccountByName(tCurName)); //Can not use CurrentUser directly, because it's not of UserAccount type.
        uiModel.addAttribute("useraccounts", tList);			//why must return a list?
        uiModel.addAttribute("authorities",BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));
    }
	
	void populateEditForm_Tag(Model uiModel, BigTag bigTag, HttpServletRequest httpServletRequest) {
    	//@NOTE if any attribute duplicated with the @RequestMapping(params = "???"), will cause trouble---if we add an "abc" into it, it will be something like "abe,".
    	//uiModel.addAttribute("twitterTitle", bigTag.getTwitterTitle());
        uiModel.addAttribute("bigTag", bigTag);
        uiModel.addAttribute("authorities",BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));
	}
	
    public String createForm_Tag(Model uiModel, HttpServletRequest httpServletRequest, String twitterTitle, String twitterContent) {
    	BigTag bigTag = new BigTag();
    	bigTag.setTwitterTitle(twitterTitle);
    	bigTag.setTwitterContent(twitterContent);
    	populateEditForm_Tag(uiModel, bigTag, httpServletRequest);
        return "twitters/create_tag";
    }

    @RequestMapping(params = "twitle", produces = "text/html")
    public String createTag(@Valid BigTag bigTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	bigTag.setOwner(1);
    	String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        tCurName = tUserAccount.getName();	//because we allow user to login with capital characters
        bigTag.setType(tCurName);
        	
        uiModel.asMap().clear();
        bigTag.persist();
        
        //go on with creating the twitter---------------
        Twitter twitter = new Twitter();
        twitter.setTwtitle(bigTag.getTwitterTitle());
        twitter.setTwitent(bigTag.getTwitterContent());
        populateEditForm(uiModel, twitter, httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "twitters/create";
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Twitter twitter, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
		//if this tAddingTagFlag was set, then we will go to the creating BigTag page.
		//while we have to make it different than normal createForm request, because we need to remember the title and content user has input.
		if(StringUtils.isNotBlank(twitter.getAddingTagFlag()))
			return createForm_Tag(uiModel, httpServletRequest, twitter.getTwtitle(), twitter.getTwitent());
		
		if(twitter.getTwitent() == null || twitter.getTwitent().length() < 1){
            populateEditForm(uiModel, twitter, httpServletRequest);
            return "twitters/create";
		}
		//get his last twitter in db compare with it.
		String tCurName = userContextService.getCurrentUserName();
	    UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
		List<Twitter> tList = Twitter.findTwitterByPublisher(tUserAccount, BigAuthority.getAuthSet(tUserAccount, tUserAccount), 0, 1);
		
		if(tList != null && tList.size() > 0){
			Twitter tTwitter = tList.get(0); System.out.println("---" + twitter.getTwitent() + "---");
			if(twitter.getTwitent().equals(tTwitter.getTwitent())){
                populateEditForm(uiModel, twitter, httpServletRequest);
				return "twitters/create";
			}
		}
		
        if (bindingResult.hasErrors()) {
        	if(bindingResult.getAllErrors().size() == 2 && twitter.getPublisher() == null && twitter.getTwitDate() == null){
        	     twitter.setPublisher(tUserAccount);
        	     twitter.setTwitDate(new Date());
        	     if(twitter.getTwtitle() == null || twitter.getTwtitle().trim().length() == 0){
        	    	 String content = twitter.getTwitent();
        	    	 int tIdx = content.indexOf("<br />");
        	    	 if (tIdx > 0)
        	    		 content = content.substring(0, tIdx);
        	    	 content = content.length() > 30 ? content.substring(0, 30) : content;
        	    	 twitter.setTwtitle(content.trim());
        	     }
        	}else{
                populateEditForm(uiModel, twitter, httpServletRequest);
                return "twitters/create";
        	}
        }
        uiModel.asMap().clear();
        twitter.setLastupdate(new Date());
        twitter.persist();
        PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
        return tController.index(tUserAccount.getName(), -1, -1, uiModel);
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Twitter twitter, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
        	if(bindingResult.getAllErrors().size() == 2 && twitter.getPublisher() == null && twitter.getTwitDate() == null){
        		 String tCurName = userContextService.getCurrentUserName();
        	     UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        	     twitter.setPublisher(tUserAccount);
        	     twitter.setTwitDate(new Date());
        	     if(twitter.getTwtitle() == null || twitter.getTwtitle().trim().length() == 0){
        	    	 String content = twitter.getTwitent();
        	    	 int tIdx = content.indexOf("<br />");
        	    	 if (tIdx > 0)
        	    		 content = content.substring(0, tIdx);
        	    	 content = content.length() > 30 ? content.substring(0, 30) : content;
        	    	 twitter.setTwtitle(content);
        	     }
        	}else{
        		populateEditForm(uiModel, twitter, httpServletRequest);
        		return "twitters/update";
        	}
        }
        uiModel.asMap().clear();
        twitter.setLastupdate(new Date());
        twitter.merge();
        return "redirect:/twitters/" + encodeUrlPathSegment(twitter.getId().toString(), httpServletRequest);
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if(tCurName == null)
        	return "login";
        
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;

    	UserAccount tPublisher = UserAccount.findUserAccountByName(tCurName);
    	tCurName = tPublisher.getName();
        float nrOfPages;
    	if(tCurName.equals("admin")){
	        uiModel.addAttribute("twitters", Twitter.findTwitterEntries(firstResult, sizeNo));
	        nrOfPages = (float) Twitter.countTwitters() / sizeNo;
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
	    	Set<Integer> tAuthSet = BigAuthority.getAuthSet(tPublisher, tPublisher);
	        uiModel.addAttribute("twitters", Twitter.findTwitterByPublisher(tPublisher, tAuthSet, firstResult, sizeNo));
	        nrOfPages = (float) Twitter.countTwitterByPublisher(tPublisher, tAuthSet) / sizeNo;
    	}
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        addDateTimeFormatPatterns(uiModel);
        return "twitters/list";
    }

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, new Twitter(), httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "twitters/create";
    }
	
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, Twitter.findTwitter(id), httpServletRequest);
        return "twitters/update";
    }
}
