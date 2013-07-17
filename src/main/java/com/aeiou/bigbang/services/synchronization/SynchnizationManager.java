package com.aeiou.bigbang.services.synchronization;

//import javax.net.ssl.SSLContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;


public class SynchnizationManager {
	
	private List<UserAccount> userAccountList;

    public String getRecentlyAddedContent(){
    	Collection<String> collection =  new ArrayList<String>();
        //useraccount
        String tUserAccountJsonAryStr = UserAccount.toJsonArray(UserAccount.findAllUserAccounts());
        collection.add(tUserAccountJsonAryStr);
        //bigtag
        String tBigTagJsonAryStr = BigTag.toJsonArray(BigTag.findAllBigTags());
        collection.add(tBigTagJsonAryStr);
        //message
        String tMessageJsonAryStr = Message.toJsonArray(Message.findAllMessages());
        collection.add(tMessageJsonAryStr);
        //bookmark
        String tBookMarkJsonAryStr = Content.toJsonArray(Content.findAllContents());
        collection.add(tBookMarkJsonAryStr);
        //blog
        String tBlogJsonAryStr = Twitter.toJsonArray(Twitter.findAllTwitters());
        collection.add(tBlogJsonAryStr);
        //remark
        String tRemarkJsonAryStr = Remark.toJsonArray(Remark.findAllRemarks());
        collection.add(tRemarkJsonAryStr);
        
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
	public int startToSynch(){
		ClientConfig config = new DefaultClientConfig();
		
//		SSLContext ctx = SSLContext.getInstance("SSL");
//		ctx.init(null, myTrustManager, null);
//		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hostnameVerifier, ctx));
		
		Client client = Client.create(config);				//if we need a ssh, can go on work in this part.
		WebResource webResource = client.resource("http://localhost/bigbang/useraccounts/jsonArray");		//have to add an value: /jsonArray, otherwise, the
		webResource.accept("application/json");																// other method in controller will be called.
		
		//webResource.method("POST");	//this method will call onto the remote server, duplicated with webResource.post();
		ClientResponse response = webResource.post(ClientResponse.class);
		if(200 == response.getStatus()){
			List<String> tList = new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class).deserialize((String)response.getEntity(String.class));
			if(tList.size() != 6)
				return -1;
			return saveContentIntoLocalDB(tList);
		}
		return -1;
	}

	public int saveContentIntoLocalDB(List<String> tList){
		//useraccount
	    userAccountList =  new JSONDeserializer<List<UserAccount>>().use(null, ArrayList.class).use("values", UserAccount.class).deserialize(tList.get(0));
	    if(!startToBackUpUserAccountsToLocal(userAccountList))
	    	return 0;
	    //biglist
	    List<BigTag> tBigTagList =  new JSONDeserializer<List<BigTag>>().use(null, ArrayList.class).use("values", BigTag.class).deserialize(tList.get(1));
	    if(!startToBackUpTagsToLocal(tBigTagList))
	    	return 1;
		//message
	    List<Message> tMessageList =  new JSONDeserializer<List<Message>>().use(null, ArrayList.class).use("values", Message.class).deserialize(tList.get(2));
	    if(!startToBackUpMessagesToLocal(tMessageList))
	    	return 2;
		//bookmarks
	    List<Content> tBookMarkList =  new JSONDeserializer<List<Content>>().use(null, ArrayList.class).use("values", Content.class).deserialize(tList.get(3));
	    if(!startToBackUpBookmarksToLocal(tBookMarkList))
	    	return 3;
	    //blogs
	    List<Twitter> tBlogList =  new JSONDeserializer<List<Twitter>>().use(null, ArrayList.class).use("values", Twitter.class).deserialize(tList.get(4));
	    if(!startToBackUpBlogsToLocal(tBlogList))
	    	return 4;
	    //remarks
	    List<Remark> tRemarkList =  new JSONDeserializer<List<Remark>>().use(null, ArrayList.class).use("values", Remark.class).deserialize(tList.get(5));
	    if(!startToBackUpRemarksToLocal(tRemarkList))
	    	return 5;
		return 200;
	}
	
	private ArrayList<String> stackUAprocessing = new ArrayList<String>();
	private ArrayList<String> stackUAprocessed = new ArrayList<String>();
	public boolean startToBackUpUserAccountsToLocal(List<UserAccount> pList){
		for(int i = 0; i < pList.size(); i++){
			UserAccount pUA = pList.get(i);
			//Replace the element with the ones in tUserAccountList, because the infomation can be not complete as those in userAccountList.
			for(int j = 0; j < userAccountList.size(); j++){
				if(userAccountList.get(j).getName().equals(pUA.getName())){
					pUA = userAccountList.get(j);
					break;
				}
			}
			
			if(stackUAprocessed.contains(pUA.getName()) || stackUAprocessing.contains(pUA.getName()))//if a unserName is already in stack, means it's already precessed, ignore it.
				continue;
			stackUAprocessing.add(pUA.getName());
			
			UserAccount tUA = UserAccount.findUserAccountByName(pUA.getName());
			if(tUA != null){	//have same one. update properties
				if(pUA.getListento() != null && pUA.getListento().size() > 0){
					Set<UserAccount> tSet = pUA.getListento();
					ArrayList<UserAccount> tList = new ArrayList<UserAccount>();
					if(tList.addAll(tSet)){
						startToBackUpUserAccountsToLocal(tList);
					}
					localizeListenTo(tUA, tList);	//to make the listened to users localized
				}else
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
			}else{				//don not have yet. create a new one.
				pUA.setId(null);	//make the Id null, or the recode with that ID will be replaced.
				if(pUA.getListento() != null && pUA.getListento().size() > 0){
					Set<UserAccount> tSet = pUA.getListento();
					ArrayList<UserAccount> tList = new ArrayList<UserAccount>();
					if(tList.addAll(tSet))
						startToBackUpUserAccountsToLocal(tList);
					localizeListenTo(pUA, tList);	//to make the listened to users localized
				}
				pUA.persist();
			}
			stackUAprocessing.remove(pUA.getName());
			stackUAprocessed.add(pUA.getName());
		}
		return true;
	}
	
	HashMap<String, List<BigTag>> tagMap = new HashMap<String, List<BigTag>>();
	public boolean startToBackUpTagsToLocal(List<BigTag> pList){
		stackUAprocessing.clear(); 
		stackUAprocessed.clear();	//can not be cleared in it's own method, cause the method can be iterated several times.
		
		tagMap.clear();
		for(int i = 0; i < pList.size(); i++){
			BigTag pTag = pList.get(i);
			List<BigTag> tList = tagMap.get(pTag.getType());
			if(tList == null){
				tList = BigTag.findTagsByPublisher(pTag.getType(), 0, 0);
				tagMap.put(pTag.getType(), tList);
			}
			
			if(tList != null && tList.size() > 0){
				for (int j = 0; j < tList.size(); j++){
					BigTag tTag = tList.get(j);
					if(tTag.getTagName().equals(pTag.getTagName())){ //already exist, then update it's properties.
						tTag.setAuthority(pTag.getAuthority());
						tTag.setOwner(pTag.getOwner());
						tTag.setTagName(pTag.getTagName());
						tTag.setType(pTag.getType());
						
						tTag.persist();
						break;
					}
				}
			}else{													//not exist, add new.
				pTag.setId(null);
				pTag.persist();			//don't need to set publisher, because it was saved as string instead of an object.
			}
		}
		tagMap.clear();
		return true;
	}

	HashMap<UserAccount, List<Message>> messageMap = new HashMap<UserAccount, List<Message>>();
	public boolean startToBackUpMessagesToLocal(List<Message> pList){
		messageMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Message pMessage = pList.get(i);
			UserAccount tUA = findUserInLocalDB(pMessage.getReceiver());
			List<Message> tList = messageMap.get(tUA);
			if(tList == null){
				tList = Message.findMessageByReceiver(tUA, 0, 0);
				messageMap.put(tUA, tList);
			}
			
			if(tList != null && tList.size() > 0){
				for (int j = 0; j < tList.size(); j++){
					Message tMessage = tList.get(j);
					if(tMessage.getPublisher().getName().equals(pMessage.getPublisher().getName())
					&& tMessage.getPostTime().equals(pMessage.getPostTime())){  //already exist, then update it's properties.
						tMessage.setContent(pMessage.getContent());
						tMessage.persist();
						break;
					}
				}
			}else{
				pMessage.setId(null);
				pMessage.setReceiver(tUA);
				pMessage.setPublisher(findUserInLocalDB(pMessage.getPublisher()));
				pMessage.persist();
			}
		}
		messageMap.clear();
		return true;
	}

	HashMap<UserAccount, List<Content>> bookmarkMap = new HashMap<UserAccount, List<Content>>();
	public boolean startToBackUpBookmarksToLocal(List<Content> pList){
		bookmarkMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Content pContent = pList.get(i);
			UserAccount tUA = findUserInLocalDB(pContent.getPublisher());
			List<Content> tList = bookmarkMap.get(tUA);
			if(tList == null){
				tList = Content.findContentsByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
				bookmarkMap.put(tUA, tList);
			}
			
			if(tList != null && tList.size() > 0){ //already exist, then update it's properties.
				boolean noMatch = true;
				for (int j = 0; j < tList.size(); j++){
					Content tContent = tList.get(j);
					if(tContent.getSourceURL().equals(pContent.getSourceURL()) && tContent.getTitle().equals(pContent.getTitle())){//already exist, then update it's properties.
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
				if(noMatch){
					pContent.setId(null);
					pContent.setPublisher(tUA);
					pContent.setCommonBigTag(findTagInLocalDB(pContent.getCommonBigTag()));
					if(pContent.getUncommonBigTag() != null)
						pContent.setUncommonBigTag(findTagInLocalDB(pContent.getUncommonBigTag()));
					pContent.persist();
				}
			}else{
				pContent.setId(null);
				pContent.setPublisher(tUA);
				pContent.setCommonBigTag(findTagInLocalDB(pContent.getCommonBigTag()));
				if(pContent.getUncommonBigTag() != null)
					pContent.setUncommonBigTag(findTagInLocalDB(pContent.getUncommonBigTag()));
				pContent.persist();
			}
		}
		bookmarkMap.clear();
		return true;
	}

	HashMap<UserAccount, List<Twitter>> twitterMap = new HashMap<UserAccount, List<Twitter>>();
	public boolean startToBackUpBlogsToLocal(List<Twitter> pList){
		twitterMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Twitter pTwitter = pList.get(i);
			UserAccount tUA = findUserInLocalDB(pTwitter.getPublisher());
			List<Twitter> tList = twitterMap.get(tUA);
			if(tList == null){
				tList = Twitter.findTwitterByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
				twitterMap.put(tUA, tList);
			}
			
			if(tList != null && tList.size() > 0){ //already exist, then update it's properties.
				boolean noMatch = true;
				for (int j = 0; j < tList.size(); j++){
					Twitter tTwitter = tList.get(j);
					if(tTwitter.getTwtitle().equals(pTwitter.getTwtitle()) && tTwitter.getTwitDate().equals(pTwitter.getTwitDate())){  //already exist, then update it's properties.
						noMatch = false;
						tTwitter.setAuthority(pTwitter.getAuthority());
						tTwitter.setTwittertag(findTagInLocalDB(pTwitter.getTwittertag()));
						tTwitter.setLastupdate(pTwitter.getLastupdate());
						tTwitter.setTwitent(pTwitter.getTwitent());
						tTwitter.persist();
						break;
					}
				}
				if(noMatch){
					pTwitter.setId(null);
					pTwitter.setPublisher(tUA);
					pTwitter.setTwittertag(findTagInLocalDB(pTwitter.getTwittertag()));
					pTwitter.persist();
				}
			}else{
				pTwitter.setId(null);
				pTwitter.setPublisher(tUA);
				pTwitter.setTwittertag(findTagInLocalDB(pTwitter.getTwittertag()));
				pTwitter.persist();
			}
		}
		//twitterMap.clear(); we don't clear it here, because the coming startToBackUpRemarksToLocal will need this cache.
		return true;
	}

	HashMap<Twitter, List<Remark>> remarkMap = new HashMap<Twitter, List<Remark>>();
	public boolean startToBackUpRemarksToLocal(List<Remark> pList){
		remarkMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Remark pRemark = pList.get(i);
			Twitter tBlog = findBlogInLocalDB(pRemark);	//because we sync blog first, so the blog must have exist in local db
			List<Remark> tList = remarkMap.get(tBlog);
			if(tList == null){
				tList = Remark.findRemarkByTwitter(tBlog, BigAuthority.getAuthSet(pRemark.getPublisher(), pRemark.getPublisher()), 0, 0);
				remarkMap.put(tBlog, tList);
			}
			
			if(tList != null && tList.size() > 0){ //already exist, then update it's properties.
				boolean noMatch = true;
				for (int j = 0; j < tList.size(); j++){
					Remark tRemark = tList.get(j);
					if(tRemark.getPublisher().getName().equals(pRemark.getPublisher().getName()) && tRemark.getRemarkTime().equals(pRemark.getRemarkTime())){  //already exist, then update it's properties.
						noMatch = false;
						tRemark.setContent(pRemark.getContent());
						tRemark.setAuthority(pRemark.getAuthority());
						tRemark.setRemarkTime(pRemark.getRemarkTime());
						tRemark.persist();
						break;
					}
				}
				if(noMatch){
					pRemark.setId(null);
					pRemark.setRemarkto(tBlog);
					pRemark.setPublisher(findUserInLocalDB(pRemark.getPublisher()));
					pRemark.persist();
				}
			}else{
				pRemark.setId(null);
				pRemark.setRemarkto(tBlog);
				pRemark.setPublisher(findUserInLocalDB(pRemark.getPublisher()));
				pRemark.persist();
			}
		}
		twitterMap.clear();
		remarkMap.clear();
		return true;
	}
	
	private UserAccount localizeListenTo(UserAccount tUA, ArrayList<UserAccount> pList){
		Set<UserAccount> tSet = tUA.getListento();
		if(tSet == null){
			tSet = new HashSet<UserAccount>();
			tUA.setListento(tSet);
		}else
			tSet.clear();
		
		for(int i = 0; i < pList.size(); i++)
			tUA.getListento().add(findUserInLocalDB(pList.get(i)));
		return tUA; 
	}
	
	private UserAccount findUserInLocalDB(UserAccount pUA){
		UserAccount tUA = UserAccount.findUserAccountByName(pUA.getName());
		//Because already synced useraccount with server, so, if not in DB yet, then must be unknow remarkers or message senders,
		//save a simple one on local, with no relation to other people.
		if(tUA == null){
			pUA.setId(null);
			pUA.setListento(null);
			pUA.persist();
			return pUA;
		}
		return tUA;
	}
	
	private BigTag findTagInLocalDB(BigTag pTag){
		if(pTag == null)
			return null;
		return BigTag.findBMTagByNameAndOwner(pTag.getTagName(), pTag.getType());
	}
	
	private Twitter findBlogInLocalDB(Remark pRemark){
		Twitter pTwitter = pRemark.getRemarkto();
		UserAccount tUA = findUserInLocalDB(pTwitter.getPublisher());
		List<Twitter> tList = twitterMap.get(tUA);
		if(tList == null){
			tList = Twitter.findTwitterByPublisher(tUA, BigAuthority.getAuthSet(tUA,tUA), 0, 0, null);
			twitterMap.put(tUA, tList);
		}
		
		if(tList != null && tList.size() > 0){ //already exist, then update it's properties.
			for (int j = 0; j < tList.size(); j++){
				Twitter tTwitter = tList.get(j);
				if(tTwitter.getTwtitle().equals(pTwitter.getTwtitle()) && tTwitter.getTwitDate().equals(pTwitter.getTwitDate())){  //already exist, then update it's properties.
					return tTwitter;
				}
			}
		}
		
		return null;
	}
}
