package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;

@RequestMapping("/public/**")
@Controller
public class PublicController {

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("go to his big uncle's!");
    }

    @RequestMapping(produces = "text/html")
    public String index(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel){
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("contents", Content.findContentEntries(firstResult, sizeNo));
            float nrOfPages = (float) Content.countContents() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
        	List<BigTag> tBigTags = BigTag.findAllCommonBigTags();
            uiModel.addAttribute("bigTags", tBigTags);

            List<List> tContentLists = new ArrayList<List>();
        	for(int i = 0; i < tBigTags.size(); i++){
        		tContentLists.add(Content.findContentsByTag(tBigTags.get(i), 8));
        	}
            uiModel.addAttribute("contents", tContentLists);
        }
        return "public/index";
    }
}
