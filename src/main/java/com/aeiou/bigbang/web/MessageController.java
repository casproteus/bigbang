package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/messages")
@Controller
@RooWebScaffold(path = "messages", formBackingObject = Message.class)
public class MessageController {

	@Inject
	private UserContextService userContextService;
	
	@RequestMapping(params = "pReceiverName", produces = "text/html")
    public String createMessageForm(Model uiModel, @RequestParam(value = "pReceiverName", required = false)String pReceiverName) {
    	UserAccount pReceiver = UserAccount.findUserAccountByName(pReceiverName);			//make sure the user exist
    	if(pReceiver == null){
    		pReceiverName = BigUtil.getUTFString(pReceiverName);
    		pReceiver = UserAccount.findUserAccountByName(pReceiverName); //bet it might still not UTF8 encoded.
    		if(pReceiver == null)
    			return null;
    	}
    	
        populateEditForm(uiModel, new Message());
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        uiModel.addAttribute("receiverName", pReceiverName);
        return "messages/create";
    }

	@RequestMapping(params = "pReceiverName", method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Message message, BindingResult bindingResult,
    		@RequestParam(value = "pReceiverName", required = false)String pReceiverName,
    		Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, message);
            return "messages/create";
        }
        uiModel.asMap().clear();
        message.persist();
        return "redirect:/messages/" + encodeUrlPathSegment(message.getId().toString(), httpServletRequest);
    }
	
	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("messages", Message.findMessageEntries(firstResult, sizeNo));
            float nrOfPages = (float) Message.countMessages() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("messages", Message.findAllMessages());
        }
        addDateTimeFormatPatterns(uiModel);
        return "messages/list";
    }
}
