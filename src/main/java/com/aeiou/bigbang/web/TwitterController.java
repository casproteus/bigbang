package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/twitters")
@Controller
@RooWebScaffold(path = "twitters", formBackingObject = Twitter.class)
@RooWebJson(jsonObject = Twitter.class)
public class TwitterController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;

    void populateEditForm(
            Model uiModel,
            Twitter twitter,
            HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("twitter", twitter);

        UserAccount userAccount = UserAccount.findUserAccountByName(userContextService.getCurrentUserName());
        String tCurName = userAccount.getName();

        List<BigTag> tags = null;
        List<String[]> tTagsAndNums = BigUtil.fetchTagAndNumberFromLayoutStr(userAccount, 1);
        if (BigUtil.notCorrect(tTagsAndNums)) {
            List<List> lists = BigUtil.generateDefaultTagsForOwner(httpServletRequest, userAccount, 1);
            tags = lists.get(0);
            tags.addAll(lists.get(1));
        } else {
            tags = BigUtil.convertTagStringListToObjList(tTagsAndNums.get(0), tCurName);
            tags.addAll(BigUtil.convertTagStringListToObjList(tTagsAndNums.get(1), tCurName));
        }

        uiModel.addAttribute("mytags", tags);
        List<UserAccount> tList = new ArrayList<UserAccount>();
        tList.add(UserAccount.findUserAccountByName(tCurName));
        uiModel.addAttribute("useraccounts", tList);
        uiModel.addAttribute("authorities", BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));

        BigUtil.checkTheme(userAccount, httpServletRequest);
    }

    void populateEditForm_Tag(
            Model uiModel,
            BigTag bigTag,
            HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("bigTag", bigTag);
        uiModel.addAttribute("authorities", BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));
    }

    /**
     * if the hidden flag is set, then when create or update page submitted, go to this method. will create a special
     * page for creating tag. when this page committed, will back to the createTag method.
     */
    private String createForm_Tag(
            Model uiModel,
            HttpServletRequest httpServletRequest,
            Long twitterID,
            String twitterTitle,
            String twitterContent) {
        BigTag bigTag = new BigTag();
        bigTag.setTwitterID(twitterID);
        bigTag.setTwitterTitle(twitterTitle);
        bigTag.setTwitterContent(twitterContent);
        populateEditForm_Tag(uiModel, bigTag, httpServletRequest);
        return "twitters/create_tag";
    }

    @RequestMapping(params = "twitle", produces = "text/html")
    public String createTag(
            @Valid
            BigTag bigTag,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        bigTag.setOwner(1);
        String tCurName = userContextService.getCurrentUserName();
        UserAccount userAccount = UserAccount.findUserAccountByName(tCurName);
        tCurName = userAccount.getName();

        // get the tag have created. if it's already ceated then do nothing.
        BigTag tBT = BigTag.findTagByNameAndOwner(bigTag.getTagName(), tCurName);
        if (tBT == null) {
            bigTag.setType(tCurName);
            uiModel.asMap().clear();
            bigTag.persist();

            String tLayout = userAccount.getNoteLayout();
            int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
            if (p > -1) {
                String tTagStr = tLayout.substring(0, p);
                String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);
                StringBuilder tStrB = new StringBuilder();
                tStrB.append(tTagStr).append(BigUtil.SEP_ITEM);
                tStrB.append(BigUtil.getLayoutFormatTagString(bigTag));
                tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tSizeStr).append(BigUtil.SEP_ITEM).append("8");
                userAccount.setLayout(tStrB.toString());
                userAccount.persist();
            } else {
                BigUtil.generateDefaultTagsForOwner(httpServletRequest, userAccount, 1);
            }
        }
        Long tWitterID = bigTag.getTwitterID();
        Twitter twitter = tWitterID == null ? new Twitter() : Twitter.findTwitter(tWitterID);
        twitter.setTwtitle(bigTag.getTwitterTitle());
        twitter.setTwitent(bigTag.getTwitterContent());
        if (tWitterID != null)
            twitter.setTwittertag(null);
        populateEditForm(uiModel, twitter, httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return bigTag.getTwitterID() == null ? "twitters/create" : "twitters/update";
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(
            @Valid
            Twitter twitter,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (StringUtils.isNotBlank(twitter.getAddingTagFlag()))
            return createForm_Tag(uiModel, httpServletRequest, null, twitter.getTwtitle(), twitter.getTwitent());
        if (twitter.getTwitent() == null || twitter.getTwitent().length() < 1) {
            populateEditForm(uiModel, twitter, httpServletRequest);
            return "twitters/create";
        }
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        List<Twitter> tList =
                Twitter.findTwitterByPublisher(tUserAccount, BigAuthority.getAuthSet(tUserAccount, tUserAccount), 0, 1,
                        null);
        if (tList != null && tList.size() > 0) {
            Twitter tTwitter = tList.get(0);
            System.out.println("---" + twitter.getTwitent() + "---");
            if (twitter.getTwitent().equals(tTwitter.getTwitent())) {
                populateEditForm(uiModel, twitter, httpServletRequest);
                return "twitters/create";
            }
        }
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 2 && twitter.getPublisher() == null
                    && twitter.getTwitDate() == null) {
                twitter.setPublisher(tUserAccount);
                twitter.setTwitDate(new Date());
                if (twitter.getTwtitle() == null || twitter.getTwtitle().trim().length() == 0) {
                    String content = twitter.getTwitent();
                    int tIdx = content.indexOf("<br />");
                    if (tIdx > 0)
                        content = content.substring(0, tIdx);
                    content = content.length() > 30 ? content.substring(0, 30) : content;
                    twitter.setTwtitle(content.trim());
                }
            } else {
                populateEditForm(uiModel, twitter, httpServletRequest);
                return "twitters/create";
            }
        }
        uiModel.asMap().clear();
        twitter.setLastupdate(new Date());
        twitter.persist();
        PersonalController tController =
                SpringApplicationContext.getApplicationContext()
                        .getBean("personalController", PersonalController.class);
        // return tController.index(tUserAccount.getName(), -1, -1, uiModel);
        return showDetailTwitters(twitter.getId(), null, null, uiModel, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(
            @Valid
            Twitter twitter,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (StringUtils.isNotBlank(twitter.getAddingTagFlag()))
            return createForm_Tag(uiModel, httpServletRequest, twitter.getId(), twitter.getTwtitle(),
                    twitter.getTwitent());
        if (bindingResult.hasErrors()) {
            if (bindingResult.getAllErrors().size() == 2 && twitter.getPublisher() == null
                    && twitter.getTwitDate() == null) {
                String tCurName = userContextService.getCurrentUserName();
                UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
                twitter.setPublisher(tUserAccount);
                twitter.setTwitDate(new Date());
                if (twitter.getTwtitle() == null || twitter.getTwtitle().trim().length() == 0) {
                    String content = twitter.getTwitent();
                    int tIdx = content.indexOf("<br />");
                    if (tIdx > 0)
                        content = content.substring(0, tIdx);
                    content = content.length() > 30 ? content.substring(0, 30) : content;
                    twitter.setTwtitle(content);
                }
            } else {
                populateEditForm(uiModel, twitter, httpServletRequest);
                return "twitters/update";
            }
        }
        uiModel.asMap().clear();
        twitter.setLastupdate(new Date());
        twitter.merge();
        // return "redirect:/twitters/" + encodeUrlPathSegment(twitter.getId().toString(), httpServletRequest);
        return showDetailTwitters(twitter.getId(), null, null, uiModel, httpServletRequest);
    }

    @RequestMapping(produces = "text/html")
    public String list(
            @RequestParam(value = "sortExpression", required = false)
            String sortExpression,
            @RequestParam(value = "page", required = false)
            Integer page,
            @RequestParam(value = "size", required = false)
            Integer size,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null)
            return "login";
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        UserAccount tPublisher = UserAccount.findUserAccountByName(tCurName);
        tCurName = tPublisher.getName();
        float nrOfPages;
        if (tCurName.equals("admin")) {
            uiModel.addAttribute("twitters", Twitter.findOrderedTwitterEntries(firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Twitter.countTwitters() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                    : nrOfPages));
        } else {
            Set<Integer> tAuthSet = BigAuthority.getAuthSet(tPublisher, tPublisher);
            uiModel.addAttribute("twitters",
                    Twitter.findTwitterByPublisher(tPublisher, tAuthSet, firstResult, sizeNo, sortExpression));
            nrOfPages = (float) Twitter.countTwitterByPublisher(tPublisher, tAuthSet) / sizeNo;
        }
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                : nrOfPages));
        addDateTimeFormatPatterns(uiModel);

        BigUtil.checkTheme(tPublisher, httpServletRequest);

        return "twitters/list";
    }

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, new Twitter(), httpServletRequest);
        List<String[]> dependencies = new ArrayList<String[]>();
        if (UserAccount.countUserAccounts() == 0) {
            dependencies.add(new String[] { "useraccount", "useraccounts" });
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "twitters/create";
    }

    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(
            @PathVariable("id")
            Long id,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, Twitter.findTwitter(id), httpServletRequest);
        return "twitters/update";
    }

    @RequestMapping(params = "twitterid", produces = "text/html")
    public String showDetailTwitters(
            @RequestParam(value = "twitterid", required = false)
            Long twitterid,
            @RequestParam(value = "page", required = false)
            Integer page,
            @RequestParam(value = "size", required = false)
            Integer size,
            Model uiModel,
            HttpServletRequest request) {
        Twitter tTwitter = Twitter.findTwitter(twitterid);
        UserAccount tOwner = tTwitter.getPublisher();

        BigUtil.checkTheme(tOwner, request);

        int sizeNo = size == null ? 20 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
        Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        uiModel.addAttribute("spaceOwner", tOwner);
        float nrOfPages;
        uiModel.addAttribute("twitter", tTwitter);
        uiModel.addAttribute("remarks", Remark.findRemarkByTwitter(tTwitter, tAuthSet, firstResult, sizeNo));
        nrOfPages = (float) Remark.countRemarksByTwitter(tTwitter, tAuthSet) / sizeNo;
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                : nrOfPages));
        Remark tRemark = new Remark();
        uiModel.addAttribute("newremark", tRemark);
        uiModel.addAttribute("authorities", BigAuthority.getRemarkOptions(messageSource, request.getLocale()));
        List<Twitter> remarktos = new ArrayList<Twitter>();
        remarktos.add(tTwitter);
        uiModel.addAttribute("remarktos", remarktos);
        uiModel.addAttribute("refresh_time", 0);
        // not need to check how many new remarks. because when not logged in, user can not set the refresh time.
        // so the new item number make no use to them.
        return "public/list_detail_twitter";
    }

    // =====================================changing images on page=====================================
    @RequestMapping(value = "/getImage/{id}")
    // when a user's theme was set to 0, then this method will be called to get his own images. if he's no image, then
    // use admin's image.
            public
            void getImage(
                    @PathVariable("id")
                    String id,
                    HttpServletRequest request,
                    HttpServletResponse response) {
        PersonalController tController =
                SpringApplicationContext.getApplicationContext()
                        .getBean("personalController", PersonalController.class);
        tController.getImage(id, request, response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(
            @PathVariable("id")
            Long id,
            @RequestParam(value = "page", required = false)
            Integer page,
            @RequestParam(value = "size", required = false)
            Integer size,
            Model uiModel) {
        Twitter twitter = Twitter.findTwitter(id);
        UserAccount tOwner = twitter.getPublisher();
        Set<Integer> authSet = BigAuthority.getAuthSet(tOwner, tOwner);
        List<Remark> remarks = Remark.findRemarkByTwitter(twitter, authSet, 0, 0);
        for (Remark remark : remarks) {
            remark.remove();
        }
        twitter.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/twitters";
    }
}
