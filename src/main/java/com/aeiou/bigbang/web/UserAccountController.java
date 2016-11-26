package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.model.MediaUpload;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.services.synchronization.SynchnizationManager;
import com.aeiou.bigbang.util.BigUtil;

import flexjson.JSONDeserializer;

@RequestMapping("/useraccounts")
@Controller
@RooWebScaffold(path = "useraccounts", formBackingObject = UserAccount.class)
@RooWebJson(jsonObject = UserAccount.class)
public class UserAccountController extends BaseController {

    @Inject
    private UserContextService userContextService;

    @Inject
    private MessageSource messageSource;

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        UserAccount tUserAccount = new UserAccount();
        tUserAccount.setPrice(1);
        tUserAccount.setBalance(1000);
        populateEditForm(uiModel, tUserAccount, httpServletRequest);
        return "useraccounts/create";
    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(
            @Valid
            UserAccount userAccount,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, userAccount, httpServletRequest);
            return "useraccounts/create";
        }
        UserAccount tUserAccount = UserAccount.findUserAccountByName(userAccount.getName());
        if (tUserAccount == null) {
            uiModel.asMap().clear();
            userAccount.setBalance(1000);
            userAccount.persist();
            // add default messsage.
            addDefaultMessageTwitter(userAccount, httpServletRequest.getLocale());
            return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
        } else {
            uiModel.addAttribute("create_error", "abc");
            return "useraccounts/create";
        }
    }

    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(
            @PathVariable("id")
            Long id,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        UserAccount tOwner = UserAccount.findUserAccount(id);
        populateEditForm(uiModel, tOwner, httpServletRequest);
        uiModel.addAttribute("returnPath", id);

        return "useraccounts/update";
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(
            @Valid
            UserAccount userAccount,
            BindingResult bindingResult,
            Model uiModel,
            HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, userAccount, httpServletRequest);
            return "useraccounts/update";
        }
        UserAccount tUserAccount = UserAccount.findUserAccount(userAccount.getId());
        if (!tUserAccount.getName().equals(userAccount.getName())) {
            List<BigTag> tBigTags = BigTag.findTagsByPublisher(tUserAccount.getName(), 0, 1000, null);
            for (int i = tBigTags.size() - 1; i > -1; i--) {
                tBigTags.get(i).setType(userAccount.getName());
                tBigTags.get(i).merge();
            }
        }
        uiModel.asMap().clear();

        // if name changed, need to update the image paths.
        if (!tUserAccount.getName().equals(userAccount.getName())) {
            if (MediaUpload.countMediaUploadsByKey(tUserAccount.getName()) > 0) {
                MediaUpload tMH = MediaUpload.findMediaByKey(tUserAccount.getName() + "_headimg");
                if (tMH != null) {
                    tMH.setFilepath(userAccount.getName() + "_headimg");
                    tMH.merge();
                }
                MediaUpload tMB = MediaUpload.findMediaByKey(tUserAccount.getName() + "_bg");
                if (tMB != null) {
                    tMB.setFilepath(userAccount.getName() + "_bg");
                    tMB.merge();
                }
            }
        }

        tUserAccount.setName(userAccount.getName());
        tUserAccount.setPassword(userAccount.getPassword());
        tUserAccount.setEmail(userAccount.getEmail());
        tUserAccount.setDescription(userAccount.getDescription());

        if ("admin".equals(tUserAccount.getName())) {
            tUserAccount.setLayout(userAccount.getLayout());
            tUserAccount.setNoteLayout(userAccount.getNoteLayout());
            tUserAccount.setTheme(userAccount.getTheme());
        }

        tUserAccount.persist();
        return "redirect:/useraccounts/" + encodeUrlPathSegment(userAccount.getId().toString(), httpServletRequest);
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
        UserAccount userAccount = UserAccount.findUserAccount(id);
        List<BigTag> tBigTags = BigTag.findTagsByPublisher(userAccount.getName(), 0, 1000, null);
        for (int i = tBigTags.size() - 1; i > -1; i--) {
            tBigTags.get(i).remove();
        }
        userAccount.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/useraccounts";
    }

    @RequestMapping(produces = "text/html")
    public String list(
            @RequestParam(value = "sortExpression", required = false)
            String sortExpression,
            @RequestParam(value = "page", required = false)
            Integer page,
            @RequestParam(value = "size", required = false)
            Integer size,
            Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if (tCurName == null)
            return "login";
        if (tCurName.equalsIgnoreCase("admin")) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("useraccounts",
                    UserAccount.findOrderedUserAccountEntries(firstResult, sizeNo, sortExpression));
            float nrOfPages = (float) UserAccount.countUserAccounts() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                    : nrOfPages));
        } else {
            List<UserAccount> tList = new ArrayList<UserAccount>();
            tList.add(UserAccount.findUserAccountByName(tCurName));
            uiModel.addAttribute("useraccounts", tList);
            uiModel.addAttribute("maxPages", 1);
        }
        return "useraccounts/list";
    }

    private void addDefaultMessageTwitter(
            UserAccount pPublisher,
            Locale pLocale) {
        Message tMessage = new Message();
        tMessage.setReceiver(pPublisher);
        tMessage.setPublisher(UserAccount.findUserAccountByName("admin"));
        tMessage.setPostTime(new Date());
        Object[] tObjAry = new Object[] { pPublisher.getName() };
        tMessage.setContent(messageSource.getMessage("default_welcome_message", tObjAry, pLocale));
        tMessage.persist();
    }

    @RequestMapping(value = "/getImage/{id}")
    /**this method should be called only when user has logged in, and user's cliking the useraccount button on top-right corner.  */
    public void getImage(
            @PathVariable("id")
            String id,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.setContentType("image/jpeg");
        // @TODO(delete):I added chenck in checkTheme method, to add the spaceOwner property again, so here, shouldn't
        // be uc__...", must have value.
        // if("uc__headimage".equals(id) || "uc__bg".equals(id)){
        // if(userContextService.getCurrentUserName() != null){
        // id = userContextService.getCurrentUserName().toLowerCase() + (id.endsWith("_bg") ? "_bg" : "_headimage");
        // id = "uc_" + id;
        // }
        // }

        // NOTE: can not reuse personalController's method, because here, if fond no image, will leave it empty not
        // using admin's image.
        MediaUpload tMedia = MediaUpload.findMediaByKey(id);
        try {
            if (tMedia != null && tMedia.getContent() != null) {
                byte[] imageBytes = tMedia.getContent();
                response.getOutputStream().write(imageBytes);
                response.getOutputStream().flush();
            } else {
                // leave empty. this method will only be called when displaying the updateForm of useraccount to display
                // the image in the dialog,
                // so shall not display admin's default image.
            }
        } catch (Exception e) {
            System.out.println("Exception occured when fetching img of ID:" + id + "! " + e);
        }
    }

    void populateEditForm(
            Model uiModel,
            UserAccount userAccount,
            HttpServletRequest httpServletRequest) {
        uiModel.addAttribute("userAccount", userAccount);
        uiModel.addAttribute("useraccounts", UserAccount.findAllUserAccounts());
        BigUtil.checkTheme(userAccount, httpServletRequest);
    }

    @RequestMapping(value = "/1210_syncdb", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            if (tList.size() == 6)
                tSyncManager.saveContentIntoLocalDB(tList, "1210_syncdb");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb"), headers,
                HttpStatus.OK);
    }

    @RequestMapping(value = "/1210_syncdb_bg", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromBGJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            tSyncManager.saveBlogsToLocalDB(tList.get(0));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_bg"), headers,
                HttpStatus.OK);
    }

    @RequestMapping(value = "/1210_syncdb_ua", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromUAJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            tSyncManager.saveUserAccountToLocalDB(tList.get(0));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_ua"), headers,
                HttpStatus.OK);
    }

    @RequestMapping(value = "/1210_syncdb_tg", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromTGJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            tSyncManager.saveTagsToLocalDB(tList.get(0));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_tg"), headers,
                HttpStatus.OK);
    }

    @RequestMapping(value = "/1210_syncdb_ms", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromMSJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            tSyncManager.saveMessagesToLocalDB(tList.get(0));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_ms"), headers,
                HttpStatus.OK);
    }

    @RequestMapping(value = "/1210_syncdb_rm", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromRMJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            tSyncManager.saveRemarksToLocalDB(tList.get(0));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_rm"), headers,
                HttpStatus.OK);
    }

    @RequestMapping(value = "/1210_syncdb_bm", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromBMJsonArray(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();
        if (json != null && json.length() > 0) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize(json);
            tSyncManager.saveBookmarksToLocalDB(tList.get(0));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_bm"), headers,
                HttpStatus.OK);
    }
}
