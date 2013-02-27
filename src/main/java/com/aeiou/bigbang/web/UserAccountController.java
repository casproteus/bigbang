package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;

@RequestMapping("/useraccounts")
@Controller
@RooWebScaffold(path = "useraccounts", formBackingObject = UserAccount.class)
public class UserAccountController {
	@Inject
	private UserContextService userContextService;
	
	@Inject
	private MessageSource messageSource;
	
	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
		UserAccount tUserAccount = new UserAccount();
		tUserAccount.setPrice(1);
		tUserAccount.setBalance(1000);
        populateEditForm(uiModel, tUserAccount);
        return "useraccounts/create";
    }
	
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid UserAccount userAccount, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, userAccount);
            return "useraccounts/create";
        }
        UserAccount tUserAccount = UserAccount.findUserAccountByName(userAccount.getName());
        userAccount.setBalance(1000);// the 1000 in view was lost when transfered from view to back end because the field was set as disabled.
        if(tUserAccount == null){
	        uiModel.asMap().clear();
	        userAccount.persist();
	        //give some default tags.
	        BigTag tBigTag1 = new BigTag();
	        tBigTag1.setTagName(messageSource.getMessage("admin_suggested_tag1", new Object[0], null));
	        tBigTag1.setType(userAccount.getName());
	        tBigTag1.setAuthority(0);
	        tBigTag1.persist();
	        
	        BigTag tBigTag2 = new BigTag();
	        tBigTag2.setTagName(messageSource.getMessage("admin_suggested_tag2", new Object[0], null));
	        tBigTag2.setType(userAccount.getName());
	        tBigTag2.setAuthority(0);
	        tBigTag2.persist();
	        
	        BigTag tBigTag3 = new BigTag();
	        tBigTag3.setTagName(messageSource.getMessage("admin_suggested_tag3", new Object[0], null));
	        tBigTag3.setType(userAccount.getName());
	        tBigTag3.setAuthority(0);
	        tBigTag3.persist();
	        
	        BigTag tBigTag4 = new BigTag();
	        tBigTag4.setTagName(messageSource.getMessage("admin_suggested_tag4", new Object[0], null));
	        tBigTag4.setType(userAccount.getName());
	        tBigTag4.setAuthority(0);
	        tBigTag4.persist();
	        
	        return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
        }else{
        	uiModel.addAttribute("create_error", "abc");
        	return "useraccounts/create";
        }
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	String tUserName = userContextService.getCurrentUserName();
        if(tUserName == null)
         	return "login";
        
        if(tUserName.equals("admin")){
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("useraccounts", UserAccount.findUserAccountEntries(firstResult, sizeNo));
            float nrOfPages = (float) UserAccount.countUserAccounts() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
    		List<UserAccount> tList = new ArrayList<UserAccount>();
    		tList.add(UserAccount.findUserAccountByName(tUserName));
            uiModel.addAttribute("useraccounts", tList);
            uiModel.addAttribute("maxPages", 1);
    	}
        return "useraccounts/list";
    }

}
