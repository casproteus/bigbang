package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
        uiModel.addAttribute("bigtags", BigTag.findTagsByPublisher("admin"));
        
        //tag in private space can be leave as null;
        List<BigTag> tList_Tag = new ArrayList<BigTag>();
        BigTag tTag = new BigTag(){public String toString(){return "";}};
        tTag.setId(Long.valueOf(-1));
        tList_Tag.add(tTag);        
        tList_Tag.addAll(BigTag.findTagsByPublisher(userContextService.getCurrentUserName()));
        uiModel.addAttribute("mytags", tList_Tag);
        
        List<UserAccount> tList = new ArrayList<UserAccount>();
        tList.add(UserAccount.findUserAccountByName(userContextService.getCurrentUserName())); //Can not use CurrentUser directly, because it's not of UserAccount type.
        uiModel.addAttribute("useraccounts", tList);		//why must return a list?
        uiModel.addAttribute("authoritys",BigAuthority.getAllOptions());
    }
}
