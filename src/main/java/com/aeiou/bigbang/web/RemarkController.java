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
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/remarks")
@Controller
@RooWebScaffold(path = "remarks", formBackingObject = Remark.class)
public class RemarkController {

	@Inject
	private UserContextService userContextService;

	void populateEditForm(Model uiModel, Remark remark) {
        uiModel.addAttribute("remark", remark);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("twitters", Twitter.findAllTwitters());
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
        uiModel.addAttribute("authorities",BigAuthority.getRemarkOptions());
    }

	@RequestMapping(params = "pTwitterId", method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Remark remark, BindingResult bindingResult,
    		@RequestParam(value = "pTwitterId", required = false)Long pTwitterId,
    		Model uiModel, HttpServletRequest httpServletRequest) {
		//TODO: Should make the check before submit.
		if(remark.getContent() == null || remark.getContent().length() < 1)
            return "remarks/create";
		
		//get his last twitter in db compare with it.
		String tCurName = userContextService.getCurrentUserName();
	    UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
		List<Remark> tList = Remark.findRemarkByPublisher(tUserAccount, 0, 1);
		//This is good, but not good enough, because when user press F5 after modifying a remark, and press back->back
		//will trick out the form to submit again, and then in this method, the content are different....
		//TODO: add a hidden field in From and save a token in it.then verify, if the token not there, then stop saving
		//http://stackoverflow.com/questions/2324931/duplicate-form-submission-in-spring
		if(tList != null && tList.size() > 0){
			Remark tTwitter = tList.get(0);
			if(remark.getContent().equals(tTwitter.getContent()) && remark.getRemarkto().equals(tTwitter.getRemarkto()))
				return "remarks/create";
		}
	
		if (bindingResult.hasErrors()) {
			if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
				remark.setPublisher(tUserAccount);
				remark.setRemarkto(Twitter.findTwitter(pTwitterId));
				remark.setRemarkTime(new Date());//add remark time when it's submitted.
			} else {
				populateEditForm(uiModel, remark);
		        return "public/list_detail_twitter";
			}
        }
        uiModel.asMap().clear();
        remark.persist();

        refreshULastUpdateTimeOfTwitter(remark);
        
        PublicController tController = SpringApplicationContext.getApplicationContext().getBean("publicController", PublicController.class);
        return tController.showDetailTwitters(remark.getRemarkto().getId(), null, null, uiModel);
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
        refreshULastUpdateTimeOfTwitter(remark);
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

	/*
	 * update the lastupdate field of twitter.
	 */
	private void refreshULastUpdateTimeOfTwitter(Remark remark){
		remark = Remark.findRemark(remark.getId());	//this remark may got from webpage, and has no some field like "remarkto"
        Twitter tTwitter = remark.getRemarkto();
        tTwitter.setLastupdate(remark.getRemarkTime());
        tTwitter.merge();
	}
}
