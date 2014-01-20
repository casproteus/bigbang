package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.theme.CookieThemeResolver;

import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.RssTwitter;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.web.beans.RefreshBean;

@RequestMapping("/remarks")
@Controller
@RooWebScaffold(path = "remarks", formBackingObject = Remark.class)
@RooWebJson(jsonObject = Remark.class)
public class RemarkController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;

    @Autowired
    private transient MailSender mailTemplate;

    @RequestMapping(value = "/refreshRemarks", method = RequestMethod.GET)
    @ResponseBody
    public List<com.aeiou.bigbang.domain.Remark> refreshRemarks(@RequestParam String twitterid) {
        Twitter tTwitter = Twitter.findTwitter(Long.valueOf(twitterid));
        UserAccount tOwner = tTwitter.getPublisher();
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
        Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        return Remark.findRemarkByTwitter(tTwitter, tAuthSet, 0, 20);
    }

    void populateEditForm(Model uiModel, Remark remark, HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("remark", remark);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("twitters", Twitter.findAllTwitters());
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
        uiModel.addAttribute("authorities", BigAuthority.getRemarkOptions(messageSource, httpServletRequest.getLocale()));
        uiModel.addAttribute("refresh_time", remark.getRefresh_time());
    }

    @RequestMapping(params = "pTwitterId", method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Remark remark, BindingResult bindingResult, @RequestParam(value = "pTwitterId", required = false) Long pTwitterId, Model uiModel, HttpServletRequest httpServletRequest) {
        if (remark.getContent() == null || remark.getContent().length() < 1) {
            populateEditForm(uiModel, new Remark(), httpServletRequest);
            return "remarks/create";
        }
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        List<Remark> tList = Remark.findRemarkByPublisher(tUserAccount, 0, 1);
        if (tList != null && tList.size() > 0) {
            Remark tTwitter = tList.get(0);
            if (remark.getContent().equals(tTwitter.getContent()) && remark.getRemarkto().equals(tTwitter.getRemarkto())) {
                populateEditForm(uiModel, new Remark(), httpServletRequest);
                return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);
            }
        }
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
                remark.setPublisher(tUserAccount);
                remark.setRemarkto(Twitter.findTwitter(pTwitterId));
                remark.setRemarkTime(new Date());
            } else {
                populateEditForm(uiModel, remark, httpServletRequest);
                return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);
            }
        }
        uiModel.asMap().clear();
        remark.persist();
        
        BigUtil.refreshULastUpdateTimeOfTwitter(remark);
        
        checkRss(remark);	//to check if need to send out notice email.
        
        return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Remark remark, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
                String tCurName = userContextService.getCurrentUserName();
                UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
                remark.setPublisher(tUserAccount);
                remark.setRemarkto(Remark.findRemark(remark.getId()).getRemarkto());
                remark.setRemarkTime(new Date());
            } else {
                populateEditForm(uiModel, remark, httpServletRequest);
                return "remarks/update";
            }
        }
        uiModel.asMap().clear();
        remark.merge();
        BigUtil.refreshULastUpdateTimeOfTwitter(remark);
        return "redirect:/remarks?twitterid=" + encodeUrlPathSegment(remark.getRemarkto().getId().toString(), httpServletRequest);
    }

    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null) return "login";
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        UserAccount tPublisher = UserAccount.findUserAccountByName(tCurName);
        tCurName = tPublisher.getName();
        float nrOfPages;
        if (tCurName.equals("admin")) {
            uiModel.addAttribute("remarks", Remark.findRemarkEntries(firstResult, sizeNo));
            nrOfPages = (float) Remark.countRemarks() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("remarks", Remark.findRemarkByPublisher(tPublisher, firstResult, sizeNo));
            nrOfPages = (float) Remark.countRemarkByPublisher(tPublisher) / sizeNo;
        }
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        addDateTimeFormatPatterns(uiModel);
        return "remarks/list";
    }

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, new Remark(), httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "remarks/create";
    }

    @RequestMapping(params = "twitterid", produces = "text/html")
    public String showDetailTwitters(@RequestParam(value = "twitterid", required = false) Long twitterid, Integer refresh_time, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel, HttpServletRequest request) {
        Twitter tTwitter = Twitter.findTwitter(twitterid);
        UserAccount tOwner = tTwitter.getPublisher();
        int tTheme = tOwner.getTheme();		//if the owner has setted theme, then use the theme! (will effect only on this request)
        if (tTheme != 0) request.setAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME, String.valueOf(tTheme));
        int sizeNo = size == null ? 20 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
        Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        uiModel.addAttribute("spaceOwner", tOwner);
        float nrOfPages;
        uiModel.addAttribute("twitter", tTwitter);
        List<Remark> tRemarksList = Remark.findRemarkByTwitter(tTwitter, tAuthSet, firstResult, sizeNo);
        uiModel.addAttribute("remarks", tRemarksList);
        nrOfPages = (float) Remark.countRemarksByTwitter(tTwitter, tAuthSet) / sizeNo;
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        Remark tRemark = new Remark();
        if (refresh_time == null) 	//default refresh time. @NOTE: can not be null, or the webpage will report error. like" if (  >0){"
        	refresh_time = Integer.valueOf(0);
        tRemark.setRefresh_time(refresh_time.intValue());
        uiModel.addAttribute("newremark", tRemark);
        uiModel.addAttribute("authorities", BigAuthority.getRemarkOptions(messageSource, request.getLocale()));
        List<Twitter> remarktos = new ArrayList<Twitter>();
        remarktos.add(tTwitter);
        uiModel.addAttribute("remarktos", remarktos);
        uiModel.addAttribute("refresh_time", refresh_time);
        
        //check how many new remark since last remark.
        //if it's not displaying the lastest ones, it's pagging up to previous page, then don't show new message account.
        if (tCurUser != null && (page == null || page.intValue() == 0)) {
            int i = 0;
            for (; i < tRemarksList.size(); i++) {
                if (tRemarksList.get(i).getPublisher().getId() == tCurUser.getId()) break;
            }
            uiModel.addAttribute("newMessageNumber", i);
        } else uiModel.addAttribute("newMessageNumber", 0);
        return "public/list_detail_twitter";
    }

    @RequestMapping(params = "twitterid", method = RequestMethod.POST, produces = "text/html")
    public String createRemark(@Valid Remark remark, BindingResult bindingResult, @RequestParam(value = "twitterid", required = false) Long pTwitterId, Model uiModel, HttpServletRequest httpServletRequest) {
        if (remark.getContent() == null || remark.getContent().length() < 1) {
            return showDetailTwitters(pTwitterId, remark.getRefresh_time(), null, null, uiModel, httpServletRequest);
        }
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        List<Remark> tList = Remark.findRemarkByPublisher(tUserAccount, 0, 1);
        if (tList != null && tList.size() > 0) {
            Remark tRemark = tList.get(0);
            if (tRemark.getContent().equals(remark.getContent()) && tRemark.getRemarkto().getId().equals(pTwitterId)) {
                populateEditForm(uiModel, remark, httpServletRequest);
                return showDetailTwitters(pTwitterId, remark.getRefresh_time(), null, null, uiModel, httpServletRequest);
            }
        }
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
                remark.setPublisher(tUserAccount);
                remark.setRemarkto(Twitter.findTwitter(pTwitterId));
                remark.setRemarkTime(new Date());
            } else {
                populateEditForm(uiModel, remark, httpServletRequest);
                return "redirect:/remarks?twitterid=" + encodeUrlPathSegment(remark.getRemarkto().getId().toString(), httpServletRequest) + "&refresh_time=" + remark.getRefresh_time();
            }
        }
        uiModel.asMap().clear();
        remark.persist();
        
        BigUtil.refreshULastUpdateTimeOfTwitter(remark);
        checkRss(remark);	//to check if need to send out notice email.
        
        return "redirect:/remarks?twitterid=" + encodeUrlPathSegment(remark.getRemarkto().getId().toString(), httpServletRequest) + "&refresh_time=" + remark.getRefresh_time();
    }

    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, Remark.findRemark(id), httpServletRequest);
        return "remarks/update";
    }

    @RequestMapping(params = "refreshTime", produces = "text/html")
    public String setRefreshTime(RefreshBean refreshBean, @RequestParam(value = "refreshTwitterid", required = true) Long refreshTwitterid, Model uiModel, HttpServletRequest httpServletRequest) {
        int tTime;
        String tTimeStr = refreshBean.getRefreshTime();
        if (tTimeStr != null && tTimeStr.startsWith(",")) tTimeStr = tTimeStr.substring(1);
        try {
            tTime = Integer.valueOf(tTimeStr);
        } catch (Exception e) {
            tTime = 0;
        }
        tTime = (tTime == 0) ? 0 : (tTime > 15 ? tTime : 15);
        return showDetailTwitters(refreshTwitterid, tTime, null, null, uiModel, httpServletRequest);
    }

    @RequestMapping(params = "rss", produces = "text/html")
    public String setRssOrder(@RequestParam(value = "rss", required = true) int refresh_time, @RequestParam(value = "rsstwitterid", required = false) Long pTwitterId, Model uiModel, HttpServletRequest httpServletRequest) {
        String tCurName = userContextService.getCurrentUserName();
        Twitter twitter = Twitter.findTwitter(pTwitterId);
        UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
        if (tCurUser != null && !RssTwitter.isAllreadyExist(tCurUser, twitter)) {
            RssTwitter tRssTwitter = new RssTwitter();
            tRssTwitter.setUseraccount(tCurUser);
            tRssTwitter.setTwitter(twitter);
            tRssTwitter.persist();
        }
        return showDetailTwitters(pTwitterId, refresh_time, null, null, uiModel, httpServletRequest);
    }
    
    @RequestMapping(params = "removerss", produces = "text/html")
    public String removeRssOrder(@RequestParam(value = "removerss", required = true) String username, @RequestParam(value = "removersstwitterid", required = true) Long pTwitterId,
		Model uiModel, HttpServletRequest httpServletRequest) {
    	
        Twitter twitter = Twitter.findTwitter(pTwitterId);
        UserAccount tCurUser = UserAccount.findUserAccountByName(username);
        if(tCurUser != null){
        	RssTwitter.deleteRssTwitterByTwitterAndUserAcount(twitter, tCurUser);
        }

        return showDetailTwitters(pTwitterId, 0, null, null, uiModel, httpServletRequest);
	}
    private void checkRss(Remark remark){
    	Twitter tTwitter = remark.getRemarkto();
    	List<RssTwitter> tList = RssTwitter.findAllListenersByTwitter(tTwitter);
    	Customize tCustomizeLink = Customize.findCustomizeByKey("RemarkSourceLinkStr");
    	Customize tCustomizeReplay = Customize.findCustomizeByKey("i18n_Replay");
    	Customize tCustomizeUnsubscribe = Customize.findCustomizeByKey("i18n_Unsubscribe");
    	Customize tCustomizeNewRemarkString = Customize.findCustomizeByKey("i18n_NewRemark");

    	String linkStr = tCustomizeLink != null ? tCustomizeLink.getCusValue() : "http://www.sharethegoodones.com";
    	String tReply = tCustomizeReplay != null ? tCustomizeReplay.getCusValue() : "Reply";
    	String tUnsubscribe = tCustomizeUnsubscribe != null ? tCustomizeUnsubscribe.getCusValue() : "Unsubscribe";
    	String tNewRemark = tCustomizeNewRemarkString != null ? tCustomizeNewRemarkString.getCusValue() : "New remark: ";
    	
    	for(int i = 0; i < tList.size(); i++){
    		RssTwitter tRT = tList.get(i);
    		String tUserName = tRT.getUseraccount().getName();
    		if(remark.getAuthority() != 0 && !tUserName.equals(tTwitter.getPublisher().getName())) //if only visible to publisher, don't sent do others.
    			continue;
    		String email = tRT.getUseraccount().getEmail();
    		String content = 
    				"<p align='right'>---" + remark.getPublisher().getName() +" </p>" +
    				"<p align='center'><a href='" + linkStr + "/remarks?twitterid=" + tTwitter.getId() + "'>" + tReply + "</a> | " +
    				   				  "<a href='" + linkStr + "/remarks?removerss=" + tUserName + "&removersstwitterid=" + tTwitter.getId() + "'>" + tUnsubscribe + "</a></p>" +
    				"<p align='center'><a href='" + linkStr + "'>" + linkStr + "</a></p>";
    				
    		if(email != null && email.indexOf("@") > 0 && email.indexOf(".", email.indexOf("@")) > 0){	//if it's valid.
    			if(!email.equals(remark.getPublisher().getEmail()))										//if it's not the author himself.
    				sendMessage("www.ShareTheGoodOnes.com", 
    						tNewRemark + tTwitter.getTwtitle(), 
    						email, 
    						remark.getContent() + content);
    		}
    	}
    }
    
    private void sendMessage(String mailFrom, String subject, String mailTo, String message) {
        MimeMessage mimeMessage = ((JavaMailSender)mailTemplate).createMimeMessage();
        MimeMessageHelper helper = null;
        try{
        	helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
	        mimeMessage.setContent(message, "text/html");
	        helper.setTo(mailTo);
	        helper.setSubject(subject);
	        helper.setFrom(mailFrom);
        }catch(Exception e){
        	System.out.println("Sending email failed!" + mailTo + "|" + subject + "|" + message);
        }
        ((JavaMailSender)mailTemplate).send(mimeMessage);
    }
    
}
