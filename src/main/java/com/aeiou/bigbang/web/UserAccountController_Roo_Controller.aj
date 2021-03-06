// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.web.UserAccountController;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect UserAccountController_Roo_Controller {
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String UserAccountController.show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("useraccount", UserAccount.findUserAccount(id));
        uiModel.addAttribute("itemId", id);
        return "useraccounts/show";
    }
    
    void UserAccountController.populateEditForm(Model uiModel, UserAccount userAccount) {
        uiModel.addAttribute("userAccount", userAccount);
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
    }
    
    String UserAccountController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
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
