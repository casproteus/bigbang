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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;

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
	        BigUtil.addDefaultUserTags(messageSource, userAccount.getName()); // If I open this, the local is OK, the server will report error when persisting the tags! 
	        
	        return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
        }else{
        	uiModel.addAttribute("create_error", "abc");
        	return "useraccounts/create";
        }
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid UserAccount userAccount, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, userAccount);
            return "useraccounts/update";
        }
        
        UserAccount tUserAccount = UserAccount.findUserAccount(userAccount.getId());
        if(!tUserAccount.getName().equals(userAccount.getName())){
	        List<BigTag> tBigTags = BigTag.findTagsByPublisher(tUserAccount.getName(), 0, 1000);
	        for(int i = tBigTags.size() - 1; i > -1 ; i--){
	        	tBigTags.get(i).setType(userAccount.getName());
	        	tBigTags.get(i).merge();
	        }
        }
        
        uiModel.asMap().clear();
        userAccount.merge();
        return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        UserAccount userAccount = UserAccount.findUserAccount(id);
        List<BigTag> tBigTags = BigTag.findTagsByPublisher(userAccount.getName(), 0, 1000);
        for(int i = tBigTags.size() - 1; i > -1 ; i--){
        	tBigTags.get(i).remove();
        }
        userAccount.remove();
        
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/useraccounts";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	String tCurName = userContextService.getCurrentUserName();
        if(tCurName == null)
         	return "login";
        
        if(tCurName.equalsIgnoreCase("admin")){
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("useraccounts", UserAccount.findUserAccountEntries(firstResult, sizeNo));
            float nrOfPages = (float) UserAccount.countUserAccounts() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
    		List<UserAccount> tList = new ArrayList<UserAccount>();
    		tList.add(UserAccount.findUserAccountByName(tCurName));
            uiModel.addAttribute("useraccounts", tList);
            uiModel.addAttribute("maxPages", 1);
    	}
        return "useraccounts/list";
    }

}
