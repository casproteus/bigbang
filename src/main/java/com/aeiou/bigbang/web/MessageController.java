package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
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
		
		UserAccount tReceiver = UserAccount.findUserAccountByName(pReceiverName);			//make sure the user exist
    	if(tReceiver == null){
    		pReceiverName = BigUtil.getUTFString(pReceiverName);
    		tReceiver = UserAccount.findUserAccountByName(pReceiverName); //bet it might still not UTF8 encoded.
    		if(tReceiver == null)
    			return null;
    	}
    	
		//TODO: Should make the check before submit.
		if(message.getContent() == null || message.getContent().length() < 1){
			populateEditForm(uiModel, message);
            return "message/create";
		}
		//get his last twitter in db compare with it.
		String tCurName = userContextService.getCurrentUserName();
	    UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
		List<Message> tList = Message.findMessageByPublisher(tReceiver, tUserAccount, 0, 1);
		//This is good, but not good enough, because when user press F5 after modifying a remark, and press back->back
		//will trick out the form to submit again, and then in this method, the content are different....
		//TODO: add a hidden field in From and save a token in it.then verify, if the token not there, then stop saving
		//http://stackoverflow.com/questions/2324931/duplicate-form-submission-in-spring
		if(tList != null && tList.size() > 0){
			Message tMsgInDB = tList.get(0);
			if(message.getContent().equals(tMsgInDB.getContent()) && (tMsgInDB.getPostTime().getHours() == new Date().getHours())){
				populateEditForm(uiModel, message);
				return "messages/create";
			}
		}
	
		if (bindingResult.hasErrors()) {
			if (bindingResult.getAllErrors().size() == 3 && message.getReceiver() == null) {
				message.setPublisher(tUserAccount);
				message.setReceiver(tReceiver);
				message.setPostTime(new Date());//add remark time when it's submitted.
			} else {
				populateEditForm(uiModel, message);
		        return "messages/create";
			}
        }
        uiModel.asMap().clear();
        message.persist();
        //update the new message number of in user account.
        int newMessageNumber = tReceiver.getNewMessageAmount();
        tReceiver.setNewMessageAmount(newMessageNumber+1);
        tReceiver.persist();
        
        PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
        return tController.index(pReceiverName, -1, -1, uiModel);
    }
	
	@RequestMapping(produces = "text/html")
    public String list(HttpSession session, @RequestParam(value = "sortExpression", required = false) String sortExpression, 
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        
        String tCurName = userContextService.getCurrentUserName();     
	    UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        if(tUserAccount == null)
        	return "login";
        tCurName = tUserAccount.getName();

    	UserAccount tReceiver = UserAccount.findUserAccountByName(tCurName);
    	tCurName = tReceiver.getName();
        float nrOfPages;
    	if(tCurName.equals("admin")){
	        uiModel.addAttribute("messages", Message.findMessageEntries(firstResult, sizeNo, sortExpression));
	        nrOfPages = (float) Message.countMessages() / sizeNo;

	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        addDateTimeFormatPatterns(uiModel);
	        return "messages/list";
    	}else{
	        uiModel.addAttribute("messages", Message.findMessageByReceiver(tUserAccount, firstResult, sizeNo));
	        nrOfPages = (float) Message.countMessagesByReceiver(tReceiver) / sizeNo;
	        //clear the Account of new message both in user account and session.
	        tUserAccount.setNewMessageAmount(0);
	        tUserAccount.persist();
	        session.setAttribute("newMessageAmount", 0);
	        
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        addDateTimeFormatPatterns(uiModel);
	        return "messages/Biglist";
    	}
    }
}
