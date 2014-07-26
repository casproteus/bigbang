package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/messages")
@Controller
@RooWebScaffold(path = "messages", formBackingObject = Message.class)
@RooWebJson(jsonObject = Message.class)
public class MessageController {

    @Inject
    private UserContextService userContextService;

    @RequestMapping(params = "pReceiverName", produces = "text/html")
    public String createMessageForm(Model uiModel, @RequestParam(value = "pReceiverName", required = false) String pReceiverName,
    		HttpServletRequest httpServletRequest) {
        UserAccount pReceiver = UserAccount.findUserAccountByName(pReceiverName);
        if (pReceiver == null) {
            pReceiverName = BigUtil.getUTFString(pReceiverName);
            pReceiver = UserAccount.findUserAccountByName(pReceiverName);
            if (pReceiver == null) return null;
        }
        populateEditForm(uiModel, new Message(), httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        uiModel.addAttribute("receiverName", pReceiverName);
        return "messages/create";
    }

    @RequestMapping(params = "pReceiverName", method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Message message, BindingResult bindingResult, @RequestParam(value = "pReceiverName", required = false) String pReceiverName,
    		Model uiModel, HttpServletRequest httpServletRequest) {
        UserAccount tReceiver = UserAccount.findUserAccountByName(pReceiverName);
        if (tReceiver == null) {
            pReceiverName = BigUtil.getUTFString(pReceiverName);
            tReceiver = UserAccount.findUserAccountByName(pReceiverName);
            if (tReceiver == null) return null;
        }
        if (message.getContent() == null || message.getContent().length() < 1) {
            populateEditForm(uiModel, message, httpServletRequest);
            return "message/create";
        }
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        List<Message> tList = Message.findMessageByPublisher(tReceiver, tUserAccount, 0, 1);
        if (tList != null && tList.size() > 0) {
            Message tMsgInDB = tList.get(0);
            if (message.getContent().equals(tMsgInDB.getContent()) && (tMsgInDB.getPostTime().getHours() == new Date().getHours())) {
                populateEditForm(uiModel, message, httpServletRequest);
                return "messages/create";
            }
        }
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 3 && message.getReceiver() == null) {
                message.setPublisher(tUserAccount);
                message.setReceiver(tReceiver);
                message.setPostTime(new Date());
            } else {
                populateEditForm(uiModel, message, httpServletRequest);
                return "messages/create";
            }
        }
        uiModel.asMap().clear();
        message.persist();
        int newMessageNumber = tReceiver.getNewMessageAmount();
        tReceiver.setNewMessageAmount(newMessageNumber + 1);
        tReceiver.persist();
        PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
        return tController.index(pReceiverName, -1, -1, uiModel, httpServletRequest);
    }

    @RequestMapping(produces = "text/html")
    public String list(HttpSession session, @RequestParam(value = "sortExpression", required = false) String sortExpression, 
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,
    		Model uiModel, HttpServletRequest httpServletRequest) {
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        if (tUserAccount == null) return "login";
        tCurName = tUserAccount.getName();
        UserAccount tReceiver = UserAccount.findUserAccountByName(tCurName);
        tCurName = tReceiver.getName();
        float nrOfPages;
        if (tCurName.equals("admin")) {
            uiModel.addAttribute("messages", Message.findMessageEntries(firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Message.countMessages() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            addDateTimeFormatPatterns(uiModel);
            return "messages/list";
        } else {
            uiModel.addAttribute("messages", Message.findMessageByReceiver(tUserAccount, firstResult, sizeNo));
            nrOfPages = (float) Message.countMessagesByReceiver(tReceiver) / sizeNo;
            tUserAccount.setNewMessageAmount(0);
            tUserAccount.persist();
            session.setAttribute("newMessageAmount", 0);
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            addDateTimeFormatPatterns(uiModel);

            BigUtil.checkTheme(tReceiver, httpServletRequest);
            
            return "messages/Biglist";
        }
    }

	void populateEditForm(Model uiModel, Message message, HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("message", message);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
        
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);        
        BigUtil.checkTheme(tOwner, httpServletRequest);
    }
}
