package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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

@RequestMapping("/contents")
@Controller
@RooWebScaffold(path = "contents", formBackingObject = Content.class)
@RooWebJson(jsonObject = Content.class)
public class ContentController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;

    void populateEditForm(Model uiModel, Content content, HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("content", content);

        String tCurName = userContextService.getCurrentUserName();
        UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);        
        BigUtil.checkTheme(tOwner, httpServletRequest);
        
        List<BigTag> tTags = null;
        List<String[]> tTagsAndNums = BigUtil.fetchTagAndNumberInListOfArrayFormat(tOwner);
        if(BigUtil.notCorrect(tTagsAndNums)){
        	List<List> lists = BigUtil.resetTagsForOwner(tOwner, httpServletRequest);
        	tTags = lists.get(0);
        	tTags.addAll(lists.get(1));
    	}else{	
        	tTags = BigUtil.convertTagArrayToList(tTagsAndNums.get(0), tCurName);
        	tTags.addAll(BigUtil.convertTagArrayToList(tTagsAndNums.get(1), tCurName));
    	}
        
        uiModel.addAttribute("mytags", tTags);
        List<UserAccount> tList = new ArrayList<UserAccount>();
        tList.add(UserAccount.findUserAccountByName(tCurName));
        uiModel.addAttribute("useraccounts", tList);
        uiModel.addAttribute("authorities", BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));
    }

    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "sortExpression", required = false) String sortExpression, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,
    		Model uiModel, HttpServletRequest httpServletRequest) {
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null) return "login";
        UserAccount tPublisher = UserAccount.findUserAccountByName(tCurName);
        tCurName = tPublisher.getName();
        float nrOfPages;
        if (tCurName.equals("admin")) {
            uiModel.addAttribute("contents", Content.findContentEntries(firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Content.countContents() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            Set<Integer> tAuthSet = BigAuthority.getAuthSet(tPublisher, tPublisher);
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, tAuthSet, firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Content.countContentsByPublisher(tPublisher, tAuthSet) / sizeNo;
        }
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        
        BigUtil.checkTheme(tPublisher, httpServletRequest);
        return "contents/list";
    }

    void populateEditForm_Tag(Model uiModel, BigTag bigTag, HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("bigTag", bigTag);
        uiModel.addAttribute("authorities", BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));

        String tUserName = userContextService.getCurrentUserName();
        UserAccount tOwner = UserAccount.findUserAccountByName(tUserName);        
        BigUtil.checkTheme(tOwner, httpServletRequest);
    }

    public String createForm_Tag(Model uiModel, HttpServletRequest httpServletRequest, String contentTitle, String contentURL, String commonTagName) {
        BigTag bigTag = new BigTag();
        bigTag.setContentTitle(contentTitle);
        bigTag.setContentURL(contentURL);
        bigTag.setCommonTagName(commonTagName);
        populateEditForm_Tag(uiModel, bigTag, httpServletRequest);
        return "contents/create_tag";
    }

    @RequestMapping(params = "twitle", produces = "text/html")
    public String createTag(@Valid BigTag bigTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        bigTag.setOwner(0);
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        tCurName = tUserAccount.getName();
        //get the tag have created. if it's already ceated then do nothing.
        BigTag tBT = BigTag.findBMTagByNameAndOwner(bigTag.getTagName(), tCurName);
        if(tBT == null){
	        bigTag.setType(tCurName);
	        uiModel.asMap().clear();
	        bigTag.persist();
	        String tLayout = tUserAccount.getLayout();
	        int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
	        if (p > -1) {
	            String tTagStr = tLayout.substring(0, p);
	            String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);
	            StringBuilder tStrB = new StringBuilder();
	            tStrB.append(tTagStr).append(BigUtil.SEP_ITEM);
	            tStrB.append(BigUtil.getTagInLayoutString(bigTag));
	            tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tSizeStr).append(BigUtil.SEP_ITEM).append("8");
	            tUserAccount.setLayout(tStrB.toString());
	            tUserAccount.persist();
	        } else {
	            BigUtil.resetLayoutString(tUserAccount);
	        }
        }
        Long tContentID = bigTag.getContentID();
        Content tContent = tContentID == null ? new Content() : Content.findContent(tContentID);
        tContent.setTitle(bigTag.getContentTitle());
        tContent.setSourceURL(bigTag.getContentURL());
        if(tContentID != null)
        	tContent.setUncommonBigTag(null);
        populateEditForm(uiModel, tContent, httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return tContentID == null ? "contents/create" : "contents/update";
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Content content, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (StringUtils.isNotBlank(content.getAddingTagFlag())) {
        	BigTag tBigTag = content.getCommonBigTag();
            return createForm_Tag(uiModel, httpServletRequest, content.getTitle(), content.getSourceURL(), tBigTag != null ? tBigTag.getTagName() : null);
        }
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 1 && content.getPublisher() == null) {
                String tCurName = userContextService.getCurrentUserName();
                UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
                content.setPublisher(tUserAccount);
                uiModel.asMap().clear();
                content.persist();
                PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
                return tController.index(tUserAccount.getName(), -1, -1, uiModel, httpServletRequest);
            } else {
                populateEditForm(uiModel, content, httpServletRequest);
                return "contents/create";
            }
        }
        uiModel.asMap().clear();
        content.persist();
        return "redirect:/contents/" + encodeUrlPathSegment(content.getId().toString(), httpServletRequest);
    }

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel, HttpServletRequest httpServletRequest) {
        Content tContent = new Content();
        tContent.setSourceURL(httpServletRequest.getParameter("url"));
        String tTitle = httpServletRequest.getParameter("title");
        if (tTitle != null) {
            tTitle = BigUtil.getUTFString(tTitle);
        }
        tContent.setTitle(tTitle);
        populateEditForm(uiModel, tContent, httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "contents/create";
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Content content, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 1 && content.getPublisher() == null) {
                String tCurName = userContextService.getCurrentUserName();
                UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
                content.setPublisher(tUserAccount);
            } else {
                populateEditForm(uiModel, content, httpServletRequest);
                return "contents/update";
            }
        }
        uiModel.asMap().clear();
        content.merge();
        return "redirect:/contents/" + encodeUrlPathSegment(content.getId().toString(), httpServletRequest);
    }

    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel, HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, Content.findContent(id), httpServletRequest);
        return "contents/update";
    }
}
