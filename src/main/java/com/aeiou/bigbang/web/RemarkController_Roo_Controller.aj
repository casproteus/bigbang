// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.web.RemarkController;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect RemarkController_Roo_Controller {
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String RemarkController.create(@Valid Remark remark, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, remark);
            return "remarks/create";
        }
        uiModel.asMap().clear();
        remark.persist();
        return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    public String RemarkController.createForm(Model uiModel) {
        populateEditForm(uiModel, new Remark());
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        if (Content.countContents() == 0) {
            dependencies.add(new String[] { "content", "contents" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "remarks/create";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String RemarkController.show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("remark", Remark.findRemark(id));
        uiModel.addAttribute("itemId", id);
        return "remarks/show";
    }
    
    @RequestMapping(produces = "text/html")
    public String RemarkController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("remarks", Remark.findRemarkEntries(firstResult, sizeNo));
            float nrOfPages = (float) Remark.countRemarks() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("remarks", Remark.findAllRemarks());
        }
        addDateTimeFormatPatterns(uiModel);
        return "remarks/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String RemarkController.update(@Valid Remark remark, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, remark);
            return "remarks/update";
        }
        uiModel.asMap().clear();
        remark.merge();
        return "redirect:/remarks/" + encodeUrlPathSegment(remark.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String RemarkController.updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, Remark.findRemark(id));
        return "remarks/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String RemarkController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Remark remark = Remark.findRemark(id);
        remark.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/remarks";
    }
    
    void RemarkController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("remark_remarttime_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }
    
    void RemarkController.populateEditForm(Model uiModel, Remark remark) {
        uiModel.addAttribute("remark", remark);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("contents", Content.findAllContents());
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
    }
    
    String RemarkController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
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
