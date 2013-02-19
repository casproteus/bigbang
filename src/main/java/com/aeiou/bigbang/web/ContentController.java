package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;

@RequestMapping("/contents")
@Controller
@RooWebScaffold(path = "contents", formBackingObject = Content.class)
public class ContentController {
	@Inject
	private UserContextService userContextService;
	
	void populateEditForm(Model uiModel, Content content) {
        uiModel.addAttribute("content", content);
        uiModel.addAttribute("bigtags", BigTag.findTagsByPublisher("admin", 0, 1000));
        
        //tag in private space can be leave as null;
        List<BigTag> tList_Tag = new ArrayList<BigTag>();
        BigTag tTag = new BigTag(){public String toString(){return "";}};
        tTag.setId(Long.valueOf(-1));
        tList_Tag.add(tTag);        
        tList_Tag.addAll(BigTag.findTagsByPublisher(userContextService.getCurrentUserName(), 0, 1000));
        uiModel.addAttribute("mytags", tList_Tag);
        
        List<UserAccount> tList = new ArrayList<UserAccount>();
        tList.add(UserAccount.findUserAccountByName(userContextService.getCurrentUserName())); //Can not use CurrentUser directly, because it's not of UserAccount type.
        uiModel.addAttribute("useraccounts", tList);		//why must return a list?
        uiModel.addAttribute("authorities",BigAuthority.getAllOptions());
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tUserName = userContextService.getCurrentUserName();
        
        if(tUserName == null)
        	return "login";
        
        float nrOfPages;
    	if(tUserName.equals("admin")){
	        uiModel.addAttribute("contents", Content.findContentEntries(firstResult, sizeNo));
	        nrOfPages = (float) Content.countContents() / sizeNo;
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
	    	UserAccount tPublisher = UserAccount.findUserAccountByName(tUserName);
	        uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, firstResult, sizeNo));
	        nrOfPages = (float) Content.countContentsByPublisher(tPublisher) / sizeNo;
    	}
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "contents/list";
    }
}
