package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.RssTwitter;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.model.MediaUpload;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
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

@RequestMapping("/useraccounts")
@Controller
@RooWebScaffold(path = "useraccounts", formBackingObject = UserAccount.class)
@RooWebJson(jsonObject = UserAccount.class)
public class UserAccountController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        UserAccount tUserAccount = new UserAccount();
        tUserAccount.setPrice(1);
        tUserAccount.setBalance(1000);
        populateEditForm(uiModel, tUserAccount);
        return "useraccounts/create";
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid UserAccount userAccount, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, userAccount);
            return "useraccounts/create";
        }
        UserAccount tUserAccount = UserAccount.findUserAccountByName(userAccount.getName());
        if (tUserAccount == null) {
            uiModel.asMap().clear();
            userAccount.setBalance(1000);
            userAccount.persist();
            addDefaultUserTags(userAccount.getName(), httpServletRequest.getLocale());
            addDefaultMessageTwitter(userAccount, httpServletRequest.getLocale());
            return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
        } else {
            uiModel.addAttribute("create_error", "abc");
            return "useraccounts/create";
        }
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, UserAccount.findUserAccount(id));
        uiModel.addAttribute("returnPath", id);
        return "useraccounts/update";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid UserAccount userAccount, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, userAccount);
            return "useraccounts/update";
        }
        UserAccount tUserAccount = UserAccount.findUserAccount(userAccount.getId());
        if (!tUserAccount.getName().equals(userAccount.getName())) {
            List<BigTag> tBigTags = BigTag.findTagsByPublisher(tUserAccount.getName(), 0, 1000);
            for (int i = tBigTags.size() - 1; i > -1; i--) {
                tBigTags.get(i).setType(userAccount.getName());
                tBigTags.get(i).merge();
            }
        }
        uiModel.asMap().clear();
        
    	//if name changed, need to update the image paths.
        if(!tUserAccount.getName().equals(userAccount.getName())){
            if(MediaUpload.countMediaUploadsByKey(tUserAccount.getName()) > 0){
            	MediaUpload tMH = MediaUpload.findMediaByKey(tUserAccount.getName() + "_headimg");
            	if(tMH != null){
            		tMH.setFilepath(userAccount.getName() + "_headimg");
            		tMH.merge();
            	}
            	MediaUpload tMB = MediaUpload.findMediaByKey(tUserAccount.getName() + "_bg");
            	if(tMB != null){
            		tMB.setFilepath(userAccount.getName() + "_bg");
            		tMB.merge();
            	}
            }
        }
        
        tUserAccount.setName(userAccount.getName());
        tUserAccount.setPassword(userAccount.getPassword());
        tUserAccount.setEmail(userAccount.getEmail());
        tUserAccount.setDescription(userAccount.getDescription());
        if(StringUtils.isNotBlank(userAccount.getLayout())) //because when user modify his useraccount info, the layout field doesn't display,
        	tUserAccount.setLayout(userAccount.getLayout());//then the layout will be null. and we don't want it be save into db.
        tUserAccount.persist();
        return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        UserAccount userAccount = UserAccount.findUserAccount(id);
        List<BigTag> tBigTags = BigTag.findTagsByPublisher(userAccount.getName(), 0, 1000);
        for (int i = tBigTags.size() - 1; i > -1; i--) {
            tBigTags.get(i).remove();
        }
        userAccount.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/useraccounts";
    }

    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null) return "login";
        if (tCurName.equalsIgnoreCase("admin")) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("useraccounts", UserAccount.findUserAccountEntries(firstResult, sizeNo));
            float nrOfPages = (float) UserAccount.countUserAccounts() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            List<UserAccount> tList = new ArrayList<UserAccount>();
            tList.add(UserAccount.findUserAccountByName(tCurName));
            uiModel.addAttribute("useraccounts", tList);
            uiModel.addAttribute("maxPages", 1);
        }
        return "useraccounts/list";
    }
    
    private void addDefaultUserTags(String pType, Locale pLocale) {
        BigTag tBigTag1 = new BigTag();
        tBigTag1.setTagName(messageSource.getMessage("admin_suggested_tag1", new Object[0], pLocale));
        tBigTag1.setType(pType);
        tBigTag1.setAuthority(0);
        tBigTag1.setOwner(0);
        tBigTag1.persist();
        BigTag tBigTag2 = new BigTag();
        tBigTag2.setTagName(messageSource.getMessage("admin_suggested_tag2", new Object[0], pLocale));
        tBigTag2.setType(pType);
        tBigTag2.setAuthority(0);
        tBigTag2.setOwner(0);
        tBigTag2.persist();
        BigTag tBigTag3 = new BigTag();
        tBigTag3.setTagName(messageSource.getMessage("admin_suggested_tag3", new Object[0], pLocale));
        tBigTag3.setType(pType);
        tBigTag3.setAuthority(0);
        tBigTag3.setOwner(0);
        tBigTag3.persist();
        BigTag tBigTag4 = new BigTag();
        tBigTag4.setTagName(messageSource.getMessage("admin_suggested_tag4", new Object[0], pLocale));
        tBigTag4.setType(pType);
        tBigTag4.setAuthority(0);
        tBigTag4.setOwner(0);
        tBigTag4.persist();
        BigTag tBigTag5 = new BigTag();
        tBigTag5.setTagName(messageSource.getMessage("admin_suggested_tag5", new Object[0], pLocale));
        tBigTag5.setType(pType);
        tBigTag5.setAuthority(0);
        tBigTag5.setOwner(1);
        tBigTag5.persist();
        BigTag tBigTag6 = new BigTag();
        tBigTag6.setTagName(messageSource.getMessage("admin_suggested_tag6", new Object[0], pLocale));
        tBigTag6.setType(pType);
        tBigTag6.setAuthority(0);
        tBigTag6.setOwner(1);
        tBigTag6.persist();
        BigTag tBigTag7 = new BigTag();
        tBigTag7.setTagName(messageSource.getMessage("admin_suggested_tag7", new Object[0], pLocale));
        tBigTag7.setType(pType);
        tBigTag7.setAuthority(0);
        tBigTag7.setOwner(1);
        tBigTag7.persist();
        BigTag tBigTag8 = new BigTag();
        tBigTag8.setTagName(messageSource.getMessage("admin_suggested_tag8", new Object[0], pLocale));
        tBigTag8.setType(pType);
        tBigTag8.setAuthority(0);
        tBigTag8.setOwner(1);
        tBigTag8.persist();
    }

    private void addDefaultMessageTwitter(UserAccount pPublisher, Locale pLocale) {
        Message tMessage = new Message();
        tMessage.setReceiver(pPublisher);
        tMessage.setPublisher(UserAccount.findUserAccountByName("admin"));
        tMessage.setPostTime(new Date());
        Object[] tObjAry = new Object[] { pPublisher.getName() };
        tMessage.setContent(messageSource.getMessage("default_welcome_message", tObjAry, pLocale));
        tMessage.persist();
    }

    @RequestMapping(value = "/getImage/{id}")
    /**this method should be called only when user has logged in, and user's cliking the useraccount button on top-right corner.  */
    public void getImage(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
	    response.setContentType("image/jpeg");
	    if("uc__headimage".equals(id) || "uc__bg".equals(id)){
	    	if(userContextService.getCurrentUserName() != null){
	    		id = userContextService.getCurrentUserName().toLowerCase() + (id.endsWith("_bg") ? "_bg" : "_headimage");
    			id = "uc_" + id;
	    	}
	    }
	    
	    MediaUpload tMedia = MediaUpload.findMediaByKey(id);
	    try{
		    if(tMedia != null && tMedia.getContent() != null){
		    	byte[] imageBytes = tMedia.getContent();
		    	response.getOutputStream().write(imageBytes);
		    	response.getOutputStream().flush();
		    }else{	
		    	//leave empty. this method will only be called when displaying the updateForm of useraccount to display the image in the dialog, 
		    	//so shall not display admin's default image.		    	
		    }
	    }catch(Exception e){
	    	System.out.println("Exception occured when fetching img of ID:" + id + "! " + e);
	    }
    }
}
