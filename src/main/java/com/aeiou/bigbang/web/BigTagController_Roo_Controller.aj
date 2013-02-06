// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.web.BigTagController;
import java.io.UnsupportedEncodingException;
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

privileged aspect BigTagController_Roo_Controller {
    
    @RequestMapping(params = "form", produces = "text/html")
    public String BigTagController.createForm(Model uiModel) {
        populateEditForm(uiModel, new BigTag());
        return "bigtags/create";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String BigTagController.show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("bigtag", BigTag.findBigTag(id));
        uiModel.addAttribute("itemId", id);
        return "bigtags/show";
    }
    
    @RequestMapping(produces = "text/html")
    public String BigTagController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("bigtags", BigTag.findBigTagEntries(firstResult, sizeNo));
            float nrOfPages = (float) BigTag.countBigTags() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("bigtags", BigTag.findAllBigTags());
        }
        return "bigtags/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String BigTagController.update(@Valid BigTag bigTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, bigTag);
            return "bigtags/update";
        }
        uiModel.asMap().clear();
        bigTag.merge();
        return "redirect:/bigtags/" + encodeUrlPathSegment(bigTag.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String BigTagController.updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, BigTag.findBigTag(id));
        return "bigtags/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String BigTagController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        BigTag bigTag = BigTag.findBigTag(id);
        bigTag.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/bigtags";
    }
    
    String BigTagController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
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
