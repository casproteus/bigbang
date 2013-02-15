package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;

@RequestMapping("/bigtags")
@Controller
@RooWebScaffold(path = "bigtags", formBackingObject = BigTag.class)
public class BigTagController {
	@Inject
	private UserContextService userContextService;

	void populateEditForm(Model uiModel, BigTag bigTag) {
		uiModel.addAttribute("bigTag", bigTag);
    }
	
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid BigTag bigTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (StringUtils.isEmpty(bigTag.getTagName())) {
            populateEditForm(uiModel, bigTag);
            return "bigtags/create";
        }
        if(StringUtils.isEmpty(bigTag.getType())){
        	bigTag.setType(userContextService.getCurrentUserName());
        }
        uiModel.asMap().clear();
        bigTag.persist();
        return "redirect:/bigtags/" + encodeUrlPathSegment(bigTag.getId().toString(), httpServletRequest);
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        String tUserName = userContextService.getCurrentUserName();
        if(tUserName == null)
        	return "login";
        
		int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
       
        float nrOfPages;
    	if(tUserName.equals("admin")){
    		uiModel.addAttribute("bigtags", BigTag.findBigTagEntries(firstResult, sizeNo));
    		nrOfPages = (float) BigTag.countBigTags() / sizeNo;
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
	        uiModel.addAttribute("bigtags", BigTag.findTagsByPublisher(tUserName, firstResult, sizeNo));
	        nrOfPages = (float) BigTag.countTagsByPublisher(tUserName) / sizeNo;
    	}
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "bigtags/list";
    }
}
