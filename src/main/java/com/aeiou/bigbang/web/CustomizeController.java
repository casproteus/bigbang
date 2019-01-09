package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/customizes")
@Controller
@RooWebScaffold(path = "customizes", formBackingObject = Customize.class)
@RooWebJson(jsonObject = Customize.class)
public class CustomizeController {

    @Inject
    private UserContextService userContextService;

    @RequestMapping(params = "relayouttype", produces = "text/html")
    public String displayTagStatus(
            @RequestParam(value = "relayouttype", required = true) String relayouttype,
            HttpServletRequest request,
            Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null) {
            return ("login");
        }
        UserAccount curUser = UserAccount.findUserAccountByName(tCurName);

        List<String[]> visibleNoteTagNameList = BigUtil.fetchTagAndNumberFromLayoutStr(curUser, 1);
        List<BigTag> visibleNoteTagList =
                BigUtil.convertTagStringListToObjList(visibleNoteTagNameList.get(0), tCurName);
        visibleNoteTagList.addAll(BigUtil.convertTagStringListToObjList(visibleNoteTagNameList.get(1), tCurName));

        List<String[]> visibleBMTagNameList = BigUtil.fetchTagAndNumberFromLayoutStr(curUser, 0);
        List<BigTag> visibleBMTagList = BigUtil.convertTagStringListToObjList(visibleBMTagNameList.get(0), tCurName);
        visibleBMTagList.addAll(BigUtil.convertTagStringListToObjList(visibleBMTagNameList.get(1), tCurName));

        List<BigTag> allNoteTags = BigTag.findTagsFromOwnerAndFriend(tCurName, 1);
        List<BigTag> allBMTags = BigTag.findTagsFromOwnerAndFriend(tCurName, 0);
        if (tCurName != "admin") {
            List<List<BigTag>> list = BigUtil.fetchAdminSuggestedAndSelectableTags(request);
            List<BigTag> tagList = list.get(0);
            tagList.addAll(list.get(1));
            for (BigTag tag : tagList) {
                if (tag.getOwner() == 0) {
                    allBMTags.add(tag);
                } else {
                    allNoteTags.add(tag);
                }
            }
        }
        List<BigTag> availableNoteTagList = pickoutUncheckedTagsFromGivenList(curUser.getNoteLayout(), allNoteTags);
        List<BigTag> availableBMTagList = pickoutUncheckedTagsFromGivenList(curUser.getLayout(), allBMTags);

        uiModel.addAttribute("visibleNoteTagList", visibleNoteTagList);
        uiModel.addAttribute("availableNoteTagList", availableNoteTagList);
        uiModel.addAttribute("visibleBMTagList", visibleBMTagList);
        uiModel.addAttribute("availableBMTagList", availableBMTagList);

        BigUtil.checkTheme(curUser, request);
        return "customizes/tagsDisplay";
    }

    private List<BigTag> pickoutUncheckedTagsFromGivenList(
            String layoutString,
            List<BigTag> allTags) {
        List<BigTag> availableUnCheckedTags = new ArrayList<BigTag>();
        for (int i = allTags.size() - 1; i >= 0; i--) {// use back order, because will remove element from list.
            String tTagStr = BigUtil.getLayoutFormatTagString(allTags.get(i));
            if (layoutString != null && layoutString.indexOf(tTagStr) < 0) {
                availableUnCheckedTags.add(allTags.get(i));
                allTags.remove(i);
            }
        }
        return availableUnCheckedTags;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(params = "updateTagsToShow", produces = "text/html")
    public String updateTagsToShow(
            @RequestParam(value = "updateTagsToShow", required = true) String relayouttype,
            HttpServletRequest request,
            Model uiModel) {

        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null) {
            return ("login");
        }
        UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);
        tCurName = tOwner.getName();

        // get the layout info from DB. and split them into string[]

        String layout = reBuildLayoutString(request, tCurName, tOwner.getLayout(), 0);
        tOwner.setLayout(layout);
        layout = reBuildLayoutString(request, tCurName, tOwner.getNoteLayout(), 1);
        tOwner.setNoteLayout(layout);

        tOwner.persist();

        // go to personal page;
        PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController",
                PersonalController.class);
        return tController.index(tCurName, uiModel, request);
    }

    private String reBuildLayoutString(
            HttpServletRequest request,
            String tCurName,
            String tLayout,
            int type) {

        String[] tBigTagStrsLeft = null;
        String[] tBigTagStrsRight = null;
        String[] tNumStrsLeft = null;
        String[] tNumStrsRight = null;

        int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
        if (p > -1) {
            String tTagStr = tLayout.substring(0, p);
            String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);

            p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
            if (p >= 0) {
                tBigTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM); // when an empty string is splited,
                                                                                   // the returned ary will be have one
                                                                                   // element(which is empty).
                tBigTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
            }
            p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
            if (p >= 0) {
                tNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
                tNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
            }
        } // ----------------------------------------

        // no need to check if the layout info in DB is good, because when log in, we
        // display personal page, when
        // display personal page, we'll check the layout string and fix it.

        // if any one in existing list not checked, then remove it from list and number
        // list
        // also remove it from the tListAllTag list, because we'll check the tags left,
        // to see if there's any one setted
        // as checked, we'll add them to the end.
        // List<String> tListAllTag = BigTag.findBMAllTagsStringByOwner(tCurName);
        // List<BigTag> tListAllTag = BigTag.findBMAllTagsByOwner(tCurName);
        @SuppressWarnings("rawtypes")
        Map tMap = new HashMap();
        tMap.putAll(request.getParameterMap()); // this way to make the tMap writable, and it contains all the checked
                                                // item from page.
        StringBuilder tLayoutStrBuilder = new StringBuilder();
        StringBuilder tNumStrBuilder = new StringBuilder();
        boolean tmpFlag = false; // use this flag to make the first time don't add BigUtil.SEP_ITEM.
        if (tBigTagStrsLeft != null) {
            for (int i = 0; i < tBigTagStrsLeft.length; i++) {
                if (tBigTagStrsLeft[i].length() > 0
                        && tMap.get(BigUtil.getTagNameFromLayoutStr(tBigTagStrsLeft[i])) != null) {
                    if (tmpFlag == true) {
                        tLayoutStrBuilder.append(BigUtil.SEP_ITEM);
                        tLayoutStrBuilder.append(tBigTagStrsLeft[i]);
                        tNumStrBuilder.append(BigUtil.SEP_ITEM);
                        tNumStrBuilder.append(tNumStrsLeft[i]);
                    } else {
                        tLayoutStrBuilder.append(tBigTagStrsLeft[i]);
                        tNumStrBuilder.append(tNumStrsLeft[i]);
                        tmpFlag = true;
                    }
                    tMap.remove(BigUtil.getTagNameFromLayoutStr(tBigTagStrsLeft[i]));
                }
            }
        }
        tmpFlag = false;
        tLayoutStrBuilder.append(BigUtil.SEP_LEFT_RIGHT);
        tNumStrBuilder.append(BigUtil.SEP_LEFT_RIGHT);
        if (tBigTagStrsRight != null) {
            for (int i = 0; i < tBigTagStrsRight.length; i++) {
                if (tBigTagStrsRight[i].length() > 0
                        && tMap.get(BigUtil.getTagNameFromLayoutStr(tBigTagStrsRight[i])) != null) {
                    if (tmpFlag == true) {
                        tLayoutStrBuilder.append(BigUtil.SEP_ITEM);
                        tLayoutStrBuilder.append(tBigTagStrsRight[i]);
                        tNumStrBuilder.append(BigUtil.SEP_ITEM);
                        tNumStrBuilder.append(tNumStrsRight[i]);
                    } else {
                        tLayoutStrBuilder.append(tBigTagStrsRight[i]);
                        tNumStrBuilder.append(tNumStrsRight[i]);
                        tmpFlag = true;
                    }
                    tMap.remove(BigUtil.getTagNameFromLayoutStr(tBigTagStrsRight[i]));
                }
            }
        }
        // add new added tags to the end of both taglist and number list.
        Object[] tKeys = tMap.keySet().toArray();
        for (int i = 0; i < tKeys.length; i++) {
            if ("on".equals(((String[]) tMap.get(tKeys[i]))[0])) {
                BigTag bigTag = BigTag.findTagByNameAndOwner((String) tKeys[i], tCurName);
                if (bigTag == null && !"admin".equals(tCurName)) {
                    bigTag = BigTag.findTagByNameAndOwner((String) tKeys[i], "admin");
                }

                if (bigTag == null || bigTag.getOwner() != type) {
                    continue;
                }
                if (tmpFlag == true) {
                    tLayoutStrBuilder.append(BigUtil.SEP_ITEM);
                    tLayoutStrBuilder.append(BigUtil.getLayoutFormatTagString(bigTag));
                    tNumStrBuilder.append(BigUtil.SEP_ITEM);
                    tNumStrBuilder.append("8");
                } else {
                    tLayoutStrBuilder.append(BigUtil.getLayoutFormatTagString(bigTag));
                    tNumStrBuilder.append("8");
                    tmpFlag = true;
                }
            }
        }
        String layout = tLayoutStrBuilder.append(BigUtil.SEP_TAG_NUMBER).append(tNumStrBuilder).toString();
        return layout;
    }

    @RequestMapping(produces = "text/html")
    public String list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Model uiModel) {

        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null) {
            tCurName = "admin";
        }
        UserAccount curUser = UserAccount.findUserAccountByName(tCurName);

        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("customizes", "admin".equals(tCurName)
                    ? Customize.findCustomizeEntries(firstResult, sizeNo, sortFieldName, sortOrder)
                    : Customize.findCustomizeEntriesByOwner(firstResult, sizeNo, sortFieldName, sortOrder, curUser));
            float nrOfPages = (float) Customize.countCustomizes() / sizeNo;
            uiModel.addAttribute("maxPages",
                    (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("customizes",
                    "admin".equals(tCurName) ? Customize.findAllCustomizes(sortFieldName, sortOrder)
                            : Customize.findCustomizesByOwner(curUser));
        }
        return "customizes/list";
    }

    void populateEditForm(
            Model uiModel,
            Customize customize) {
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName != null) {
            UserAccount userAccount = UserAccount.findUserAccountByName(tCurName);
            if (customize.getUseraccount() == null) {
                customize.setUseraccount(userAccount);
            }
        }
        uiModel.addAttribute("customize", customize);
        if ("admin".equals(tCurName)) {
            uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(
            @Valid Customize customize,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, customize);
            return "customizes/create";
        }

        uiModel.asMap().clear();
        if (customize.getUseraccount() == null) {
            customize.setUseraccount(UserAccount.findUserAccountByName(userContextService.getCurrentUserName()));
        }
        customize.persist();
        return "redirect:/customizes/" + encodeUrlPathSegment(customize.getId().toString(), httpServletRequest);
    }
}
