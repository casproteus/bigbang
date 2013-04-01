package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.SpringApplicationContext;

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

@RequestMapping("/remarks")
@Controller
@RooWebScaffold(path = "remarks", formBackingObject = Remark.class)
public class RemarkController {

	@Inject
	private UserContextService userContextService;
	
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Remark remark, BindingResult bindingResult, Twitter pTwitter, Model uiModel, HttpServletRequest httpServletRequest) {
		//TODO: Should make the check before submit.
		if(remark.getContent() == null || remark.getContent().length() < 1)
            return "remarks/create";
		
		//get his last twitter in db compare with it.
		String tCurName = userContextService.getCurrentUserName();
	    UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
		List<Remark> tList = Remark.findRemarkByPublisher(tUserAccount, 0, 1);
		
		if(tList != null && tList.size() > 0){
			Remark tTwitter = tList.get(0);
			if(remark.getContent().equals(tTwitter.getContent()) && remark.getRemarkto().equals(tTwitter.getRemarkto()))
				return "remarks/create";
		}
	
		if (bindingResult.hasErrors()) {
			if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
				remark.setPublisher(tUserAccount);
				
				remark.setRemarkTime(new Date());//add remark time when it's submitted.
			} else {
				populateEditForm(uiModel, remark);
		        return "public/list_detail_twitter";
			}
        }
        uiModel.asMap().clear();
        remark.persist();
//        return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);

        PublicController tController = SpringApplicationContext.getApplicationContext().getBean("publicController", PublicController.class);
        return tController.showDetailTwitters(remark.getRemarkto().getId(), null, null, uiModel);
        /*
        int sizeNo = 25;
        final int firstResult = 0;
        
        UserAccount tOwner = remark.getRemarkto().getPublisher();

    	UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
    	Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        uiModel.addAttribute("spaceOwner", tOwner);
        float nrOfPages;
        Twitter tTwitter = remark.getRemarkto();
        uiModel.addAttribute("twitter", tTwitter);
        uiModel.addAttribute("remarks", Remark.findRemarkByTwitter(tTwitter, tAuthSet, firstResult, sizeNo));
        nrOfPages = (float) Remark.countRemarksByTwitter(tTwitter, tAuthSet) / sizeNo;
    	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	Remark tRemark = new Remark();
        uiModel.addAttribute("newremark", tRemark);
        uiModel.addAttribute("authorities",BigAuthority.getRemarkOptions());
        List<Twitter> remarktos = new ArrayList<Twitter>();
        remarktos.add(tTwitter);
        uiModel.addAttribute("remarktos", remarktos);
        
        return "public/list_detail_twitter";*/
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Remark remark, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
        	if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
				String tCurName = userContextService.getCurrentUserName();
				UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
				remark.setPublisher(tUserAccount);
				remark.setRemarkTime(new Date());//update remark time when it's modified.
			} else {
	            populateEditForm(uiModel, remark);
	            return "remarks/update";
			}
        }
        uiModel.asMap().clear();
        remark.merge();
        return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if(tCurName == null)
        	return "login";
        
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;

    	UserAccount tPublisher = UserAccount.findUserAccountByName(tCurName);
    	tCurName = tPublisher.getName();
        float nrOfPages;
    	if(tCurName.equals("admin")){
            uiModel.addAttribute("remarks", Remark.findRemarkEntries(firstResult, sizeNo));
            nrOfPages = (float) Remark.countRemarks() / sizeNo;
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
	        uiModel.addAttribute("remarks", Remark.findRemarkByPublisher(tPublisher, firstResult, sizeNo));
	        nrOfPages = (float) Remark.countRemarkByPublisher(tPublisher) / sizeNo;
    	}
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        addDateTimeFormatPatterns(uiModel);
        return "remarks/list";
    }
}
