// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.web.ContentController;
import com.aeiou.bigbang.domain.UserAccount;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect ContentController_Roo_Controller {
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String ContentController.create(@Valid Content content, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, content);
            return "contents/create";
        }
        uiModel.asMap().clear();
        content.persist();
        return "redirect:/contents/" + encodeUrlPathSegment(content.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    public String ContentController.createForm(Model uiModel) {
        populateEditForm(uiModel, new Content());
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "contents/create";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String ContentController.show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("content", Content.findContent(id));
        uiModel.addAttribute("itemId", id);
        return "contents/show";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String ContentController.update(@Valid Content content, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, content);
            return "contents/update";
        }
        uiModel.asMap().clear();
        content.merge();
        return "redirect:/contents/" + encodeUrlPathSegment(content.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String ContentController.updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, Content.findContent(id));
        return "contents/update";
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
