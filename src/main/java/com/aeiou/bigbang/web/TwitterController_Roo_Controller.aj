// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.web.TwitterController;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect TwitterController_Roo_Controller{

@RequestMapping(value="/{id}",produces="text/html")public String TwitterController.show(@PathVariable("id")Long id,Model uiModel){addDateTimeFormatPatterns(uiModel);uiModel.addAttribute("twitter",Twitter.findTwitter(id));uiModel.addAttribute("itemId",id);return"twitters/show";}

void TwitterController.addDateTimeFormatPatterns(Model uiModel){uiModel.addAttribute("twitter_twitdate_date_format",DateTimeFormat.patternForStyle("M-",LocaleContextHolder.getLocale()));uiModel.addAttribute("twitter_lastupdate_date_format",DateTimeFormat.patternForStyle("M-",LocaleContextHolder.getLocale()));}

void TwitterController.populateEditForm(Model uiModel,Twitter twitter){uiModel.addAttribute("twitter",twitter);addDateTimeFormatPatterns(uiModel);uiModel.addAttribute("bigtags",BigTag.findAllBigTags());uiModel.addAttribute("useraccounts",UserAccount.findAllUserAccounts());}

String TwitterController.encodeUrlPathSegment(String pathSegment,HttpServletRequest httpServletRequest){String enc=httpServletRequest.getCharacterEncoding();if(enc==null){enc=WebUtils.DEFAULT_CHARACTER_ENCODING;}try{pathSegment=UriUtils.encodePathSegment(pathSegment,enc);}catch(UnsupportedEncodingException uee){}return pathSegment;}

}
