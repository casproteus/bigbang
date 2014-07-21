package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;
import com.aeiou.bigbang.web.beans.RefreshBean;

@RequestMapping("/public")
@Controller
public class PublicController{
	
	@Inject
	private UserContextService userContextService;
	@Inject
	private MessageSource messageSource;
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	LogFactory.getLog(PublicController.class).info("Called! PublicController.post is finally called from: " + Thread.getAllStackTraces().toString());
    }
    
    @RequestMapping(produces = "text/html")
    public String index( Model uiModel){
    	
    	String[] tAryTagStrsLeft = null;
    	String[] tAryTagStrsRight = null;
    	String[] tAryNumStrsLeft = null;
    	String[] tAryNumStrsRight = null;
    	List<BigTag> tBigTagsLeft = new ArrayList<BigTag>();
    	List<BigTag> tBigTagsRight = new ArrayList<BigTag>();
    	List<Long> tTagIdsLeft = new ArrayList<Long>();
    	List<Long> tTagIdsRight = new ArrayList<Long>();
    	
    	UserAccount tOwner = UserAccount.findUserAccountByName("admin");
    	String tLayout = tOwner.getLayout();										//get the layout info from DB.
    	int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
		if(p > -1){
			String tTagStr = tLayout.substring(0, p);
			String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);
			
    		p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
    		if(p >=0 ){
    			tAryTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
    			tAryTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
    		}
    		p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
    		if(p >=0 ){
    			tAryNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
    			tAryNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
    		}
		}
    	
		//if the layout info in DB is not good, create it from beginning.
		if(BigUtil.notCorrect(tAryTagStrsLeft, tAryTagStrsRight, tAryNumStrsLeft, tAryNumStrsRight)){
			
	    	List<BigTag> tBigTags = BigTag.findBMTagsByOwner("admin"); 	//fetch out all tags of admin's, 
    		List<Long> tTagIds = new ArrayList<Long>();						//then adjust it. @note: don't know if we can use AthenSet to move this into JPQL, because 
	    	for(int i = 0; i < tBigTags.size(); i++){						//here, we need to compare the tag names, to avoid duplication.
	    		tTagIds.add(tBigTags.get(i).getId());
	    	}
	    	
	    	int tSize = tBigTags.size();									//Separate tags and IDs into 2 columns and prepare the Layout String.
	    	tAryNumStrsLeft = new String[tSize/2];
	    	tAryNumStrsRight = new String[tSize - tSize/2] ;
	    	
	    	StringBuilder tStrB = new StringBuilder();
	    	StringBuilder tStrB_Num = new StringBuilder();
    		for(int j = 0; j < tSize/2; j++){
    			BigTag tTag = tBigTags.get(j);
    	    	tBigTagsLeft.add(tBigTags.get(j));
    	    	tTagIdsLeft.add(tTagIds.get(j));
    	    	
    	    	tStrB.append(BigUtil.getTagInLayoutString(tTag));

    	    	tAryNumStrsLeft[j] = "8";
    	    	tStrB_Num.append(tAryNumStrsLeft[j]);
    	    	
    	    	if(j + 1 < tSize/2){
    	    		tStrB.append(BigUtil.SEP_ITEM);
        	    	tStrB_Num.append(BigUtil.SEP_ITEM);
    	    	}
	    	}

    		tStrB.append(BigUtil.SEP_LEFT_RIGHT);
    		tStrB_Num.append(BigUtil.SEP_LEFT_RIGHT);
    		
    		for(int j = tSize/2; j < tSize; j++){
    			BigTag tTag = tBigTags.get(j);
    			tBigTagsRight.add(tBigTags.get(j));
    	    	tTagIdsRight.add(tTagIds.get(j));
    	    	
    	    	tStrB.append(BigUtil.getTagInLayoutString(tTag));
    	    	
    	    	tAryNumStrsRight[j - tSize/2] = "8";
    	    	tStrB_Num.append(tAryNumStrsRight[j - tSize/2]);
    	    	
    	    	if(j + 1 < tSize){
    	    		tStrB.append(BigUtil.SEP_ITEM);
        	    	tStrB_Num.append(BigUtil.SEP_ITEM);
    	    	}
	    	}
    		tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tStrB_Num);

    		tOwner.setLayout(tStrB.toString());	    						//save to DB
    		tOwner.persist();
		}else{																			//prepare the info for view base on the string in db:
    		tBigTagsLeft = BigUtil.transferToTags(tAryTagStrsLeft, "admin");
    		for(int i = 0; i < tBigTagsLeft.size(); i++){
    			tTagIdsLeft.add(tBigTagsLeft.get(i).getId());
    		}
    		
    		tBigTagsRight = BigUtil.transferToTags(tAryTagStrsRight, "admin");
    		for(int i = 0; i < tBigTagsRight.size(); i++){
    			tTagIdsRight.add(tBigTagsRight.get(i).getId());
    		}
    	}
		
        List<List> tContentListsLeft = new ArrayList<List>();								//prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>();								//prepare the contentList for each tag.
    	for(int i = 0; i < tBigTagsLeft.size(); i++){
    		tContentListsLeft.add(Content.findContentsByTag(tBigTagsLeft.get(i), 0, Integer.valueOf(tAryNumStrsLeft[i]).intValue(), null));
    	}
    	for(int i = 0; i < tBigTagsRight.size(); i++){
    		tContentListsRight.add(Content.findContentsByTag(tBigTagsRight.get(i), 0, Integer.valueOf(tAryNumStrsRight[i]).intValue(), null));
    	}

        uiModel.addAttribute("spaceOwner", "admin");
        uiModel.addAttribute("description", tOwner.getDescription());
        uiModel.addAttribute("bigTagsLeft", tBigTagsLeft);
        uiModel.addAttribute("bigTagsRight", tBigTagsRight);
        uiModel.addAttribute("tagIdsLeft", tTagIdsLeft);
        uiModel.addAttribute("tagIdsRight", tTagIdsRight);
        uiModel.addAttribute("contentsLeft", tContentListsLeft);
        uiModel.addAttribute("contentsRight", tContentListsRight);
        
        return "public/index";
    }
    
    /**
     * We have to use both tag's tagname and type to match out a single tag, because different user can create tags with same name. 
     * if we match content with only tag name, will cause mistake when clicking the "more" button from personal space. 
     * so we have to use tag's ID to match content.
     * @param page
     * @param size
     * @param uiModel
     * @param sortExpression TODO
     * @param tag
     * @return
     */
    @RequestMapping(params = "spaceOwner", produces = "text/html")
    public String showMore(@RequestParam(value = "tagId", required = false) Long tagId, @RequestParam(value = "spaceOwner", required = false) String spaceOwner,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, String sortExpression) {
    	BigTag tBigTag = BigTag.findBigTag(tagId);
    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner);
    		if(tOwner == null){
    			return null;
    		}
    	}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            if("admin".equals(spaceOwner) || "".equals(spaceOwner)){
                uiModel.addAttribute("spaceOwner", "admin");
            	uiModel.addAttribute("contents", Content.findContentsByTag(tBigTag, firstResult, sizeNo, sortExpression));
            	float nrOfPages = (float) Content.countContentsByTag(tBigTag) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }else{
            	String tCurName = userContextService.getCurrentUserName();
            	UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
            	Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
                uiModel.addAttribute("spaceOwner", spaceOwner);
                uiModel.addAttribute("spaceOwnerId", tOwner.getId());
            	uiModel.addAttribute("contents", Content.findContentsByTagAndSpaceOwner(tBigTag, tOwner, tAuthSet, firstResult, sizeNo, sortExpression));
            	float nrOfPages = (float) Content.countContentsByTagAndSpaceOwner(tBigTag, tOwner, tAuthSet) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }
        }

        uiModel.addAttribute("tag", tBigTag.getTagName());
        uiModel.addAttribute("tagId", tagId);
        uiModel.addAttribute("description", tOwner.getDescription());
        return "public/list_more";
    }
    
    @RequestMapping(params = "twitterid", produces = "text/html")
    public String showDetailTwitters(@RequestParam(value = "twitterid", required = false) Long twitterid,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, HttpServletRequest request) {
    	RemarkController tController = SpringApplicationContext.getApplicationContext().getBean("remarkController", RemarkController.class);
		return(tController.showDetailTwitters(twitterid, 0, page, size, uiModel, request));
    }
    
    @RequestMapping(params = "refreshTime", produces = "text/html")
    public String setRefreshTime(RefreshBean refreshBean, @RequestParam(value = "refreshTwitterid", required = true) Long refreshTwitterid, Model uiModel, HttpServletRequest httpServletRequest) {
    	int tTime = 120;
    	String tTimeStr = refreshBean.getRefreshTime();
    	if (tTimeStr != null && tTimeStr.startsWith(","))
    		tTimeStr = tTimeStr.substring(1);
    	
    	try{
    		tTime = Integer.valueOf(tTimeStr);
    	}catch(Exception e){
    		tTime = 0;
    	}
    	
    	tTime = (tTime == 0) ? 0 : (tTime > 15 ? tTime : 15);
    	RemarkController tController = SpringApplicationContext.getApplicationContext().getBean("remarkController", RemarkController.class);
    	return tController.showDetailTwitters(refreshTwitterid, tTime, null, null, uiModel, httpServletRequest);
    }

    
    @RequestMapping(params = "publisher", produces = "text/html")
    public String listContentByPublisher(@RequestParam(value = "publisher", required = false) String pPublisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, String sortExpression) {
		UserAccount tPublisher = UserAccount.findUserAccountByName(pPublisher);
		if(tPublisher == null){
			pPublisher = BigUtil.getUTFString(pPublisher);
			tPublisher = UserAccount.findUserAccountByName(pPublisher);
			if(tPublisher == null)
				return "";
		}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 20 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;

            String tCurName = userContextService.getCurrentUserName();
        	UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
            Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tPublisher);
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, tAuthSet, firstResult, sizeNo, sortExpression));
            uiModel.addAttribute("publisher", pPublisher);
            uiModel.addAttribute("balance",tPublisher.getBalance());
            if(tCurName != null){
            	tCurName = tCurUser.getName();
            	if(pPublisher.equals(tCurName)){
        			uiModel.addAttribute("nothireable", "true");
        			uiModel.addAttribute("notfireable", "true");
            	}else if(tCurUser.getListento().contains(tPublisher)){
        			uiModel.addAttribute("nothireable", "true");
        		}else{
        			uiModel.addAttribute("notfireable", "true");
        		}
            }
            float nrOfPages = (float) Content.countContentsByPublisher(tPublisher, tAuthSet) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        }
        return "public/list_publisher";
    }
    
    @RequestMapping(params = "listmoreblog", produces = "text/html")
    public String listMoreBlogs(@RequestParam(value = "listmoreblog", required = false) String pPublisher, @RequestParam(value = "twittertype", required = false) String twittertype,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, String sortExpression) {
		UserAccount tPublisher = UserAccount.findUserAccountByName(pPublisher);
		if(tPublisher == null){
			pPublisher = BigUtil.getUTFString(pPublisher);
			tPublisher = UserAccount.findUserAccountByName(pPublisher);
			if(tPublisher == null)
				return "";
		}

        float nrOfPages;
        
        int sizeNo = size == null ? 20 : size.intValue();
        int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;

        String tCurName = userContextService.getCurrentUserName();
    	UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
        if("friend".equals(twittertype)){
            Set<UserAccount> tTeamSet = tPublisher.getListento();  	//get all the users that the owner cares.
            List<UserAccount> tFansList = new ArrayList<UserAccount>();
            List<UserAccount> tNonfansList = new ArrayList<UserAccount>();

            Set<Integer> tAuthSetFans = BigAuthority.getAuthSetForFans();		//the authset for fans.
            Set<Integer> tAuthSetNonFans = BigAuthority.getAuthSetForNonFans();	//the autosent for non fans.
            
            long twittersFromFans = 0;
            long twittersFromNonFans = 0;
            
            if(tCurUser != null){
	            Object[] friendsAry = tTeamSet.toArray();
	            for(int i = 0; i < friendsAry.length; i++){
	            	Set<UserAccount> tWhoHeCares = ((UserAccount)friendsAry[i]).getListento();
	            	if(tWhoHeCares.contains(tCurUser))					//check if this guy cares currently logged in user (not space owner).
	            		tFansList.add((UserAccount)friendsAry[i]);							//fans into this list, will display hist public and visible to friends blog.
	            	else
	            		tNonfansList.add((UserAccount)friendsAry[i]);						//non-fans into this list, will display his only public blog.
	            }
	            
	            twittersFromFans = Twitter.countTwittersByOwner(new HashSet<UserAccount>(tFansList), tAuthSetFans);				//twitter amount (fans)
	            twittersFromNonFans = Twitter.countTwittersByOwner(new HashSet<UserAccount>(tNonfansList), tAuthSetNonFans);	//twitter amount (non-fans)
	            
	            //got the list from fans first. 
	        	List<Twitter> tListTwitter = Twitter.findTwitterByOwner(new HashSet<UserAccount>(tFansList), tAuthSetFans, firstResult, sizeNo, sortExpression);
	        	//if not find, or not enough, then find from non-fans. be careful that the parameters are different.
        		if(tListTwitter == null){		//out of range  
	        		//Let's say the second page should start from 20(suppose page size is 20), but because the first page has 5 blogs from fans, So, first result is 20 - 5.
	        		int fanFullPageQt = (int)twittersFromFans/sizeNo;
	        		int fanItemQtLeft = (int)twittersFromFans - (int)twittersFromFans/sizeNo * sizeNo;
	        		firstResult = ((page.intValue() - 1) - fanFullPageQt) * sizeNo - fanItemQtLeft;
	        		tListTwitter = Twitter.findTwitterByOwner(new HashSet<UserAccount>(tNonfansList), tAuthSetNonFans, firstResult, sizeNo, sortExpression);
		        	uiModel.addAttribute("blogs", tListTwitter);
	        	}else if(tListTwitter.size() < sizeNo){ //or on the last page of fans. so must be start from 0. size must be (sizeNo - tListTwitter.size())
		        	List<Twitter> tListForReturn = new ArrayList<Twitter>();//serch result can't be changed, so create a new one.
		        	tListForReturn.addAll(tListTwitter);
		        	
		        	List<Twitter> tList = Twitter.findTwitterByOwner(new HashSet<UserAccount>(tNonfansList), tAuthSetNonFans, 0, sizeNo - tListTwitter.size(), sortExpression);
	        		if(tList != null)
	        			tListForReturn.addAll(tList);

		        	uiModel.addAttribute("blogs", tListForReturn);
	        	}else{
	        		uiModel.addAttribute("blogs", tListTwitter);
	        	}
        		
	        }else{
	        	uiModel.addAttribute("blogs", Twitter.findTwitterByOwner(tTeamSet, tAuthSetNonFans, firstResult, sizeNo, sortExpression));
	        }
        	nrOfPages = (float) (twittersFromFans + twittersFromNonFans) / sizeNo;
        	
        }else{
            Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tPublisher);
        	uiModel.addAttribute("blogs", Twitter.findTwitterByPublisher(tPublisher, tAuthSet, firstResult, sizeNo, sortExpression));
        	nrOfPages = (float) Twitter.countTwitterByPublisher(tPublisher, tAuthSet) / sizeNo;
        }
        uiModel.addAttribute("publisher", pPublisher);
        uiModel.addAttribute("balance",tPublisher.getBalance());
        if(tCurName != null){
        	tCurName = tCurUser.getName();
        	if(pPublisher.equals(tCurName)){
    			uiModel.addAttribute("nothireable", "true");
    			uiModel.addAttribute("notfireable", "true");
        	}else if(tCurUser.getListento().contains(tPublisher)){
    			uiModel.addAttribute("nothireable", "true");
    		}else{
    			uiModel.addAttribute("notfireable", "true");
    		}
        }
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        uiModel.addAttribute("type", twittertype);
        uiModel.addAttribute("twitter_twitdate_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
            
        return "public/list_more_blog";
    }
    
    /**
     * will give 1 month salary when click the hire button.
     * @param publisher
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "hire", produces = "text/html")
    public String hirePublisher(@RequestParam(value = "hire", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, HttpServletRequest request) {
    	
    	String tCurName = userContextService.getCurrentUserName();				// not logged in? to login page.
		if(tCurName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);		//make sure the publisher still exist.
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		int tSalary = tPublisher.getPrice();
		UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);			//in who's space right now?
		tCurName = tOwner.getName();
		tOwner.getListento().add(tPublisher);
		tOwner.setBalance(tOwner.getBalance() - tSalary);
		tPublisher.setBalance(tPublisher.getBalance() + tSalary);
		//TODO: better should put them in a transaction.
		tOwner.persist();
		tPublisher.persist();
		
		PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
		return(tController.index(tCurName, page, size, uiModel, request));
    }
    
    /**
     * will give the guy one month salary to fire.
     * @param publisher
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "fire", produces = "text/html")
    public String firePublisher(@RequestParam(value = "fire", required = false) String publisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, HttpServletRequest request) {
    	
    	String tCurName = userContextService.getCurrentUserName();
		if(tCurName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		int tSalary = tPublisher.getPrice();
		UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);			//in who's space right now?
		tCurName = tOwner.getName();
		tOwner.getListento().remove(tPublisher);
		tOwner.setBalance(tOwner.getBalance() - tSalary);
		tPublisher.setBalance(tPublisher.getBalance() + tSalary);
		//TODO: better should put them in a transaction.
		tOwner.persist();
		tPublisher.persist();

		return(SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tCurName, page, size, uiModel, request));
    }
    
   /**
     * adjust the layout
     * @param publisher
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "relayouttype", produces = "text/html")
    public String relayout(@RequestParam(value = "relayouttype", required = true) String relayouttype, 
    		@RequestParam(value = "tagId", required = true) Long tagId, HttpServletRequest request, Model uiModel) {
    	String tCurName = userContextService.getCurrentUserName();
    	//@Note:this method can be called by logoutfilter when click the logout link, don't know the reason yet. so for now, just add a check of the curname.
    	if(tCurName ==  null){
    		return(index(uiModel));
    	}
    	
		PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
		UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);
		tCurName = tOwner.getName();

		//this command is not used for now, because we are using the icon for another function which display an interface for add/remve tags on screen.//@?while I just saw there's a set to default link in that page.
		if("reset".equals(relayouttype)){
			tOwner.setLayout(null);
			tOwner.persist();
			return tController.index(tCurName, 0, 8, uiModel, request);
		}
		
		BigTag tBigTag = BigTag.findBigTag(tagId);

	   	String[] tAryTagStrsLeft = null;								//for generating the new layout string.
	   	String[] tAryTagStrsRight = null;
	   	String[] tAryNumStrsLeft = null;
	   	String[] tAryNumStrsRight = null;
	   	
	   	String tLayout = tOwner.getLayout();							//get the layout info from DB.and separate it into the array
   		int p = tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
		String tTagStr = tLayout.substring(0, p);
		String tSizeStr = tLayout.substring(p + BigUtil.MARK_SEP_LENGTH);
		p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
		if(p >= 0){
    		tAryTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
    		tAryTagStrsRight = tTagStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
		}
		p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
		if(p >= 0){
    		tAryNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
    		tAryNumStrsRight = tSizeStr.substring(p + BigUtil.MARK_SEP_LENGTH).split(BigUtil.SEP_ITEM);
		}
		//for the case that when empty string split, it return a string[] which one element. which will the treated as has meaningful element later.
		if(tAryTagStrsLeft.length == 1 && tAryTagStrsLeft[0].length() == 0)
			tAryTagStrsLeft = new String[0];
		if(tAryTagStrsRight.length == 1 && tAryTagStrsRight[0].length() == 0)
			tAryTagStrsRight = new String[0];
		if(tAryNumStrsLeft.length == 1 && tAryNumStrsLeft[0].length() == 0)
			tAryNumStrsLeft = new String[0];
		if(tAryNumStrsRight.length == 1 && tAryNumStrsRight[0].length() == 0)
			tAryNumStrsRight = new String[0];

		
		//---------adjusting the Sting Arys-------------
		//to find out the column and position
		tTagStr = BigUtil.getTagInLayoutString(tBigTag);
		
		boolean tIsInLeftColumn = false;
		int tPos;
		for(tPos = 0; tPos < tAryTagStrsLeft.length; tPos++){
			if(tAryTagStrsLeft[tPos].equals(tTagStr)){
				tIsInLeftColumn = true;
				break;
			}
		}
		if(!tIsInLeftColumn){
			for(tPos = 0; tPos < tAryTagStrsRight.length; tPos++){
				if(tAryTagStrsRight[tPos].equals(tTagStr)){
					break;
				}
			}
		}	//now know the column and position.
		
		if("close".equals(relayouttype)){
			if(tIsInLeftColumn){
				String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length - 1];
				String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length - 1];
				for(int j = 0; j < tAryTagStrsLeft.length; j++){
					if(j < tPos){
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
					}else if(j == tPos){
						continue;
					}else{
						tAryTagStrsLeft2[j - 1] = tAryTagStrsLeft[j];
						tAryNumStrsLeft2[j - 1] = tAryNumStrsLeft[j];
					}
				}
				tAryTagStrsLeft = tAryTagStrsLeft2;
				tAryNumStrsLeft = tAryNumStrsLeft2;				
			}else{
				String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length - 1];
				String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length - 1];
				for(int j = 0; j < tAryTagStrsRight.length; j++){
					if(j < tPos){
						tAryTagStrsRight2[j] = tAryTagStrsRight[j];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j];
					}else if(j == tPos){
						continue;
					}else{
						tAryTagStrsRight2[j - 1] = tAryTagStrsRight[j];
						tAryNumStrsRight2[j - 1] = tAryNumStrsRight[j];
					}
				}
				tAryTagStrsRight = tAryTagStrsRight2;
				tAryNumStrsRight = tAryNumStrsRight2;
			}
		}else if("left".equals(relayouttype) && !tIsInLeftColumn){
			String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length + 1];
			String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length + 1];
			for(int j = 0; j < tAryTagStrsLeft2.length; j++){
				if(j < tPos){
					if(j < tAryNumStrsLeft.length){
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
					}else{
						tAryTagStrsLeft2[j] = tAryTagStrsRight[tPos];
						tAryNumStrsLeft2[j] = tAryNumStrsRight[tPos];
					}
				}else if(j == tPos){
					tAryTagStrsLeft2[j] = tAryTagStrsRight[j];
					tAryNumStrsLeft2[j] = tAryNumStrsRight[j];
				}else{
					tAryTagStrsLeft2[j] = tAryTagStrsLeft[j - 1];
					tAryNumStrsLeft2[j] = tAryNumStrsLeft[j - 1];
				}
			}
			
			String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length - 1];
			String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length - 1];
			for(int j = 0; j < tAryTagStrsRight.length; j++){
				if(j < tPos){
					tAryTagStrsRight2[j] = tAryTagStrsRight[j];
					tAryNumStrsRight2[j] = tAryNumStrsRight[j];
				}else if(j == tPos){
					continue;
				}else{
					tAryTagStrsRight2[j - 1] = tAryTagStrsRight[j];
					tAryNumStrsRight2[j - 1] = tAryNumStrsRight[j];
				}
			}
			tAryTagStrsLeft = tAryTagStrsLeft2;
			tAryNumStrsLeft = tAryNumStrsLeft2;
			tAryTagStrsRight = tAryTagStrsRight2;
			tAryNumStrsRight = tAryNumStrsRight2;
		}else if("up".equals(relayouttype) && tPos > 0){
			if(tIsInLeftColumn){
				String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length];
				String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length];
				for(int j = 0; j < tAryTagStrsLeft.length; j++){
					if(j == tPos - 1){
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j + 1];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j + 1];
					}else if(j == tPos){
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j - 1];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j - 1];
					}else{
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
					}
				}
				tAryTagStrsLeft = tAryTagStrsLeft2;
				tAryNumStrsLeft = tAryNumStrsLeft2;
			}else{
				String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length];
				String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length];
				for(int j = 0; j < tAryTagStrsRight.length; j++){
					if(j == tPos - 1){
						tAryTagStrsRight2[j] = tAryTagStrsRight[j + 1];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j + 1];
					}else if(j == tPos){
						tAryTagStrsRight2[j] = tAryTagStrsRight[j - 1];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j - 1];
					}else{
						tAryTagStrsRight2[j] = tAryTagStrsRight[j];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j];
					}
				}
				tAryTagStrsRight = tAryTagStrsRight2;
				tAryNumStrsRight = tAryNumStrsRight2;
			}
		}else if("down".equals(relayouttype) && 
				((tIsInLeftColumn && tPos < tAryTagStrsLeft.length - 1) || (!tIsInLeftColumn && tPos < tAryTagStrsRight.length - 1))){

			if(tIsInLeftColumn){
				String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length];
				String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length];
				for(int j = 0; j < tAryTagStrsLeft.length; j++){
					if(j == tPos){
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j + 1];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j + 1];
					}else if(j == tPos + 1){
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j - 1];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j - 1];
					}else{
						tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
					}
				}
				tAryTagStrsLeft = tAryTagStrsLeft2;
				tAryNumStrsLeft = tAryNumStrsLeft2;
			}else{
				String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length];
				String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length];
				for(int j = 0; j < tAryTagStrsRight.length; j++){
					if(j == tPos){
						tAryTagStrsRight2[j] = tAryTagStrsRight[j + 1];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j + 1];
					}else if(j == tPos + 1){
						tAryTagStrsRight2[j] = tAryTagStrsRight[j - 1];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j - 1];
					}else{
						tAryTagStrsRight2[j] = tAryTagStrsRight[j];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j];
					}
				}
				tAryTagStrsRight = tAryTagStrsRight2;
				tAryNumStrsRight = tAryNumStrsRight2;
			}
		}else if("right".equals(relayouttype) && tIsInLeftColumn){
			String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length - 1];
			String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length - 1];
			for(int j = 0; j < tAryTagStrsLeft.length; j++){
				if(j < tPos){
					tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
					tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
				}else if(j == tPos){
					continue;
				}else{
					tAryTagStrsLeft2[j - 1] = tAryTagStrsLeft[j];
					tAryNumStrsLeft2[j - 1] = tAryNumStrsLeft[j];
				}
			}

			String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length + 1];
			String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length + 1];
			for(int j = 0; j < tAryTagStrsRight2.length; j++){
				if(j < tPos){
					if(j < tAryTagStrsRight.length){
						tAryTagStrsRight2[j] = tAryTagStrsRight[j];
						tAryNumStrsRight2[j] = tAryNumStrsRight[j];
					}else{
						tAryTagStrsRight2[j] = tAryTagStrsLeft[tPos];
						tAryNumStrsRight2[j] = tAryNumStrsLeft[tPos];
					}
				}else if(j == tPos){
					tAryTagStrsRight2[j] = tAryTagStrsLeft[j];
					tAryNumStrsRight2[j] = tAryNumStrsLeft[j];
				}else{
					tAryTagStrsRight2[j] = tAryTagStrsRight[j-1];
					tAryNumStrsRight2[j] = tAryNumStrsRight[j-1];
				}
			}
			tAryTagStrsLeft = tAryTagStrsLeft2;
			tAryNumStrsLeft = tAryNumStrsLeft2;
			tAryTagStrsRight = tAryTagStrsRight2;
			tAryNumStrsRight = tAryNumStrsRight2;
		}else if("list_size".equals(relayouttype)){
			String[] tAry = request.getParameterValues("list_size");
			if(tAry == null || tAry.length ==0)
				tAry = new String[]{"8"};			
			String tNewSize = tAry[0];
			int tList_size = Integer.parseInt(tNewSize);
			if(tList_size < 0)
				tNewSize = "8";
			if(tList_size > 200)
				tNewSize = "200";						//validate the parameters.
			
			if(tIsInLeftColumn){
				String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length];
				for(int j = 0; j < tAryTagStrsLeft.length; j++){
					if(j == tPos){
						tAryNumStrsLeft2[j] = tNewSize;
					}else{
						tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
					}
				}
				tAryNumStrsLeft = tAryNumStrsLeft2;
			}else{
				String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length];
				for(int j = 0; j < tAryTagStrsRight.length; j++){
					if(j == tPos){
						tAryNumStrsRight2[j] = tNewSize;
					}else{
						tAryNumStrsRight2[j] = tAryNumStrsRight[j];
					}
				}
				tAryNumStrsRight = tAryNumStrsRight2;
			}
		}
		
		//----------------------
	    StringBuilder tStrB = new StringBuilder();						//construct the new String of layout
	    StringBuilder tStrB_Num = new StringBuilder();
   		for(int j = 0; j < tAryTagStrsLeft.length; j++){			
   	    	tStrB.append(tAryTagStrsLeft[j]);
   	    	tStrB_Num.append(tAryNumStrsLeft[j]);
   	    	if(j + 1 < tAryTagStrsLeft.length){
   	    		tStrB.append(BigUtil.SEP_ITEM);
       	    	tStrB_Num.append(BigUtil.SEP_ITEM);
   	    	}
	    }

   		tStrB.append(BigUtil.SEP_LEFT_RIGHT);
   		tStrB_Num.append(BigUtil.SEP_LEFT_RIGHT);
   		
   		for(int j = 0; j < tAryTagStrsRight.length; j++){
   	    	tStrB.append(tAryTagStrsRight[j]);
   	    	tStrB_Num.append(tAryNumStrsRight[j]);
   	    	if(j + 1 < tAryTagStrsRight.length){
   	    		tStrB.append(BigUtil.SEP_ITEM);
       	    	tStrB_Num.append(BigUtil.SEP_ITEM);
   	    	}
	    }
   		tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tStrB_Num);

   		tOwner.setLayout(tStrB.toString());	    						//save the new layout string to DB
   		tOwner.persist();
   		
   		//----------------prepare for show-------------------
		return(tController.index(tCurName, 0, 8, uiModel, request));
    }
}
