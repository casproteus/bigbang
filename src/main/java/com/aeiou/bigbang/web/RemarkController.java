package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.MessageSource;
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

import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;

@RequestMapping("/remarks")
@Controller
@RooWebScaffold(path = "remarks", formBackingObject = Remark.class)
@RooWebJson(jsonObject = Remark.class)
public class RemarkController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;
    
    @RequestMapping(value="/refreshRemarks", method=RequestMethod.GET)
    public @ResponseBody List<Remark> refreshRemarks(@RequestParam String twitterid) {
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
    public String showDetailTwitters(@RequestParam(value = "twitterid", required = false) Long twitterid, @RequestParam(value = "refresh_time", required = false) Integer refresh_time, 
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel, HttpServletRequest request) {
        Twitter tTwitter = Twitter.findTwitter(twitterid);
        UserAccount tOwner = tTwitter.getPublisher();
        int sizeNo = size == null ? 20 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
        Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        uiModel.addAttribute("spaceOwner", tOwner);
        float nrOfPages;
        uiModel.addAttribute("twitter", tTwitter);
        uiModel.addAttribute("remarks", Remark.findRemarkByTwitter(tTwitter, tAuthSet, firstResult, sizeNo));
        nrOfPages = (float) Remark.countRemarksByTwitter(tTwitter, tAuthSet) / sizeNo;
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        Remark tRemark = new Remark();
        uiModel.addAttribute("newremark", tRemark);
        uiModel.addAttribute("authorities", BigAuthority.getRemarkOptions(messageSource, request.getLocale()));
        List<Twitter> remarktos = new ArrayList<Twitter>();
        remarktos.add(tTwitter);
        uiModel.addAttribute("remarktos", remarktos);
        uiModel.addAttribute("refresh_time", (refresh_time == null || refresh_time == 0) ? 0 : (refresh_time > 15 ? refresh_time : 15));
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
            //@note: remark.getRemarkto() can be null, don't call it's equals method.
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
                return "redirect:/remarks?twitterid=" + encodeUrlPathSegment(remark.getRemarkto().getId().toString(), httpServletRequest) + "&refresh_time="+remark.getRefresh_time();
            }
        }
        uiModel.asMap().clear();
        remark.persist();
        BigUtil.refreshULastUpdateTimeOfTwitter(remark);
        return "redirect:/remarks?twitterid=" + encodeUrlPathSegment(remark.getRemarkto().getId().toString(), httpServletRequest) + "&refresh_time="+remark.getRefresh_time();
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, Remark.findRemark(id), httpServletRequest);
        return "remarks/update";
    }
   
    @RequestMapping(params = "refresh_time", produces = "text/html")
    public String setRefreshTime2(@RequestParam(value = "refresh_time", required = true) Integer refresh_time, 
    		@RequestParam(value = "pTwitterid", required = true) Long pTwitterId, 
    		Model uiModel, HttpServletRequest httpServletRequest) {
    	return showDetailTwitters(pTwitterId, refresh_time, null, null, uiModel, httpServletRequest);
    }
}
