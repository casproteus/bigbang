package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

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
    	int p = tLayout == null ? -1 : tLayout.indexOf('�');
		if(p > -1){
			String tTagStr = tLayout.substring(0, p);
			String tSizeStr = tLayout.substring(p+1);
			
    		p = tTagStr.indexOf('�');
    		if(p >=0 ){
    			tAryTagStrsLeft = tTagStr.substring(0, p).split("�");
    			tAryTagStrsRight = tTagStr.substring(p+1).split("�");
    		}
    		p = tSizeStr.indexOf('�');
    		if(p >=0 ){
    			tAryNumStrsLeft = tSizeStr.substring(0, p).split("�");
    			tAryNumStrsRight = tSizeStr.substring(p+1).split("�");
    		}
		}
    	
		//if the layout info in DB is not good, create it from beginning.
		if(BigUtil.notCorrect(tAryTagStrsLeft, tAryTagStrsRight, tAryNumStrsLeft, tAryNumStrsRight)){
			
	    	List<BigTag> tBigTags = BigTag.findBMTagsByOwner("admin"); 	//fetch out all tags of admin's, owner's and his team's, 
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
    	    		tStrB.append('�');
        	    	tStrB_Num.append('�');
    	    	}
	    	}

    		tStrB.append('�');
    		tStrB_Num.append('�');
    		
    		for(int j = tSize/2; j < tSize; j++){
    			BigTag tTag = tBigTags.get(j);
    			tBigTagsRight.add(tBigTags.get(j));
    	    	tTagIdsRight.add(tTagIds.get(j));
    	    	
    	    	tStrB.append(BigUtil.getTagInLayoutString(tTag));
    	    	
    	    	tAryNumStrsRight[j - tSize/2] = "8";
    	    	tStrB_Num.append(tAryNumStrsRight[j - tSize/2]);
    	    	
    	    	if(j + 1 < tSize){
    	    		tStrB.append('�');
        	    	tStrB_Num.append('�');
    	    	}
	    	}
    		tStrB.append('�').append(tStrB_Num);

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
            int sizeNo = size == null ? 25 : size.intValue();
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
    
    /**
     * We have to use both tag's tagname and type to match out a single tag, because different user can create tags with same name. 
     * if we match content with only tag name, will cause mistake when clicking the "more" button from personal space. 
     * so we have to use tag's ID to match content.
     * @note we have to use "owner" instead of "spaceowner", to avoid spring can not matching the request to the other method.
     * @param tag
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "twittertype", produces = "text/html")
    public String showMoreTwitters(@RequestParam(value = "twittertype", required = false) String twittertype, @RequestParam(value = "owner", required = false) String spaceOwner,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner);
    		if(tOwner == null){
    			return null;
    		}
    	}
        int sizeNo = size == null ? 25 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
       
    	String tCurName = userContextService.getCurrentUserName();
    	UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
    	Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        uiModel.addAttribute("spaceOwner", spaceOwner);
        uiModel.addAttribute("spaceOwnerId", tOwner.getId());
        float nrOfPages;
        if("friend".equals(twittertype)){
        	uiModel.addAttribute("twittertype", "friend");
        	uiModel.addAttribute("contents", Twitter.findTwitterByOwner(tOwner, tAuthSet, firstResult, sizeNo));
        	nrOfPages = (float) Twitter.countTwittersByOwner(tOwner, tAuthSet) / sizeNo;
        }else{
        	uiModel.addAttribute("twittertype", "self");
        	uiModel.addAttribute("contents", Twitter.findTwitterByPublisher(tOwner, tAuthSet, firstResult, sizeNo));
        	nrOfPages = (float) Twitter.countTwitterByPublisher(tOwner, tAuthSet) / sizeNo;
        }
    	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));

        return "public/list_more_twitter";
    }

    /**
     * We have to use both tag's tagname and type to match out a single tag, because different user can create tags with same name. 
     * if we match content with only tag name, will cause mistake when clicking the "more" button from personal space. 
     * so we have to use tag's ID to match content.
     * @param tag
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "twitterid", produces = "text/html")
    public String showDetailTwitters(@RequestParam(value = "twitterid", required = false) Long twitterid,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel, HttpServletRequest request) {
        Twitter tTwitter = Twitter.findTwitter(twitterid);
    	UserAccount tOwner = tTwitter.getPublisher();
        int sizeNo = size == null ? 25 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
       
    	String tCurName = userContextService.getCurrentUserName();
    	UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
    	Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        uiModel.addAttribute("spaceOwner", tOwner);
        float nrOfPages;
        uiModel.addAttribute("twitter", tTwitter);
        uiModel.addAttribute("remarks", Remark.findRemarkByTwitter(tTwitter, tAuthSet, firstResult, sizeNo));
        nrOfPages = (float) Remark.countRemarksByTwitter(tTwitter, tAuthSet) / sizeNo;
    	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	Remark tRemark = new Remark();
        uiModel.addAttribute("newremark", tRemark);
        uiModel.addAttribute("authorities",BigAuthority.getRemarkOptions(messageSource, request.getLocale()));
        List<Twitter> remarktos = new ArrayList<Twitter>();
        remarktos.add(tTwitter);
        uiModel.addAttribute("remarktos", remarktos);
        
        return "public/list_detail_twitter";
    }
    
	@RequestMapping(params = "pTwitterId", method = RequestMethod.POST, produces = "text/html")
    public String createRemark(@Valid Remark remark, BindingResult bindingResult,
    		@RequestParam(value = "pTwitterId", required = false)Long pTwitterId,
    		Model uiModel, HttpServletRequest httpServletRequest) {
		//TODO: Should make the check before submit.
		if(remark.getContent() == null || remark.getContent().length() < 1)
            return "remarks/create";
		
		//get his last twitter in db compare with it.
		String tCurName = userContextService.getCurrentUserName();
	    UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
		List<Remark> tList = Remark.findRemarkByPublisher(tUserAccount, 0, 1);
		//This is good, but not good enough, because when user press F5 after modifying a remark, and press back->back
		//will trick out the form to submit again, and then in this method, the content are different....
		//TODO: add a hidden field in From and save a token in it.then verify, if the token not there, then stop saving
		//http://stackoverflow.com/questions/2324931/duplicate-form-submission-in-spring
		if(tList != null && tList.size() > 0){
			Remark tTwitter = tList.get(0);
			if(remark.getContent().equals(tTwitter.getContent()) && remark.getRemarkto().equals(tTwitter.getRemarkto()))
				return "remarks/create";
		}
	
		if (bindingResult.hasErrors()) {
			if (bindingResult.getAllErrors().size() == 1 && remark.getPublisher() == null) {
				remark.setPublisher(tUserAccount);
				remark.setRemarkto(Twitter.findTwitter(pTwitterId));
				remark.setRemarkTime(new Date());//add remark time when it's submitted.
			} else {
		        return "public/list_detail_twitter";
			}
        }
        uiModel.asMap().clear();
        remark.persist();
        
        BigUtil.refreshULastUpdateTimeOfTwitter(remark);
        return showDetailTwitters(remark.getRemarkto().getId(), null, null, uiModel, httpServletRequest);
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
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	
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
		return(tController.index(tCurName, page, size, uiModel));
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
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	
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

		return(SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tCurName, page, size, uiModel));
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
		UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);
		tCurName = tOwner.getName();
		if("reset".equals(relayouttype)){
			tOwner.setLayout(null);
			tOwner.persist();
			return (SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tCurName, 0, 8, uiModel));
		}
		
		BigTag tBigTag = BigTag.findBigTag(tagId);

	   	String[] tAryTagStrsLeft = null;								//for generating the new layout string.
	   	String[] tAryTagStrsRight = null;
	   	String[] tAryNumStrsLeft = null;
	   	String[] tAryNumStrsRight = null;
	   	
	   	String tLayout = tOwner.getLayout();							//get the layout info from DB.and separate it into the array
   		int p = tLayout.indexOf('�');
		String tTagStr = tLayout.substring(0, p);
		String tSizeStr = tLayout.substring(p+1);
		p = tTagStr.indexOf('�');
		if(p >= 0){
    		tAryTagStrsLeft = tTagStr.substring(0, p).split("�");
    		tAryTagStrsRight = tTagStr.substring(p+1).split("�");
		}
		p = tSizeStr.indexOf('�');
		if(p >= 0){
    		tAryNumStrsLeft = tSizeStr.substring(0, p).split("�");
    		tAryNumStrsRight = tSizeStr.substring(p+1).split("�");
		}
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
   	    		tStrB.append('�');
       	    	tStrB_Num.append('�');
   	    	}
	    }

   		tStrB.append('�');
   		tStrB_Num.append('�');
   		
   		for(int j = 0; j < tAryTagStrsRight.length; j++){
   	    	tStrB.append(tAryTagStrsRight[j]);
   	    	tStrB_Num.append(tAryNumStrsRight[j]);
   	    	if(j + 1 < tAryTagStrsRight.length){
   	    		tStrB.append('�');
       	    	tStrB_Num.append('�');
   	    	}
	    }
   		tStrB.append('�').append(tStrB_Num);

   		tOwner.setLayout(tStrB.toString());	    						//save the new layout string to DB
   		tOwner.persist();
   		
   		//----------------prepare for show-------------------
	   	List<BigTag> tBigTagsLeft = BigUtil.transferToTags(tAryTagStrsLeft, tCurName);
	   	List<Long> tTagIdsLeft = new ArrayList<Long>();					//prepare the info for view base on the string in db:
   		for(int i = 0; i < tBigTagsLeft.size(); i++){
   			tTagIdsLeft.add(tBigTagsLeft.get(i).getId());
   		}
   		
   		List<BigTag> tBigTagsRight = BigUtil.transferToTags(tAryTagStrsRight, tCurName);
	   	List<Long> tTagIdsRight = new ArrayList<Long>();
   		for(int i = 0; i < tBigTagsRight.size(); i++){
   			tTagIdsRight.add(tBigTagsRight.get(i).getId());
   		}
   		
        List<List> tContentListsLeft = new ArrayList<List>();								//prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>();								//prepare the contentList for each tag.
    	for(int i = 0; i < tBigTagsLeft.size(); i++){
    		tContentListsLeft.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsLeft.get(i), tOwner, BigAuthority.getAuthSet(tOwner, tOwner),
    				0, Integer.valueOf(tAryNumStrsLeft[i]).intValue(), null));
    	}
    	for(int i = 0; i < tBigTagsRight.size(); i++){
    		tContentListsRight.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsRight.get(i), tOwner, BigAuthority.getAuthSet(tOwner, tOwner),
    				0, Integer.valueOf(tAryNumStrsRight[i]).intValue(), null));
    	}
    	
        uiModel.addAttribute("spaceOwner", tCurName);
        uiModel.addAttribute("description", tOwner.getDescription());
        uiModel.addAttribute("bigTagsLeft", tBigTagsLeft);
        uiModel.addAttribute("bigTagsRight", tBigTagsRight);
        uiModel.addAttribute("tagIdsLeft", tTagIdsLeft);
        uiModel.addAttribute("tagIdsRight", tTagIdsRight);
        uiModel.addAttribute("contentsLeft", tContentListsLeft);
        uiModel.addAttribute("contentsRight", tContentListsRight);
       
        return "public/index";
    }

}
