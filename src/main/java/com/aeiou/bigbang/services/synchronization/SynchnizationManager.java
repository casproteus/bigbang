package com.aeiou.bigbang.services.synchronization;

//import javax.net.ssl.SSLContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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


public class SynchnizationManager {
	
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
			if(tList.size() != 2)
				return -1;

			//useraccount
	        List<UserAccount> tUserAccountList =  new JSONDeserializer<List<UserAccount>>().use(null, ArrayList.class).use("values", UserAccount.class).deserialize(tList.get(0));
	        if(!startToBackUpUserAccountsToLocal(tUserAccountList))
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
		return -1;
	}

	private ArrayList<UserAccount> stackUAprocessing = new ArrayList<UserAccount>();
	private ArrayList<UserAccount> stackUAprocessed = new ArrayList<UserAccount>();
	public boolean startToBackUpUserAccountsToLocal(List<UserAccount> pList){
		stackUAprocessing.clear();	//in case that method exit by exception and the object is shared by application server.
		stackUAprocessed.clear();
		for(int i = 0; i < pList.size(); i++){
			UserAccount pUA = pList.get(i);
			if(stackUAprocessed.contains(pUA) || stackUAprocessing.contains(pUA))//if a useraccount is already in stack, means it's already precessed, ignore it.
				continue;
			stackUAprocessing.add(pUA);
			
			UserAccount tUA = UserAccount.findUserAccountByName(pUA.getName());
			if(tUA != null){	//have same one. update properties
				pUA.setId(tUA.getId());
				if(pUA.getListento() != null && pUA.getListento().size() > 0){
					Set<UserAccount> tSet = pUA.getListento();
					ArrayList<UserAccount> tList = new ArrayList<UserAccount>();
					if(tList.addAll(tSet))
						startToBackUpUserAccountsToLocal(tList);
				}
			}else{				//don not have yet. create a new one.
				pUA.setId(null);	//make the Id null, or the recode with that ID will be replaced.
				if(pUA.getListento() != null && pUA.getListento().size() > 0){
					Set<UserAccount> tSet = pUA.getListento();
					ArrayList<UserAccount> tList = new ArrayList<UserAccount>();
					if(tList.addAll(tSet))
						startToBackUpUserAccountsToLocal(tList);
				}
			}
			pUA.persist();
			stackUAprocessing.remove(pUA);
			stackUAprocessed.add(pUA);
		}
		stackUAprocessing.clear();
		stackUAprocessed.clear();
		return true;
	}
	
	HashMap<String, List<BigTag>> tagMap = new HashMap<String, List<BigTag>>();
	public boolean startToBackUpTagsToLocal(List<BigTag> pList){
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
						pTag.setId(tTag.getId());
						break;
					}
				}
			}else{													//not exist, add new.
				pTag.setId(null);
			}
			pTag.persist();
		}
		tagMap.clear();
		return true;
	}

	HashMap<UserAccount, List<Message>> messageMap = new HashMap<UserAccount, List<Message>>();
	public boolean startToBackUpMessagesToLocal(List<Message> pList){
		messageMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Message pMessage = pList.get(i);
			UserAccount tUA = UserAccount.findUserAccountByName(pMessage.getReceiver().getName());
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
						pMessage.setId(tMessage.getId());
						break;
					}
				}
			}else{
				pMessage.setId(null);
			}
			pMessage.persist();
		}
		messageMap.clear();
		return true;
	}

	HashMap<UserAccount, List<Content>> bookmarkMap = new HashMap<UserAccount, List<Content>>();
	public boolean startToBackUpBookmarksToLocal(List<Content> pList){
		bookmarkMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Content pContent = pList.get(i);
			UserAccount tUA = UserAccount.findUserAccountByName(pContent.getPublisher().getName());
			List<Content> tList = bookmarkMap.get(tUA);
			if(tList != null){
				tList = Content.findContentsByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
				bookmarkMap.put(tUA, tList);
			}
			
			if(tList != null && tList.size() > 0){ //already exist, then update it's properties.
				for (int j = 0; j < tList.size(); j++){
					Content tContent = tList.get(j);
					if(tContent.getSourceURL().equals(pContent.getSourceURL()) && tContent.getTitle().equals(pContent.getTitle())){//already exist, then update it's properties.
						pContent.setId(pContent.getId());
						break;
					}
				}
			}else{
				pContent.setId(null);
			}
			pContent.persist();
		}
		bookmarkMap.clear();
		return true;
	}

	HashMap<UserAccount, List<Twitter>> twitterMap = new HashMap<UserAccount, List<Twitter>>();
	public boolean startToBackUpBlogsToLocal(List<Twitter> pList){
		twitterMap.clear();
		for(int i = 0; i < pList.size(); i++){
			Twitter pTwitter = pList.get(i);
			UserAccount tUA = UserAccount.findUserAccountByName(pTwitter.getPublisher().getName());
			List<Twitter> tList = twitterMap.get(tUA);
			if(tList == null){
				tList = Twitter.findTwitterByPublisher(tUA, BigAuthority.getAuthSet(tUA, tUA), 0, 0, null);
				twitterMap.put(tUA, tList);
			}
			
			if(tList != null && tList.size() > 0){ //already exist, then update it's properties.
				for (int j = 0; j < tList.size(); j++){
					Twitter tTwitter = tList.get(j);
					if(tTwitter.getTwtitle().equals(pTwitter.getTwtitle()) && tTwitter.getTwitDate().equals(pTwitter.getTwitDate())){  //already exist, then update it's properties.
						pTwitter.setId(tTwitter.getId());
						break;
					}
				}
			}else{
				pTwitter.setId(null);
			}
			pTwitter.persist();
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
				for (int j = 0; j < tList.size(); j++){
					Remark tRemark = tList.get(j);
					if(tRemark.getPublisher().getName().equals(pRemark.getPublisher().getName()) && tRemark.getRemarkTime().equals(pRemark.getRemarkTime())){  //already exist, then update it's properties.
						pRemark.setId(tRemark.getId());
						break;
					}
				}
			}else{
				pRemark.setId(null);
			}
			pRemark.persist();
		}
		twitterMap.clear();
		remarkMap.clear();
		return true;
	}
	
	private Twitter findBlogInLocalDB(Remark pRemark){
		Twitter pTwitter = pRemark.getRemarkto();
		UserAccount tUA = UserAccount.findUserAccountByName(pTwitter.getPublisher().getName());
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
