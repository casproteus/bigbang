package com.aeiou.bigbang.services.synchronization;

//import javax.net.ssl.SSLContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.util.BigAuthority;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SynchnizationManager {

    private List<UserAccount> userAccountList;

    public String getRecentlyAddedContent(
            String tUserName,
            String pCommand) {
        Collection<String> collection = new ArrayList<String>();
        if ("1210_syncdb".equals(pCommand) || "1210_syncdb_ua".equals(pCommand)) {
            // useraccount
            String tUserAccountJsonAryStr = UserAccount.toJsonArray(UserAccount.findAllUserAccounts());
            collection.add(tUserAccountJsonAryStr);
        }
        if ("1210_syncdb".equals(pCommand) || "1210_syncdb_tg".equals(pCommand)) {
            // bigtag
            String tBigTagJsonAryStr = BigTag.toJsonArray(BigTag.findAllBigTags());
            collection.add(tBigTagJsonAryStr);
        }
        if ("1210_syncdb".equals(pCommand) || "1210_syncdb_ms".equals(pCommand)) {
            // message
            String tMessageJsonAryStr = Message.toJsonArray(Message.findAllMessages());
            collection.add(tMessageJsonAryStr);
        }
        if ("1210_syncdb".equals(pCommand) || "1210_syncdb_bg".equals(pCommand)) {
            // blog
            String tBlogJsonAryStr = Twitter.toJsonArray(Twitter.findAllTwitters());
            collection.add(tBlogJsonAryStr);
        }
        if ("1210_syncdb".equals(pCommand) || "1210_syncdb_rm".equals(pCommand)) {
            // remark
            String tRemarkJsonAryStr = Remark.toJsonArray(Remark.findAllRemarks());
            collection.add(tRemarkJsonAryStr);
        }
        if ("1210_syncdb".equals(pCommand) || "1210_syncdb_bm".equals(pCommand)) {
            // bookmark
            String tBookMarkJsonAryStr = Content.toJsonArray(Content.findAllContents());
            collection.add(tBookMarkJsonAryStr);
        }
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

    // TODO: Bug1: the local useraccount should not update remote useraccount, because the romote one might
    // be modified already and is newer than local ones. so should check the last update time to know which should be
    // modified.
    public int saveContentIntoLocalDB(
            List<String> tList,
            String pCommand) {
        if ("1210_syncdb".equals(pCommand)) {
            if (!saveUserAccountToLocalDB(tList.get(0))) // useraccount
                return 0;
            if (!saveTagsToLocalDB(tList.get(1))) // biglist
                return 1;
            if (!saveMessagesToLocalDB(tList.get(2))) // message
                return 2;
            if (!saveBlogsToLocalDB(tList.get(3))) // blogs
                return 3;
            if (!saveRemarksToLocalDB(tList.get(4))) // remarks
                return 4;
            if (!saveBookmarksToLocalDB(tList.get(5))) // bookmarks
                return 5;
        } else if ("1210_syncdb_ua".equals(pCommand)) { // useraccount
            if (!saveUserAccountToLocalDB(tList.get(0)))
                return 0;
        } else if ("1210_syncdb_tg".equals(pCommand)) { // bigtag
            if (!saveTagsToLocalDB(tList.get(0)))
                return 1;
        } else if ("1210_syncdb_ms".equals(pCommand)) { // message
            if (!saveMessagesToLocalDB(tList.get(0)))
                return 2;
        } else if ("1210_syncdb_bg".equals(pCommand)) { // blog
            if (!saveBlogsToLocalDB(tList.get(0)))
                return 3;
        } else if ("1210_syncdb_rm".equals(pCommand)) { // remark
            if (!saveRemarksToLocalDB(tList.get(0)))
                return 4;
        } else if ("1210_syncdb_bm".equals(pCommand)) { // bookmark
            if (!saveBookmarksToLocalDB(tList.get(0)))
                return 5;
        }
        return 200;
    }

    private ArrayList<String> stackUAprocessing = new ArrayList<String>();
    private ArrayList<String> stackUAprocessed = new ArrayList<String>();

    public boolean saveUserAccountToLocalDB(
            String pJsonStr) {
        // @NOTE, have to get the uaList out side of the method, because the method will be called recurrencely
        userAccountList =
                new JSONDeserializer<List<UserAccount>>().use(null, ArrayList.class).use("values", UserAccount.class)
                        .deserialize(pJsonStr);
        return startToSaveUAIntoDB(userAccountList);
    }

    private boolean startToSaveUAIntoDB(
            List<UserAccount> pList) {
        System.out.println("start to save UserAccount! there are [" + pList.size()
                + "] items to save---------------------");

        for (int i = 0; i < pList.size(); i++) {
            System.out.println(i);
            UserAccount pUA = pList.get(i);
            // Replace the element with the ones in tUserAccountList, because the infomation can be not complete as
            // those in userAccountList.
            for (int j = 0; j < userAccountList.size(); j++) {
                if (userAccountList.get(j).getName().equals(pUA.getName())) {
                    pUA = userAccountList.get(j);
                    break;
                }
            }

            if (stackUAprocessed.contains(pUA.getName()) || stackUAprocessing.contains(pUA.getName()))// if a unserName
                                                                                                      // is already in
                                                                                                      // stack, means
                                                                                                      // it's already
                                                                                                      // precessed,
                                                                                                      // ignore it.
                continue;
            stackUAprocessing.add(pUA.getName());

            UserAccount tUA = UserAccount.findUserAccountByName(pUA.getName());
            if (tUA != null) { // have same one. update properties
                if (pUA.getListento() != null && pUA.getListento().size() > 0) {
                    Set<UserAccount> tSet = pUA.getListento();
                    ArrayList<UserAccount> tList = new ArrayList<UserAccount>();
                    if (tList.addAll(tSet)) {
                        startToSaveUAIntoDB(tList);
                    }
                    localizeListenTo(tUA, tList); // to make the listened to users localized
                } else
                    tUA.setListento(null);
                tUA.setBalance(pUA.getBalance());
                tUA.setDescription(pUA.getDescription());
                tUA.setEmail(pUA.getEmail());
                tUA.setLayout(pUA.getLayout());
                tUA.setName(pUA.getName());
                tUA.setNewMessageAmount(pUA.getNewMessageAmount());
                tUA.setPassword(pUA.getPassword());
                tUA.setPrice(pUA.getPrice());
                tUA.setStatus(pUA.getStatus());
                tUA.setTheme(pUA.getTheme());
                tUA.setVersion(pUA.getVersion());
                tUA.persist();
            } else { // don not have yet. create a new one.
                pUA.setId(null); // make the Id null, or the recode with that ID will be replaced.
                if (pUA.getListento() != null && pUA.getListento().size() > 0) {
                    Set<UserAccount> tSet = pUA.getListento();
                    ArrayList<UserAccount> tList = new ArrayList<UserAccount>();
                    if (tList.addAll(tSet))
                        startToSaveUAIntoDB(tList);
                    localizeListenTo(pUA, tList); // to make the listened to users localized
                }
                pUA.persist();
            }
            stackUAprocessing.remove(pUA.getName());
            stackUAprocessed.add(pUA.getName());
        }
        return true;
    }

    HashMap<String, List<BigTag>> tagMap = new HashMap<String, List<BigTag>>();

    public boolean saveTagsToLocalDB(
            String pJsonStr) {
        List<BigTag> tBigTagList =
                new JSONDeserializer<List<BigTag>>().use(null, ArrayList.class).use("values", BigTag.class)
                        .deserialize(pJsonStr);
        stackUAprocessing.clear();
        stackUAprocessed.clear(); // can not be cleared in it's own method, cause the method can be iterated several
                                  // times.

        tagMap.clear();
        System.out.println("start to save BigTags! there are [" + tBigTagList.size()
                + "] items to save---------------------");

        for (int i = 0; i < tBigTagList.size(); i++) {
            System.out.println(i);
            BigTag pTag = tBigTagList.get(i);
            if ("admin".equals(pTag.getType())) {
                System.out.println(pTag);
            }
            List<BigTag> tList = tagMap.get(pTag.getType());
            if (tList == null) {
                tList = BigTag.findTagsByPublisher(pTag.getType(), 0, 0, null);
                tagMap.put(pTag.getType(), tList);
            }

            if (tList != null && tList.size() > 0) {
                boolean noMatch = true;
                for (int j = 0; j < tList.size(); j++) {
                    BigTag tTag = tList.get(j);
                    if (tTag.getTagName().equals(pTag.getTagName())) { // already exist, then update it's properties.
                        noMatch = false;
                        tTag.setAuthority(pTag.getAuthority());
                        tTag.setOwner(pTag.getOwner());
                        tTag.setTagName(pTag.getTagName());
                        tTag.setType(pTag.getType());

                        tTag.persist();
                        break;
                    }
                }
                if (noMatch) { // not exist, add new.
                    pTag.setId(null);
                    pTag.persist(); // don't need to set publisher, because it was saved as string instead of an object.
                }
            } else { // not exist, add new.
                pTag.setId(null);
                pTag.persist(); // don't need to set publisher, because it was saved as string instead of an object.
            }
        }
        tagMap.clear();
        return true;
    }

    HashMap<UserAccount, List<Message>> messageMap = new HashMap<UserAccount, List<Message>>();

    public boolean saveMessagesToLocalDB(
            String pJsonStr) {
        List<Message> tMessageList =
                new JSONDeserializer<List<Message>>().use(null, ArrayList.class).use("values", Message.class)
                        .deserialize(pJsonStr);
        messageMap.clear();
        System.out.println("start to save Messages! there are [" + tMessageList.size()
                + "] items to save---------------------");

        for (int i = 0; i < tMessageList.size(); i++) {
            System.out.println(i);
            Message pMessage = tMessageList.get(i);
            UserAccount tUA = findUserInLocalDB(pMessage.getReceiver());
            List<Message> tList = messageMap.get(tUA);
            if (tList == null) {
                tList = Message.findMessageByReceiver(tUA, 0, 0, null);
                messageMap.put(tUA, tList);
            }

            if (tList != null && tList.size() > 0) {
                boolean noMatch = true;
                for (int j = 0; j < tList.size(); j++) {
                    Message tMessage = tList.get(j);
                    if (tMessage.getPublisher().getName().equals(pMessage.getPublisher().getName())
                            && tMessage.getPostTime().equals(pMessage.getPostTime())) { // already exist, then update
                                                                                        // it's properties.
                        noMatch = false;
                        tMessage.setContent(pMessage.getContent());
                        tMessage.persist();
                        break;
                    }
                }
                if (noMatch) {
                    pMessage.setId(null);
                    pMessage.setReceiver(tUA);
                    pMessage.setPublisher(findUserInLocalDB(pMessage.getPublisher()));
                    pMessage.persist();
                }
            } else {
                pMessage.setId(null);
                pMessage.setReceiver(tUA);
                pMessage.setPublisher(findUserInLocalDB(pMessage.getPublisher()));
                pMessage.persist();
            }
        }
        messageMap.clear();
        return true;
    }

    HashMap<UserAccount, List<Twitter>> twitterMap = new HashMap<UserAccount, List<Twitter>>();

    public boolean saveBlogsToLocalDB(
            String pJsonStr) {
        List<Twitter> tBlogList =
                new JSONDeserializer<List<Twitter>>().use(null, ArrayList.class).use("values", Twitter.class)
                        .deserialize(pJsonStr);
        twitterMap.clear();
        System.out.println("start to save Blogs! there are [" + tBlogList.size()
                + "] items to save---------------------");

        for (int i = 0; i < tBlogList.size(); i++) {
            System.out.println(i);
            Twitter pTwitter = tBlogList.get(i);
            System.out.println("item content:" + pTwitter);
            UserAccount tUA = findUserInLocalDB(pTwitter.getPublisher());
            List<Twitter> tList = twitterMap.get(tUA);
            if (tList == null) {
                System.out.println("not in map yet, finding by publisher " + tUA.getName());
                tList = Twitter.findTwitterByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
                twitterMap.put(tUA, tList);
            }

            if (tList != null && tList.size() > 0) { // already exist, then update it's properties.
                System.out.println("this guy has " + tList.size() + " blogs.");
                boolean noMatch = true;
                for (int j = 0; j < tList.size(); j++) {
                    Twitter tTwitter = tList.get(j);
                    if (tTwitter.getTwtitle().equals(pTwitter.getTwtitle())
                            && tTwitter.getTwitDate().equals(pTwitter.getTwitDate())) { // already exist, then update
                                                                                        // it's properties.
                        System.out.println("and find one match!");
                        noMatch = false;
                        tTwitter.setAuthority(pTwitter.getAuthority());
                        tTwitter.setTwittertag(findTagInLocalDB(pTwitter.getTwittertag()));
                        tTwitter.setLastupdate(pTwitter.getLastupdate());
                        tTwitter.setTwitent(pTwitter.getTwitent());
                        System.out.println("now to persist the matched one!");
                        tTwitter.persist();
                        break;
                    }
                }
                if (noMatch) {
                    System.out.println("while no one match :(");
                    pTwitter.setId(null);
                    pTwitter.setPublisher(tUA);
                    pTwitter.setTwittertag(findTagInLocalDB(pTwitter.getTwittertag()));
                    System.out.println("so save as a new one!");
                    pTwitter.persist();
                }
            } else {
                System.out.println("This guy dosen't have any blog yet.");
                pTwitter.setId(null);
                pTwitter.setPublisher(tUA);
                pTwitter.setTwittertag(findTagInLocalDB(pTwitter.getTwittertag()));
                System.out.println("save as a new one.");
                pTwitter.persist();
            }
        }
        // twitterMap.clear(); we don't clear it here, because the coming startToBackUpRemarksToLocal will need this
        // cache.
        return true;
    }

    HashMap<Twitter, List<Remark>> remarkMap = new HashMap<Twitter, List<Remark>>();

    public boolean saveRemarksToLocalDB(
            String pJsonStr) {
        List<Remark> tRemarkList =
                new JSONDeserializer<List<Remark>>().use(null, ArrayList.class).use("values", Remark.class)
                        .deserialize(pJsonStr);
        remarkMap.clear();
        System.out.println("start to save Remarks! there are [" + tRemarkList.size()
                + "] items to save---------------------");

        for (int i = 0; i < tRemarkList.size(); i++) {
            System.out.println(i);
            Remark pRemark = tRemarkList.get(i);
            System.out.println("item content:" + pRemark);
            Twitter tBlog = findBlogInLocalDB(pRemark); // because we sync blog first, so the blog must have exist in
                                                        // local db
            List<Remark> tList = remarkMap.get(tBlog);
            if (tList == null) {
                System.out.println("not in map yet, finding by tBlog " + tBlog);
                tList =
                        Remark.findRemarkByTwitter(tBlog,
                                BigAuthority.getAuthSet(pRemark.getPublisher(), pRemark.getPublisher()), 0, 0);
                remarkMap.put(tBlog, tList);
            }

            if (tList != null && tList.size() > 0) { // already exist, then update it's properties.
                boolean noMatch = true;
                for (int j = 0; j < tList.size(); j++) {
                    Remark tRemark = tList.get(j);
                    if (tRemark.getPublisher().getName().equals(pRemark.getPublisher().getName())
                            && tRemark.getRemarkTime().equals(pRemark.getRemarkTime())) { // already exist, then update
                                                                                          // it's properties.
                        noMatch = false;
                        tRemark.setContent(pRemark.getContent());
                        tRemark.setAuthority(pRemark.getAuthority());
                        tRemark.setRemarkTime(pRemark.getRemarkTime());
                        tRemark.persist();
                        break;
                    }
                }
                if (noMatch) {
                    pRemark.setId(null);
                    pRemark.setRemarkto(tBlog);
                    pRemark.setPublisher(findUserInLocalDB(pRemark.getPublisher()));
                    pRemark.persist();
                }
            } else {
                pRemark.setId(null);
                pRemark.setRemarkto(tBlog);
                pRemark.setPublisher(findUserInLocalDB(pRemark.getPublisher()));
                pRemark.persist();
            }

            if (pRemark.getRemarkTime().compareTo(tBlog.getLastupdate()) > 0) {
                tBlog.setLastupdate(pRemark.getRemarkTime());
                tBlog.persist();
            }
        }
        twitterMap.clear();
        remarkMap.clear();
        return true;
    }

    HashMap<UserAccount, List<Content>> bookmarkMap = new HashMap<UserAccount, List<Content>>();

    public boolean saveBookmarksToLocalDB(
            String pJsonStr) {
        List<Content> tBookMarkList =
                new JSONDeserializer<List<Content>>().use(null, ArrayList.class).use("values", Content.class)
                        .deserialize(pJsonStr);
        bookmarkMap.clear();
        System.out.println("start to save contents! there are [" + tBookMarkList.size()
                + "] items to save---------------------");

        for (int i = 0; i < tBookMarkList.size(); i++) {
            Content pContent = tBookMarkList.get(i);
            System.out.println(i + pContent.getTitle() + pContent.getCommonBigTag());
            UserAccount tUA = findUserInLocalDB(pContent.getPublisher());
            if (tUA == null) {
                System.out.println("Error! the publisher was parsered to null! content:" + pContent);
                continue;
            }
            List<Content> tList = bookmarkMap.get(tUA);
            if (tList == null) {
                tList = Content.findContentsByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
                bookmarkMap.put(tUA, tList);
            }

            if (tList != null && tList.size() > 0) { // already exist, then update it's properties.
                boolean noMatch = true;
                for (int j = 0; j < tList.size(); j++) {
                    Content tContent = tList.get(j);
                    if (tContent.getSourceURL().equals(pContent.getSourceURL())
                            && tContent.getTitle().equals(pContent.getTitle())) {// already exist, then update it's
                                                                                 // properties.
                        noMatch = false;
                        tContent.setAuthority(pContent.getAuthority());
                        tContent.setCommonBigTag(findTagInLocalDB(pContent.getCommonBigTag()));
                        tContent.setUncommonBigTag(findTagInLocalDB(pContent.getUncommonBigTag()));
                        tContent.setConentCache(pContent.getConentCache());
                        tContent.setMarkDate(pContent.getMarkDate());
                        tContent.persist();
                        break;
                    }
                }
                if (noMatch) {
                    pContent.setId(null);
                    pContent.setPublisher(tUA);
                    pContent.setCommonBigTag(findTagInLocalDB(pContent.getCommonBigTag()));
                    if (pContent.getUncommonBigTag() != null)
                        pContent.setUncommonBigTag(findTagInLocalDB(pContent.getUncommonBigTag()));
                    pContent.persist();
                }
            } else {
                pContent.setId(null);
                pContent.setPublisher(tUA);
                if (pContent.getCommonBigTag() != null) // @Not supposed to be null, while incase the db on client side
                                                        // or json serialization got error.
                    pContent.setCommonBigTag(findTagInLocalDB(pContent.getCommonBigTag()));
                if (pContent.getUncommonBigTag() != null)
                    pContent.setUncommonBigTag(findTagInLocalDB(pContent.getUncommonBigTag()));
                pContent.persist();
            }
        }
        bookmarkMap.clear();
        return true;
    }

    private UserAccount localizeListenTo(
            UserAccount tUA,
            ArrayList<UserAccount> pList) {
        Set<UserAccount> tSet = tUA.getListento();
        if (tSet == null) {
            tSet = new HashSet<UserAccount>();
            tUA.setListento(tSet);
        } else
            tSet.clear();

        for (int i = 0; i < pList.size(); i++)
            tUA.getListento().add(findUserInLocalDB(pList.get(i)));
        return tUA;
    }

    private UserAccount findUserInLocalDB(
            UserAccount pUA) {
        if (pUA == null) // some times, when string is big, some user (like Jane) might be parsered to null.
            return null;
        UserAccount tUA = UserAccount.findUserAccountByName(pUA.getName());
        // Because already synced useraccount with server, so, if not in DB yet, then must be unknow remarkers or
        // message senders,
        // save a simple one on local, with no relation to other people.
        if (tUA == null) {
            pUA.setId(null);
            pUA.setListento(null);
            pUA.persist();
            return pUA;
        }
        return tUA;
    }

    private BigTag findTagInLocalDB(
            BigTag pTag) {
        if (pTag == null)
            return null;
        return BigTag.findTagByNameAndOwner(pTag.getTagName(), pTag.getType());
    }

    private Twitter findBlogInLocalDB(
            Remark pRemark) {
        Twitter pTwitter = pRemark.getRemarkto();
        UserAccount tUA = findUserInLocalDB(pTwitter.getPublisher());
        List<Twitter> tList = twitterMap.get(tUA);
        if (tList == null) {
            tList = Twitter.findTwitterByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
            twitterMap.put(tUA, tList);
        }

        if (tList != null && tList.size() > 0) { // already exist, then update it's properties.
            for (int j = 0; j < tList.size(); j++) {
                Twitter tTwitter = tList.get(j);
                if (tTwitter.getTwtitle().equals(pTwitter.getTwtitle())) {
                    if (tTwitter.getTwitDate().toString().equals(pTwitter.getTwitDate().toString())) // already exist,
                                                                                                     // then update it's
                                                                                                     // properties.
                        return tTwitter;
                }
            }
        }

        return null;
    }
}
