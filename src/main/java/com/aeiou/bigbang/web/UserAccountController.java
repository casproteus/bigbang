package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.model.MediaUpload;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.services.synchronization.SynchnizationManager;
import com.aeiou.bigbang.util.BigAuthority;
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

        // if name changed, need to check if it's available, update the tags and update the image paths.

        UserAccount oldAccount = UserAccount.findUserAccount(userAccount.getId());
        String oldName = oldAccount.getName();
        String newName = userAccount.getName();
        if (!oldName.equals(newName)) {
            if (UserAccount.findUserAccountByName(newName) != null) {
                uiModel.addAttribute("create_error", "name used by others, please choose an other one.");
                userAccount.setName("");
                uiModel.addAttribute("userAccount", userAccount);
                return "useraccounts/update";
            }

            List<BigTag> tBigTags = BigTag.findTagsByPublisher(oldName, 0, 1000, null);
            for (int i = tBigTags.size() - 1; i > -1; i--) {
                tBigTags.get(i).setType(newName);
                tBigTags.get(i).merge();
            }
        }
        uiModel.asMap().clear();

        if (!oldName.equals(newName)) {
            if (MediaUpload.countMediaUploadsByKey(oldName) > 0) {
                MediaUpload tMH = MediaUpload.findMediaByKey(oldName + "_headimg");
                if (tMH != null) {
                    tMH.setFilepath(newName + "_headimg");
                    tMH.merge();
                }
                MediaUpload tMB = MediaUpload.findMediaByKey(oldName + "_bg");
                if (tMB != null) {
                    tMB.setFilepath(userAccount.getName() + "_bg");
                    tMB.merge();
                }
            }
        }

        oldAccount.setName(newName);
        oldAccount.setPassword(userAccount.getPassword());
        oldAccount.setEmail(userAccount.getEmail());
        oldAccount.setDescription(userAccount.getDescription());

        if ("admin".equals(userContextService.getCurrentUserName())) {
            oldAccount.setLayout(userAccount.getLayout());
            oldAccount.setNoteLayout(userAccount.getNoteLayout());
            oldAccount.setTheme(userAccount.getTheme());
        }

        oldAccount.persist();
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

    @RequestMapping(value = "/loglog", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> testLog(
            @RequestBody
            String json) {
        SynchnizationManager tSyncManager = new SynchnizationManager();

        String userName = "JustPrint";
        UserAccount userAccount = makeSureUserCreated(userName);

        // to make sure twitter exist.
        Set<Integer> authSet = BigAuthority.getAuthSet(userAccount, userAccount);
        List<Twitter> twitters = Twitter.findTwitterByPublisher(userAccount, authSet, 0, 0, "o.lastupdate ASC");
        Twitter twitter = null;
        if (twitters != null && twitters.size() > 0) {
            twitter = twitters.get(0);
        } else {
            twitter = new Twitter();
            twitter.setLastupdate(new Date());
            twitter.setPublisher(userAccount);
            twitter.setTwitDate(new Date());
            twitter.setTwtitle("JustPrint Logs");
            twitter.setTwitent("This blog is used to record JustPrint Logs!");
            twitter.persist();
        }

        // add the new remark base on content in param: {"tag":"OrderIdMarkViewHolder","msg":"item%3A+0select%3A+false"}
        if (json != null && json.length() > 0) {
            UserAccount publisher = null;
            int p = json.indexOf("\"tag\"");
            if (p > -1) {
                int startP = p + 7;
                p = json.indexOf("\"msg\"");
                if (p > -1) {
                    int endP = p - 2;
                    publisher = makeSureUserCreated(json.substring(startP, endP));
                }
            }
            // get the content
            if (p > -1) {
                json = json.substring(p + 7);
                json = json.substring(0, json.length() - 2);
            }

            Remark remark = new Remark();
            remark.setAuthority(0);
            remark.setContent(json);
            remark.setPublisher(publisher);
            remark.setRemarkTime(new Date());
            remark.setRemarkto(twitter);
            remark.persist();

            // check if threre's too many:
            List<Remark> remarks = Remark.findRemarkByTwitter(twitter, authSet, 0, 0);
            if (remarks.size() > 100) {// keep only 100 logs.
                remarks.get(100).remove();
            }
        } else {
            logFormatError();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(tSyncManager.getRecentlyAddedContent("", "1210_syncdb_rm"), headers,
                HttpStatus.OK);
    }

    private UserAccount makeSureUserCreated(
            String userName) {
        UserAccount userAccount = UserAccount.findUserAccountByName(userName);
        if (userAccount == null) {
            userAccount = new UserAccount();
            userAccount.setName(userName);
            userAccount.setPassword("asdf");
            userAccount.persist();
        }
        return userAccount;
    }

    private void logFormatError() {

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
