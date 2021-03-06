package com.aeiou.bigbang.web;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.model.MediaUpload;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/")
@Controller
public class PersonalController extends BaseController {
    @Inject
    private UserContextService userContextService;

    @RequestMapping(value = "/{ownerName}", produces = "text/html")
    public String index(
            @PathVariable("ownerName") String ownerName,
            Model uiModel,
            HttpServletRequest request) {
        String curName = userContextService.getCurrentUserName(); // the current user.
        UserAccount curUser = curName == null ? null : UserAccount.findUserAccountByName(curName);

        if (BigUtil.systemCommandCheck(ownerName, curUser)) // secrete commands goes here.
            return "public/index";

        UserAccount owner = convertUserNameToUserAccount(ownerName);
        if (owner == null)
            return null;

        swithCurrentOwner(owner, uiModel, request);

        ownerName = owner.getName(); // this name has no utf8 string issue. is a readable one.

        Set<Integer> tAuthSet = BigAuthority.getAuthSet(curUser, owner);

        // Determine if the add_as_friend/unfollow links should be displayed.
        if (curUser != null && curUser.getListento() != null && !ownerName.equals("admin")) {
            boolean isAlreadyFriend = curUser.getListento().contains(owner);
            uiModel.addAttribute("nothireable", isAlreadyFriend ? "true" : "false");
            uiModel.addAttribute("notfireable", isAlreadyFriend ? "false" : "true");
        }
        if (!"admin".equals(ownerName)) {
            String title = owner.getDescription();
            if (!StringUtils.isBlank(title)) {
                uiModel.addAttribute("txt_welcome", title);
            } else {
                uiModel.addAttribute("txt_welcome", request.getSession().getAttribute("default_welcome_text"));
            }

            prepareBookmarks(ownerName, uiModel, request, curUser, owner, tAuthSet);
            prepareNotes(ownerName, uiModel, request, curUser, owner, tAuthSet);
        } else {
            uiModel.addAttribute("txt_welcome", request.getSession().getAttribute("default_welcome_text"));
            List<List<BigTag>> list = BigUtil.prepareAdminSuggestedTagsOnMainPage(uiModel, request);
            List<BigTag> bigBMTagsLeft = list.get(0);
            List<BigTag> bigBMTagsRight = list.get(1);
            List<BigTag> bigNoteTagsLeft = list.get(2);
            List<BigTag> bigNoteTagsRight = list.get(3);

            uiModel.addAttribute("contentsLeft", getContentListByTagList(owner, tAuthSet, bigBMTagsLeft));
            uiModel.addAttribute("contentsRight", getContentListByTagList(owner, tAuthSet, bigBMTagsRight));

            uiModel.addAttribute("twittersLeft", getBlogListByTagList(owner, tAuthSet, bigNoteTagsLeft));
            uiModel.addAttribute("twittersRight", getBlogListByTagList(owner, tAuthSet, bigNoteTagsRight));
        }
        // prepareNotesInStyle1(uiModel, tCurUser, tOwner, tAuthSet);
        // ---------------------------------------------------------------------------------
        // to save the reqeust to requestCache;
        /**
         * If I add @ModelAttribute(), and add HttpServletRequest and HttpServletResponse in params, then will throw out
         * exception (can not resolve a view with name tao), and this method will be called twice. in the second time,
         * the spaceOwner is null, so return a null....so can not use this way.
         */
        // BigAuthenticationSuccessHandler tHandler =
        // SpringApplicationContext.getApplicationContext().getBean("authenticationSuccessHandler",
        // BigAuthenticationSuccessHandler.class);
        // tHandler.getRequestCache().saveRequest(request, response);
        return "public/index";
    }

    private List<List> getContentListByTagList(
            UserAccount owner,
            Set<Integer> authSet,
            List<BigTag> bigBMTags) {
        List<List> contentLists = new ArrayList<List>(); // prepare the contentList for each tag.
        for (BigTag bigTag : bigBMTags) {
            contentLists.add(Content.findContentsByTagAndSpaceOwner(bigTag, owner, authSet, 0, 8, null));
        }
        return contentLists;
    }

    private List<List> getBlogListByTagList(
            UserAccount owner,
            Set<Integer> authSet,
            List<BigTag> bigNoteTags) {
        List<List> blogLists = new ArrayList<List>(); // prepare the contentList for each tag.
        for (BigTag bigTag : bigNoteTags) {
            blogLists.add(Twitter.findTwittersByTagAndSpaceOwner(bigTag, owner, authSet, 0, 8, null));
        }
        return blogLists;
    }

    private void prepareBookmarks(
            String spaceOwner,
            Model uiModel,
            HttpServletRequest request,
            UserAccount tCurUser,
            UserAccount tOwner,
            Set<Integer> tAuthSet) {
        List<String[]> tagsAndNumbers = BigUtil.fetchTagAndNumberFromLayoutStr(tOwner, 0);
        String[] tBigTagStrsLeft = tagsAndNumbers.get(0);
        String[] tBigTagStrsRight = tagsAndNumbers.get(1);
        String[] tNumStrsLeft = tagsAndNumbers.get(2);
        String[] tNumStrsRight = tagsAndNumbers.get(3);

        List<BigTag> tBigTagsLeft = new ArrayList<BigTag>();
        List<BigTag> tBigTagsRight = new ArrayList<BigTag>();
        List<Long> tTagIdsLeft = new ArrayList<Long>();
        List<Long> tTagIdsRight = new ArrayList<Long>();
        // if the layout info in DB is not correct, create it from beginning.
        if (BigUtil.notCorrect(tagsAndNumbers)) {
            List<List> lists = BigUtil.generateDefaultTagsForOwner(request, tOwner, 0);
            tBigTagsLeft = lists.get(0);
            tBigTagsRight = lists.get(1);
            tTagIdsLeft = lists.get(2);
            tTagIdsRight = lists.get(3);
            tNumStrsLeft = null;
            tNumStrsRight = null;
        } else { // prepare the info for view base on the string in db:
            tBigTagsLeft = BigUtil.convertTagStringListToObjList(tBigTagStrsLeft, spaceOwner);
            tBigTagsRight = BigUtil.convertTagStringListToObjList(tBigTagStrsRight, spaceOwner);

            for (BigTag tag : tBigTagsLeft) {
                tTagIdsLeft.add(tag.getId()); // it can not be null, even if admin changed the name of the tags, cause
                                              // it's handled in BigtUtil
            }
            for (BigTag tag : tBigTagsRight) {
                tTagIdsRight.add(tag.getId()); // it can not be null, even if admin changed the name of the tags, cause
                                               // it's handled in BigtUtil
            }
        }

        filterTagsWithAithenticationCheck(tBigTagsLeft, tBigTagsRight, tTagIdsLeft, tTagIdsRight, tCurUser, spaceOwner,
                tOwner);

        List<List> tContentListsLeft = new ArrayList<List>(); // prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>(); // prepare the contentList for each tag.
        for (int i = 0; i < tBigTagsLeft.size(); i++) {
            tContentListsLeft.add(Content.findContentsByTagAndSpaceOwner(tBigTagsLeft.get(i), tOwner, tAuthSet, 0,
                    tNumStrsLeft == null || tNumStrsLeft[i] == null ? 8 : Integer.valueOf(tNumStrsLeft[i]).intValue(),
                    null));
        }
        for (int i = 0; i < tBigTagsRight.size(); i++) {
            tContentListsRight.add(Content.findContentsByTagAndSpaceOwner(tBigTagsRight.get(i), tOwner, tAuthSet, 0,
                    tNumStrsRight == null || tNumStrsRight[i] == null ? 8
                            : Integer.valueOf(tNumStrsRight[i]).intValue(),
                    null));
        }

        uiModel.addAttribute("bigTagsLeft", tBigTagsLeft);
        uiModel.addAttribute("bigTagsRight", tBigTagsRight);
        uiModel.addAttribute("tagIdsLeft", tTagIdsLeft);
        uiModel.addAttribute("tagIdsRight", tTagIdsRight);
        uiModel.addAttribute("contentsLeft", tContentListsLeft);
        uiModel.addAttribute("contentsRight", tContentListsRight);
    }

    private void prepareNotes(
            String ownerName,
            Model uiModel,
            HttpServletRequest request,
            UserAccount curUser,
            UserAccount owner,
            Set<Integer> tAuthSet) {
        List<String[]> tagsAndNumbers = BigUtil.fetchTagAndNumberFromLayoutStr(owner, 1);
        String[] tBigTagStrsLeft = tagsAndNumbers.get(0);
        String[] tBigTagStrsRight = tagsAndNumbers.get(1);
        String[] tNumStrsLeft = tagsAndNumbers.get(2);
        String[] tNumStrsRight = tagsAndNumbers.get(3);

        List<BigTag> tBigTagsLeft = new ArrayList<BigTag>();
        List<BigTag> tBigTagsRight = new ArrayList<BigTag>();
        List<Long> tTagIdsLeft = new ArrayList<Long>();
        List<Long> tTagIdsRight = new ArrayList<Long>();
        // if the layout info in DB is not correct, create it from beginning.
        if (BigUtil.notCorrect(tagsAndNumbers)) {
            List<List> lists = BigUtil.generateDefaultTagsForOwner(request, owner, 1);
            tBigTagsLeft = lists.get(0);
            tBigTagsRight = lists.get(1);
            tTagIdsLeft = lists.get(2);
            tTagIdsRight = lists.get(3);
            tNumStrsLeft = null;
            tNumStrsRight = null;
        } else { // prepare the info for view base on the string in db:
            tBigTagsLeft = BigUtil.convertTagStringListToObjList(tBigTagStrsLeft, ownerName);
            tBigTagsRight = BigUtil.convertTagStringListToObjList(tBigTagStrsRight, ownerName);

            for (BigTag tag : tBigTagsLeft) {
                tTagIdsLeft.add(tag.getId()); // it can not be null, even if admin changed the name of the tags, cause
                                              // it's handled in BigtUtil
            }
            for (BigTag tag : tBigTagsRight) {
                tTagIdsRight.add(tag.getId()); // it can not be null, even if admin changed the name of the tags, cause
                                               // it's handled in BigtUtil
            }
        }

        filterTagsWithAithenticationCheck(tBigTagsLeft, tBigTagsRight, tTagIdsLeft, tTagIdsRight, curUser, ownerName,
                owner);

        List<List> blogListsLeft = new ArrayList<List>(); // prepare the contentList for each tag.
        List<List> blogListsRight = new ArrayList<List>(); // prepare the contentList for each tag.
        for (int i = 0; i < tBigTagsLeft.size(); i++) {
            blogListsLeft.add(Twitter.findTwittersByTagAndSpaceOwner(tBigTagsLeft.get(i), owner, tAuthSet, 0,
                    tNumStrsLeft == null || tNumStrsLeft[i] == null ? 8 : Integer.valueOf(tNumStrsLeft[i]).intValue(),
                    null));
        }
        for (int i = 0; i < tBigTagsRight.size(); i++) {
            blogListsRight.add(Twitter.findTwittersByTagAndSpaceOwner(tBigTagsRight.get(i), owner, tAuthSet, 0,
                    tNumStrsRight == null || tNumStrsRight[i] == null ? 8
                            : Integer.valueOf(tNumStrsRight[i]).intValue(),
                    null));
        }

        uiModel.addAttribute("twitterTagsLeft", tBigTagsLeft);
        uiModel.addAttribute("twitterTagsRight", tBigTagsRight);
        uiModel.addAttribute("twitterTagIdsLeft", tTagIdsLeft);
        uiModel.addAttribute("twitterTagIdsRight", tTagIdsRight);
        uiModel.addAttribute("twittersLeft", blogListsLeft);
        uiModel.addAttribute("twittersRight", blogListsRight);
    }

    private void prepareNotesInStyle1(
            Model uiModel,
            UserAccount tCurUser,
            UserAccount tOwner,
            Set<Integer> tAuthSet) {
        // ====================prepare content for twitter area
        // ============================
        List<Twitter> twitterLeft = Twitter.findTwitterByPublisher(tOwner, tAuthSet, 0, 8, null);
        // for this part it's a little complex: it's about to display the twitters of
        // the owner's friends. not the
        // owner's, so it's not
        // like if the logged in user is owner, then display all, if it's owner's friend
        // friends display more, if it's
        // stranger, then display only public ones.
        // so, can not use the tauthset directly. the logic should be:
        // if current user is owner, then display public and visible to friend ones,
        // otherwise, display only public ones
        tAuthSet = BigAuthority.getAuthSetForTwitterOfFriends(tCurUser, tOwner);
        List<Twitter> twitterRight = null;
        List<Twitter> twitterRightFix = null;
        Set<UserAccount> tTeamSet = tOwner.getListento(); // get all the users that the owner cares.
        if (twitterLeft.size() == 0) {
            twitterRight = Twitter.findTwitterByOwner(tTeamSet, tAuthSet, 9, 8, null);
            twitterRightFix = Twitter.findTwitterByOwner(tTeamSet, tAuthSet, 0, 9, null);
        } else {
            twitterRight = Twitter.findTwitterByOwner(tTeamSet, tAuthSet, 0, twitterLeft.size(), null);
        }
        uiModel.addAttribute("twitterLeft", twitterLeft);
        uiModel.addAttribute("twitterRight", twitterRight);
        uiModel.addAttribute("twitterRightFix", twitterRightFix);
    }

    private void filterTagsWithAithenticationCheck(
            List<BigTag> tBigTagsLeft,
            List<BigTag> tBigTagsRight,
            List<Long> tTagIdsLeft,
            List<Long> tTagIdsRight,
            UserAccount tCurUser,
            String spaceOwner,
            UserAccount tOwner) {
        // final adjust---not all tags should be shown to curUser:
        if (tCurUser == null) { // not logged in
            for (int i = tBigTagsLeft.size() - 1; i >= 0; i--) {
                if (tBigTagsLeft.get(i).getAuthority() != 0) {
                    tBigTagsLeft.remove(i);
                    tTagIdsLeft.remove(i);
                }
            }
            for (int i = tBigTagsRight.size() - 1; i >= 0; i--) {
                if (tBigTagsRight.get(i).getAuthority() != 0) {
                    tBigTagsRight.remove(i);
                    tTagIdsRight.remove(i);
                }
            }
        } else {
            String tCurName = tCurUser.getName(); // to avoid the Capital issue.
            // tCurName are not capital
            // sensitive.
            if (!tCurName.equals(spaceOwner)) { // has logged in but not self.
                if (tOwner.getListento().contains(tCurUser)) { // it's team
                                                               // member
                    for (int i = tBigTagsLeft.size() - 1; i >= 0; i--) {
                        if (tBigTagsLeft.get(i).getAuthority() != 0 && tBigTagsLeft.get(i).getAuthority() != 2) {
                            tBigTagsLeft.remove(i);
                            tTagIdsLeft.remove(i);
                        }
                    }
                    for (int i = tBigTagsRight.size() - 1; i >= 0; i--) {
                        if (tBigTagsRight.get(i).getAuthority() != 0 && tBigTagsRight.get(i).getAuthority() != 2) {
                            tBigTagsRight.remove(i);
                            tTagIdsRight.remove(i);
                        }
                    }
                } else { // it's someone else TODO: consider about case 3 in
                         // future.
                    for (int i = tBigTagsLeft.size() - 1; i >= 0; i--) {
                        if (tBigTagsLeft.get(i).getAuthority() != 0) {
                            tBigTagsLeft.remove(i);
                            tTagIdsLeft.remove(i);
                        }
                    }
                    for (int i = tBigTagsRight.size() - 1; i >= 0; i--) {
                        if (tBigTagsRight.get(i).getAuthority() != 0) {
                            tBigTagsRight.remove(i);
                            tTagIdsRight.remove(i);
                        }
                    }
                }
            }
        }
    }

    // ==============================alive check==================================
    @RequestMapping(value = "stgocheck/{keyStr}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> stgocheck(
            @PathVariable("keyStr") String keyStr,
            HttpServletRequest request) {
        if (!keyStr.equals("stgo"))
            return null;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");

        Customize customize =  new Customize();
        return new ResponseEntity<String>(customize.toJson(), headers, HttpStatus.OK);
    }

    // =====================================changing images on
    // page=====================================
    @RequestMapping(value = "/getImage/{id}")
    // when a user's theme was set to 0, then this method will be called to get his
    // own images. if he's no image, then
    // use admin's image.
    public void getImage(
            @PathVariable("id") String id,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.setContentType("image/jpeg");
        MediaUpload tMedia = MediaUpload.findMediaByKey(id);
        if (tMedia == null || tMedia.getContent() == null) {
            if (id.endsWith("_bg"))
                tMedia = MediaUpload.findMediaByKey("uc_admin_bg");
            if (id.endsWith("_headimage"))
                tMedia = MediaUpload.findMediaByKey("uc_admin_headimage");
        }

        if (tMedia != null && tMedia.getContent() != null) {
            try {
                byte[] imageBytes = tMedia.getContent();
                response.getOutputStream().write(imageBytes);
                response.getOutputStream().flush();
            } catch (Exception e) {
                System.out.println("Exception occured when fetching img of ID:" + id + "! " + e);
            }
        } else {
            System.out.println("found no immage with id:" + id);
        }
    }

    @RequestMapping(value = "/changeImgForm", method = RequestMethod.POST)
    public String changeImgForm(
            Model uiModel,
            HttpServletRequest request) {
        uiModel.addAttribute("replacingImage", request.getParameter("position"));
        uiModel.addAttribute("mediaUpload", new MediaUpload());

        UserAccountController tController = SpringApplicationContext.getApplicationContext()
                .getBean("userAccountController", UserAccountController.class);
        return tController.updateForm(Long.parseLong(request.getParameter("returnPath")), uiModel, request);
    }

    @RequestMapping(value = "/mediauploads", method = RequestMethod.POST, produces = "text/html")
    public String create(
            @Valid MediaUpload mediaUpload,
            BindingResult bindingResult,
            Model uiModel,
            @RequestParam("content") CommonsMultipartFile content,
            HttpServletRequest request) {

        String tKeyString = request.getParameter("position"); // can uc_tao_headimage or uc_tao_bg.
        Long ownerID = Long.parseLong(request.getParameter("returnPath")); // the owner must be the logged in user.

        long tCount = -1;
        MediaUpload tMedia;

        // -----------file path---------
        try {
            tMedia = MediaUpload.findMediaByKey(tKeyString);
        } catch (Exception e) {
            e.printStackTrace();
            tMedia = new MediaUpload();
            tMedia.setFilepath(tKeyString);
        }
        FileItem tFileItem = content.getFileItem();
        boolean isSpecial = false;
        // -----------file type-----------------
        String tFormat = BigUtil.DEFAULT_IMAGE_TYPE;
        if (tFileItem != null) {
            String tFileName = tFileItem.getName();
            isSpecial = tFileName.indexOf("stgos.com") >= 0;
            if (tFileName.indexOf("sharethegoodones.com") >= 0) { // check if it is a delete command
                tMedia.remove();
                // change user's them to 0.css
                BigUtil.changeUserTheme(ownerID, 0);
                UserAccountController tController = SpringApplicationContext.getApplicationContext()
                        .getBean("userAccountController", UserAccountController.class);
                return tController.updateForm(ownerID, uiModel, request);
            }

            if (tFileName != null) {
                int tPos = tFileName.lastIndexOf(".");
                if (tPos > 0 && tPos < tFileName.length() - 1) {
                    tFormat = tFileName.substring(tPos);
                }
            }
        }
        tMedia.setContentType(tFormat);

        // ------------if need to save to disk-------------
        // File tDiskFile = new File(serverInfo.getDataPath() + tKeyString + tFormat);
        // content.transferTo(tDiskFile);
        // ------------save image to db------------------
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(content.getInputStream());
            // Image big = inputImage.getScaledInstance(256, 256,Image.SCALE_DEFAULT);
            byte[] tContent = BigUtil.resizeImage(inputImage, tMedia.getFilepath(), tFormat, isSpecial);// tKeyString);
                                                                                                        // //because
                                                                                                        // when
                                                                                                        // uploading
                                                                                                        // gallery, the
                                                                                                        // tKeyString is
                                                                                                        // like
                                                                                                        // "gallery_".
            tMedia.setContent(tContent == null ? content.getBytes() : tContent); // ------------file content----------
            tMedia.setFilesize(tContent == null ? content.getSize() : tContent.length); // ------------file
                                                                                        // size-------------
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("got exception when resizing the image!" + e);
            UserAccountController tController = SpringApplicationContext.getApplicationContext()
                    .getBean("userAccountController", UserAccountController.class);
            return tController.updateForm(ownerID, uiModel, request);
        }

        uiModel.asMap().clear();
        tMedia.persist();

        // change user's them to 0.css
        BigUtil.changeUserTheme(ownerID, 0);

        UserAccountController tController = SpringApplicationContext.getApplicationContext()
                .getBean("userAccountController", UserAccountController.class);
        return tController.updateForm(ownerID, uiModel, request);
    }

    private UserAccount convertUserNameToUserAccount(
            String pName) {
        UserAccount tUserAccount = UserAccount.findUserAccountByName(pName); // make sure the owner exist, and set the
                                                                             // name on title
        if (tUserAccount == null) {
            pName = BigUtil.getUTFString(pName);
            tUserAccount = UserAccount.findUserAccountByName(pName); // bet it might still not UTF8 encoded.
            if (tUserAccount == null)
                return null;
        }
        return tUserAccount;
    }

}
