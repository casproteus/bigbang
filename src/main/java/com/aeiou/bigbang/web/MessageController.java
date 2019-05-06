package com.aeiou.bigbang.web;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/messages")
@Controller
public class MessageController {

    @Inject
    private UserContextService userContextService;

    @RequestMapping(params = "pReceiverName", produces = "text/html")
    public String createMessageForm(
            Model uiModel,
            @RequestParam(value = "pReceiverName", required = false) String pReceiverName,
            HttpServletRequest httpServletRequest) {
        UserAccount pReceiver = UserAccount.findUserAccountByName(pReceiverName);
        if (pReceiver == null) {
            pReceiverName = BigUtil.getUTFString(pReceiverName);
            pReceiver = UserAccount.findUserAccountByName(pReceiverName);
            if (pReceiver == null)
                return null;
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
    public String create(
            @Valid Message message,
            BindingResult bindingResult,
            @RequestParam(value = "pReceiverName", required = false) String pReceiverName,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        UserAccount tReceiver = UserAccount.findUserAccountByName(pReceiverName);
        if (tReceiver == null) {
            pReceiverName = BigUtil.getUTFString(pReceiverName);
            tReceiver = UserAccount.findUserAccountByName(pReceiverName);
            if (tReceiver == null)
                return null;
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
            if (message.getContent().equals(tMsgInDB.getContent())
                    && (tMsgInDB.getPostTime().getHours() == new Date().getHours())) {
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
        PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController",
                PersonalController.class);
        return tController.index(pReceiverName, uiModel, httpServletRequest);
    }

    @RequestMapping(produces = "text/html")
    public String list(
            HttpSession session,
            @RequestParam(value = "sortExpression", required = false) String sortExpression,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        if (tUserAccount == null)
            return "login";
        tCurName = tUserAccount.getName();
        UserAccount tReceiver = UserAccount.findUserAccountByName(tCurName);
        tCurName = tReceiver.getName();
        float nrOfPages;
        if (tCurName.equals("admin")) {
            uiModel.addAttribute("messages", Message.findMessageEntries(firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Message.countMessages() / sizeNo;
            uiModel.addAttribute("maxPages",
                    (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            addDateTimeFormatPatterns(uiModel);
            return "messages/list";
        } else {
            uiModel.addAttribute("messages",
                    Message.findMessageByReceiver(tUserAccount, firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Message.countMessagesByReceiver(tReceiver) / sizeNo;
            tUserAccount.setNewMessageAmount(0);
            tUserAccount.persist();
            session.setAttribute("newMessageAmount", 0);
            uiModel.addAttribute("maxPages",
                    (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            addDateTimeFormatPatterns(uiModel);

            BigUtil.checkTheme(tReceiver, httpServletRequest);

            return "messages/Biglist";
        }
    }

    void populateEditForm(
            Model uiModel,
            Message message,
            HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("message", message);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());

        String tCurName = userContextService.getCurrentUserName();
        UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);
        BigUtil.checkTheme(tOwner, httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new Message());
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "receiver", "useraccounts" });
        }
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "publisher", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "messages/create";
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("message", Message.findMessage(id));
        uiModel.addAttribute("itemId", id);
        return "messages/show";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Message message, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, message);
            return "messages/update";
        }
        uiModel.asMap().clear();
        message.merge();
        return "redirect:/messages/" + encodeUrlPathSegment(message.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, Message.findMessage(id));
        return "messages/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Message message = Message.findMessage(id);
        message.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/messages";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("message_posttime_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }

	void populateEditForm(Model uiModel, Message message) {
        uiModel.addAttribute("message", message);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        try {
            Message message = Message.findMessage(id);
            if (message == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<String>(message.toJson(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        try {
            List<Message> result = Message.findAllMessages();
            return new ResponseEntity<String>(Message.toJsonArray(result), headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json, UriComponentsBuilder uriBuilder) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            Message message = Message.fromJsonToMessage(json);
            message.persist();
            RequestMapping a = (RequestMapping) getClass().getAnnotation(RequestMapping.class);
            headers.add("Location",uriBuilder.path(a.value()[0]+"/"+message.getId().toString()).build().toUriString());
            return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            for (Message message: Message.fromJsonArrayToMessages(json)) {
                message.persist();
            }
            return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json, @PathVariable("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            Message message = Message.fromJsonToMessage(json);
            message.setId(id);
            if (message.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<String>(headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            Message message = Message.findMessage(id);
            if (message == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
            message.remove();
            return new ResponseEntity<String>(headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
