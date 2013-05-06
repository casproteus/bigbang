package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	@Inject
	private MessageSource messageSource;
	
	void populateEditForm(Model uiModel, Content content, HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("content", content);
        List<BigTag> tCommontags = new ArrayList<BigTag>();
        tCommontags.addAll(BigTag.findBMTagsByPublisher("administrator", 0, 10));
        tCommontags.addAll(BigTag.findBMTagsByPublisher("admin", 0, 200));
        uiModel.addAttribute("bigtags", tCommontags);
        
        //tag in private space can be leave as null;
        List<BigTag> tList_Tag = new ArrayList<BigTag>();
        BigTag tTag = new BigTag(){public String toString(){return "";}};
        tTag.setId(Long.valueOf(-1));
        tList_Tag.add(tTag);
        

        String tCurName = UserAccount.findUserAccountByName(userContextService.getCurrentUserName()).getName();
        tList_Tag.addAll(BigTag.findBMTagsByPublisher(tCurName, 0, 1000));
        uiModel.addAttribute("mytags", tList_Tag);
        
        List<UserAccount> tList = new ArrayList<UserAccount>();
        tList.add(UserAccount.findUserAccountByName(tCurName)); //Can not use CurrentUser directly, because it's not of UserAccount type.
        uiModel.addAttribute("useraccounts", tList);		//why must return a list?
        uiModel.addAttribute("authorities",BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));
    }
	
	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "sortExpression", required = false) String sortExpression,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        
        if(tCurName == null)
        	return "login";

    	UserAccount tPublisher = UserAccount.findUserAccountByName(tCurName);
    	tCurName = tPublisher.getName();
        float nrOfPages;
    	if(tCurName.equals("admin")){
	        uiModel.addAttribute("contents", Content.findContentEntries(firstResult, sizeNo, sortExpression));
	        nrOfPages = (float) Content.countContents() / sizeNo;
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
	    	Set<Integer> tAuthSet = BigAuthority.getAuthSet(tPublisher, tPublisher);
	        uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, tAuthSet, firstResult, sizeNo, sortExpression));
	        nrOfPages = (float) Content.countContentsByPublisher(tPublisher, tAuthSet) / sizeNo;
    	}
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "contents/list";
    }

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Content content, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
        	if(bindingResult.getAllErrors().size() == 1 && content.getPublisher() == null){
        		 String tCurName = userContextService.getCurrentUserName();
        	     UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        	     content.setPublisher(tUserAccount);
        	}else{
        		populateEditForm(uiModel, content, httpServletRequest);
        		return "contents/create";
        	}
        }
        uiModel.asMap().clear();
        content.persist();
        return "redirect:/contents/" + encodeUrlPathSegment(content.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, new Content(), httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "contents/create";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Content content, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, content, httpServletRequest);
            return "contents/update";
        }
        uiModel.asMap().clear();
        content.merge();
        return "redirect:/contents/" + encodeUrlPathSegment(content.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, Content.findContent(id), httpServletRequest);
        return "contents/update";
    }
}
