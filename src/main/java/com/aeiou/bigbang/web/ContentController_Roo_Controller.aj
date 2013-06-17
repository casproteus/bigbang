// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.web.ContentController;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect ContentController_Roo_Controller {
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String ContentController.show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("content", Content.findContent(id));
        uiModel.addAttribute("itemId", id);
        return "contents/show";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String ContentController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Content content = Content.findContent(id);
        content.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/contents";
    }
    
    void ContentController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("content_markdate_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }
    
    void ContentController.populateEditForm(Model uiModel, Content content) {
        uiModel.addAttribute("content", content);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("bigtags", BigTag.findAllBigTags());
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
    }
    
    String ContentController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    
}
