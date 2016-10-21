package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigType;
import com.aeiou.bigbang.util.BigUtil;
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

@RequestMapping("/bigtags")
@Controller
@RooWebScaffold(path = "bigtags", formBackingObject = BigTag.class)
@RooWebJson(jsonObject = BigTag.class)
public class BigTagController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;

    void populateEditForm(
            Model uiModel,
            BigTag bigTag,
            HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("bigTag", bigTag);
        uiModel.addAttribute("authorities", BigAuthority.getAllOptions(messageSource, httpServletRequest.getLocale()));
        uiModel.addAttribute("types", BigType.getAllOptions(bigTag.getOwner(), httpServletRequest.getLocale()));

        String tUserName = userContextService.getCurrentUserName();
        UserAccount tOwner = UserAccount.findUserAccountByName(tUserName);
        BigUtil.checkTheme(tOwner, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(
            @Valid
            BigTag bigTag,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (StringUtils.isEmpty(bigTag.getTagName())) {
            populateEditForm(uiModel, bigTag, httpServletRequest);
            return "bigtags/create";
        }
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        tCurName = tUserAccount.getName();
        if (StringUtils.isEmpty(bigTag.getType())) {
            bigTag.setType(tCurName);
        }
        uiModel.asMap().clear();
        bigTag.persist();
        if (bigTag.getOwner() != null && bigTag.getOwner() == 0) {
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
        return "redirect:/bigtags/" + encodeUrlPathSegment(bigTag.getId().toString(), httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(
            @Valid
            BigTag bigTag,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (StringUtils.isEmpty(bigTag.getTagName())) {
            populateEditForm(uiModel, bigTag, httpServletRequest);
            return "bigtags/update";
        }
        if (bigTag.getOwner() != null && bigTag.getOwner() == 0) {
            BigTag tBigTag = BigTag.findBigTag(bigTag.getId());
            UserAccount tUserAccount = UserAccount.findUserAccountByName(tBigTag.getType());
            String tLayout = tUserAccount == null ? null : tUserAccount.getLayout();
            int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
            if (p > -1) {
                String[] tAryTagStrsLeft = null;
                String[] tAryTagStrsRight = null;
                String[] tAryNumStrsLeft = null;
                String[] tAryNumStrsRight = null;
                String tTagStr = tLayout.substring(0, p);
                String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);
                p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
                if (p >= 0) {
                    tAryTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
                    tAryTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
                }
                p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
                if (p >= 0) {
                    tAryNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
                    tAryNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
                }
                if (BigUtil.notCorrect(tAryTagStrsLeft, tAryTagStrsRight, tAryNumStrsLeft, tAryNumStrsRight)) {
                    BigUtil.resetLayoutString(tUserAccount);
                } else {
                    tTagStr = BigUtil.getTagInLayoutString(tBigTag);
                    String tTagStrNEW = BigUtil.getTagInLayoutString(bigTag);
                    boolean tIsInLeftColumn = false;
                    int tPos;
                    for (tPos = 0; tPos < tAryTagStrsLeft.length; tPos++) {
                        if (tAryTagStrsLeft[tPos].equals(tTagStr)) {
                            tIsInLeftColumn = true;
                            break;
                        }
                    }
                    if (!tIsInLeftColumn) {
                        for (tPos = 0; tPos < tAryTagStrsRight.length; tPos++) {
                            if (tAryTagStrsRight[tPos].equals(tTagStr)) {
                                break;
                            }
                        }
                    }
                    if (tIsInLeftColumn) {
                        String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length];
                        String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length];
                        for (int j = 0; j < tAryTagStrsLeft.length; j++) {
                            if (j != tPos) {
                                tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
                                tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
                            } else {
                                tAryTagStrsLeft2[j] = tTagStrNEW;
                                tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
                            }
                        }
                        tAryTagStrsLeft = tAryTagStrsLeft2;
                        tAryNumStrsLeft = tAryNumStrsLeft2;
                    } else {
                        String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length];
                        String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length];
                        for (int j = 0; j < tAryTagStrsRight.length; j++) {
                            if (j != tPos) {
                                tAryTagStrsRight2[j] = tAryTagStrsRight[j];
                                tAryNumStrsRight2[j] = tAryNumStrsRight[j];
                            } else {
                                tAryTagStrsRight2[j] = tTagStrNEW;
                                tAryNumStrsRight2[j] = tAryNumStrsRight[j];
                            }
                        }
                        tAryTagStrsRight = tAryTagStrsRight2;
                        tAryNumStrsRight = tAryNumStrsRight2;
                    }
                    StringBuilder tStrB = new StringBuilder();
                    StringBuilder tStrB_Num = new StringBuilder();
                    for (int j = 0; j < tAryTagStrsLeft.length; j++) {
                        tStrB.append(tAryTagStrsLeft[j]);
                        tStrB_Num.append(tAryNumStrsLeft[j]);
                        if (j + 1 < tAryTagStrsLeft.length) {
                            tStrB.append(BigUtil.SEP_ITEM);
                            tStrB_Num.append(BigUtil.SEP_ITEM);
                        }
                    }
                    tStrB.append(BigUtil.SEP_LEFT_RIGHT);
                    tStrB_Num.append(BigUtil.SEP_LEFT_RIGHT);
                    for (int j = 0; j < tAryTagStrsRight.length; j++) {
                        tStrB.append(tAryTagStrsRight[j]);
                        tStrB_Num.append(tAryNumStrsRight[j]);
                        if (j + 1 < tAryTagStrsRight.length) {
                            tStrB.append(BigUtil.SEP_ITEM);
                            tStrB_Num.append(BigUtil.SEP_ITEM);
                        }
                    }
                    tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tStrB_Num);
                    tUserAccount.setLayout(tStrB.toString());
                    tUserAccount.persist();
                }
            } else {
                BigUtil.resetLayoutString(tUserAccount);
            }
        }
        uiModel.asMap().clear();
        bigTag.merge();
        return "redirect:/bigtags/" + encodeUrlPathSegment(bigTag.getId().toString(), httpServletRequest);
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
        BigTag bigTag = BigTag.findBigTag(id);
        bigTag.remove();
        if (bigTag.getOwner() != null && bigTag.getOwner() == 0) {
            UserAccount tUserAccount = UserAccount.findUserAccountByName(bigTag.getType());
            String tLayout = tUserAccount == null ? null : tUserAccount.getLayout();
            int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
            if (p > -1) {
                String[] tAryTagStrsLeft = null;
                String[] tAryTagStrsRight = null;
                String[] tAryNumStrsLeft = null;
                String[] tAryNumStrsRight = null;
                String tTagStr = tLayout.substring(0, p);
                String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);
                p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
                if (p >= 0) {
                    tAryTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
                    tAryTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
                }
                p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
                if (p >= 0) {
                    tAryNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
                    tAryNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
                }
                if (BigUtil.notCorrect(tAryTagStrsLeft, tAryTagStrsRight, tAryNumStrsLeft, tAryNumStrsRight)) {
                    BigUtil.resetLayoutString(tUserAccount);
                } else {
                    tTagStr = BigUtil.getTagInLayoutString(bigTag);
                    boolean tIsInLeftColumn = false;
                    int tPos;
                    for (tPos = 0; tPos < tAryTagStrsLeft.length; tPos++) {
                        if (tAryTagStrsLeft[tPos].equals(tTagStr)) {
                            tIsInLeftColumn = true;
                            break;
                        }
                    }
                    if (!tIsInLeftColumn) {
                        for (tPos = 0; tPos < tAryTagStrsRight.length; tPos++) {
                            if (tAryTagStrsRight[tPos].equals(tTagStr)) {
                                break;
                            }
                        }
                    }
                    if (tIsInLeftColumn) {
                        String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length - 1];
                        String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length - 1];
                        for (int j = 0; j < tAryTagStrsLeft.length; j++) {
                            if (j < tPos) {
                                tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
                                tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
                            } else if (j == tPos) {
                                continue;
                            } else {
                                tAryTagStrsLeft2[j - 1] = tAryTagStrsLeft[j];
                                tAryNumStrsLeft2[j - 1] = tAryNumStrsLeft[j];
                            }
                        }
                        tAryTagStrsLeft = tAryTagStrsLeft2;
                        tAryNumStrsLeft = tAryNumStrsLeft2;
                    } else {
                        String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length - 1];
                        String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length - 1];
                        for (int j = 0; j < tAryTagStrsRight.length; j++) {
                            if (j < tPos) {
                                tAryTagStrsRight2[j] = tAryTagStrsRight[j];
                                tAryNumStrsRight2[j] = tAryNumStrsRight[j];
                            } else if (j == tPos) {
                                continue;
                            } else {
                                tAryTagStrsRight2[j - 1] = tAryTagStrsRight[j];
                                tAryNumStrsRight2[j - 1] = tAryNumStrsRight[j];
                            }
                        }
                        tAryTagStrsRight = tAryTagStrsRight2;
                        tAryNumStrsRight = tAryNumStrsRight2;
                    }
                    StringBuilder tStrB = new StringBuilder();
                    StringBuilder tStrB_Num = new StringBuilder();
                    for (int j = 0; j < tAryTagStrsLeft.length; j++) {
                        tStrB.append(tAryTagStrsLeft[j]);
                        tStrB_Num.append(tAryNumStrsLeft[j]);
                        if (j + 1 < tAryTagStrsLeft.length) {
                            tStrB.append(BigUtil.SEP_ITEM);
                            tStrB_Num.append(BigUtil.SEP_ITEM);
                        }
                    }
                    tStrB.append(BigUtil.SEP_LEFT_RIGHT);
                    tStrB_Num.append(BigUtil.SEP_LEFT_RIGHT);
                    for (int j = 0; j < tAryTagStrsRight.length; j++) {
                        tStrB.append(tAryTagStrsRight[j]);
                        tStrB_Num.append(tAryNumStrsRight[j]);
                        if (j + 1 < tAryTagStrsRight.length) {
                            tStrB.append(BigUtil.SEP_ITEM);
                            tStrB_Num.append(BigUtil.SEP_ITEM);
                        }
                    }
                    tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tStrB_Num);
                    tUserAccount.setLayout(tStrB.toString());
                    tUserAccount.persist();
                }
            } else {
                BigUtil.resetLayoutString(tUserAccount);
            }
        }
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/bigtags";
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
        UserAccount tCurUser = UserAccount.findUserAccountByName(tCurName);
        tCurName = tCurUser.getName();
        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
        float nrOfPages;
        if (tCurName.equals("admin")) {
            uiModel.addAttribute("bigtags", BigTag.findOrderedBigTagEntries(firstResult, sizeNo, sortExpression));
            nrOfPages = (float) BigTag.countBigTags() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                    : nrOfPages));
        } else {
            uiModel.addAttribute("bigtags", BigTag.findTagsByPublisher(tCurName, firstResult, sizeNo, sortExpression));
            nrOfPages = (float) BigTag.countTagsByPublisher(tCurName) / sizeNo;
        }
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                : nrOfPages));
        BigUtil.checkTheme(tCurUser, httpServletRequest);
        return "bigtags/list";
    }

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(
            Model uiModel,
            @RequestParam(value = "type", required = false)
            String type,
            HttpServletRequest httpServletRequest) {
        BigTag tBigTag = new BigTag();
        tBigTag.setOwner(("0".equals(type) || "1".equals(type)) ? Integer.valueOf(type) : null);
        populateEditForm(uiModel, tBigTag, httpServletRequest);
        return "bigtags/create";
    }

    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(
            @PathVariable("id")
            Long id,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        populateEditForm(uiModel, BigTag.findBigTag(id), httpServletRequest);
        return "bigtags/update";
    }
}
