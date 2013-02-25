package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
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
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("go to his big uncle's!");
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
    	if(tLayout != null && tLayout.length() > 2){
    		int p = tLayout.indexOf('™');
    		if(p >= 0){
    			String tTagStr = tLayout.substring(0, p);
    			String tSizeStr = tLayout.substring(p+1);
    			
        		p = tTagStr.indexOf('¬');
        		if(p >=0 ){
        			tAryTagStrsLeft = tTagStr.substring(0, p).split("¯");
        			tAryTagStrsRight = tTagStr.substring(p+1).split("¯");
        		}
        		p = tSizeStr.indexOf('¬');
        		if(p >=0 ){
        			tAryNumStrsLeft = tSizeStr.substring(0, p).split("¯");
        			tAryNumStrsRight = tSizeStr.substring(p+1).split("¯");
        		}
    		}
    	}
    	
		//if the layout info in DB is not good, create it from beginning.
		if(((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0) && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
				|| ((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0) && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
				|| (tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)){
			
	    	List<BigTag> tBigTags = BigTag.findTagsByOwner("admin"); 	//fetch out all tags of admin's, owner's and his team's, 
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
    	    	tAryNumStrsLeft[j] = "8";
    	    	if("admin".equals(tTag.getType()) || "administrator".equals(tTag.getType())){
    	    		tStrB.append('¶');
    	    	}
    	    	tStrB.append(tTag.getTagName());
    	    	tStrB_Num.append(tAryNumStrsLeft[j]);
    	    	if(j + 1 < tSize/2){
    	    		tStrB.append('¯');
        	    	tStrB_Num.append('¯');
    	    	}
	    	}

    		tStrB.append('¬');
    		tStrB_Num.append('¬');
    		
    		for(int j = tSize/2; j < tSize; j++){
    			BigTag tTag = tBigTags.get(j);
    			tBigTagsRight.add(tBigTags.get(j));
    	    	tTagIdsRight.add(tTagIds.get(j));
    	    	tAryNumStrsRight[j - tSize/2] = "8";
    	    	if("admin".equals(tTag.getType()) || "administrator".equals(tTag.getType())){
    	    		tStrB.append('¶');
    	    	}
    	    	tStrB.append(tTag.getTagName());
    	    	tStrB_Num.append(tAryNumStrsRight[j - tSize/2]);
    	    	if(j + 1 < tSize){
    	    		tStrB.append('¯');
        	    	tStrB_Num.append('¯');
    	    	}
	    	}
    		tStrB.append('™').append(tStrB_Num);

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
    		tContentListsLeft.add(Content.findContentsByTag(tBigTagsLeft.get(i), 0, Integer.valueOf(tAryNumStrsLeft[i]).intValue()));
    	}
    	for(int i = 0; i < tBigTagsRight.size(); i++){
    		tContentListsRight.add(Content.findContentsByTag(tBigTagsRight.get(i), 0, Integer.valueOf(tAryNumStrsRight[i]).intValue()));
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
     * @param tag
     * @param page
     * @param size
     * @param uiModel
     * @return
     */
    @RequestMapping(params = "spaceOwner", produces = "text/html")
    public String showMore(@RequestParam(value = "tagId", required = false) Long tagId, @RequestParam(value = "spaceOwner", required = false) String spaceOwner,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	BigTag tBigTag = BigTag.findBigTag(tagId);
    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner);
    		if(tOwner == null){
    			return "";
    		}
    	}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 25 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            if("admin".equals(spaceOwner) || "".equals(spaceOwner)){
                uiModel.addAttribute("spaceOwner", "admin");
            	uiModel.addAttribute("contents", Content.findContentsByTag(tBigTag, firstResult, sizeNo));
            	float nrOfPages = (float) Content.countContentsByTag(tBigTag) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }else{
            	Set<Integer> tAuthSet = BigAuthority.getAuthSet(userContextService.getCurrentUserName(), tOwner);
                uiModel.addAttribute("spaceOwner", spaceOwner);
                uiModel.addAttribute("spaceOwnerId", tOwner.getId());
            	uiModel.addAttribute("contents", Content.findContentsByTagAndSpaceOwner(tBigTag, tOwner, tAuthSet, firstResult, sizeNo));
            	float nrOfPages = (float) Content.countContentsByTagAndSpaceOwner(tBigTag, tOwner, tAuthSet) / sizeNo;
            	uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            }
        }

        uiModel.addAttribute("tag", tBigTag.getTagName());
        uiModel.addAttribute("tagId", tagId);
        return "public/list_more";
    }

    @RequestMapping(params = "publisher", produces = "text/html")
    public String listContentByPublisher(@RequestParam(value = "publisher", required = false) String pPublisher,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
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
            Set<Integer> tAuthSet = BigAuthority.getAuthSet(userContextService.getCurrentUserName(), tPublisher);
            uiModel.addAttribute("contents", Content.findContentsByPublisher(tPublisher, tAuthSet, firstResult, sizeNo));
            uiModel.addAttribute("publisher", pPublisher);
            uiModel.addAttribute("price",tPublisher.getPrice());
            String tCurUserName = userContextService.getCurrentUserName();
            if(tCurUserName != null){
            	UserAccount tCurUser = UserAccount.findUserAccountByName(tCurUserName);
                uiModel.addAttribute("balance", tCurUser.getBalance());
            	if(pPublisher.equals(tCurUserName)){
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
    	
    	String tOwnerName = userContextService.getCurrentUserName();				// not logged in? to login page.
		if(tOwnerName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);		//make sure the publisher still exist.
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		int tSalary = tPublisher.getPrice();
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);			//in who's space right now?
		tOwner.getListento().add(tPublisher);
		tOwner.setBalance(tOwner.getBalance() - tSalary);
		tPublisher.setBalance(tPublisher.getBalance() + tSalary);
		//TODO: better should put them in a transaction.
		tOwner.persist();
		tPublisher.persist();
		
		PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
		return(tController.index(tOwnerName, page, size, uiModel));
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
    	
    	String tOwnerName = userContextService.getCurrentUserName();
		if(tOwnerName == null)
	        return "login";
			
		UserAccount tPublisher = UserAccount.findUserAccountByName(publisher);
		if(tPublisher == null){
			publisher = BigUtil.getUTFString(publisher);
			tPublisher = UserAccount.findUserAccountByName(publisher);
			if(tPublisher == null)
				return "";
		}
		
		int tSalary = tPublisher.getPrice();
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);			//in who's space right now?
		tOwner.getListento().remove(tPublisher);
		tOwner.setBalance(tOwner.getBalance() - tSalary);
		tPublisher.setBalance(tPublisher.getBalance() + tSalary);
		//TODO: better should put them in a transaction.
		tOwner.persist();
		tPublisher.persist();

		return(SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tOwnerName, page, size, uiModel));
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
    	
    	String tOwnerName = userContextService.getCurrentUserName();
		UserAccount tOwner = UserAccount.findUserAccountByName(tOwnerName);
		if("reset".equals(relayouttype)){
			tOwner.setLayout(null);
			tOwner.persist();
			return (SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class).index(tOwnerName, 0, 8, uiModel));
		}
		
		BigTag tBigTag = BigTag.findBigTag(tagId);

	   	String[] tAryTagStrsLeft = null;								//for generating the new layout string.
	   	String[] tAryTagStrsRight = null;
	   	String[] tAryNumStrsLeft = null;
	   	String[] tAryNumStrsRight = null;
	   	
	   	String tLayout = tOwner.getLayout();							//get the layout info from DB.and separate it into the array
   		int p = tLayout.indexOf('™');
		String tTagStr = tLayout.substring(0, p);
		String tSizeStr = tLayout.substring(p+1);
		p = tTagStr.indexOf('¬');
		if(p >= 0){
    		tAryTagStrsLeft = tTagStr.substring(0, p).split("¯");
    		tAryTagStrsRight = tTagStr.substring(p+1).split("¯");
		}
		p = tSizeStr.indexOf('¬');
		if(p >= 0){
    		tAryNumStrsLeft = tSizeStr.substring(0, p).split("¯");
    		tAryNumStrsRight = tSizeStr.substring(p+1).split("¯");
		}
		//---------adjusting the Sting Arys-------------
		tTagStr = "admin".equals(tBigTag.getType()) || "administrator".equals(tBigTag.getType())? "¶"+ tBigTag.getTagName() : tBigTag.getTagName();
		boolean tIsInLeftColumn = false;
		int tPos;
		for(tPos = 0; tPos < tAryTagStrsLeft.length; tPos++){
			if(tTagStr.equals(tAryTagStrsLeft[tPos])){
				tIsInLeftColumn = true;
				break;
			}
		}
		if(!tIsInLeftColumn){
			for(tPos = 0; tPos < tAryTagStrsRight.length; tPos++){
				if(tTagStr.equals(tAryTagStrsRight[tPos])){
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
				if(j == tAryNumStrsLeft.length){
					tAryTagStrsLeft2[j] = tAryTagStrsRight[tPos];
					tAryNumStrsLeft2[j] = tAryNumStrsRight[tPos];
				}else if(j < tPos){
					tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
					tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
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
				if(j == tAryTagStrsRight.length){
					tAryTagStrsRight2[j] = tAryTagStrsLeft[tPos];
					tAryNumStrsRight2[j] = tAryNumStrsLeft[tPos];
				}else if(j < tPos){
					tAryTagStrsRight2[j] = tAryTagStrsRight[j];
					tAryNumStrsRight2[j] = tAryNumStrsRight[j];
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
			String[] tAry = request.getParameterValues("j_username");
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
   	    		tStrB.append('¯');
       	    	tStrB_Num.append('¯');
   	    	}
	    }

   		tStrB.append('¬');
   		tStrB_Num.append('¬');
   		
   		for(int j = 0; j < tAryTagStrsRight.length; j++){
   	    	tStrB.append(tAryTagStrsRight[j]);
   	    	tStrB_Num.append(tAryNumStrsRight[j]);
   	    	if(j + 1 < tAryTagStrsRight.length){
   	    		tStrB.append('¯');
       	    	tStrB_Num.append('¯');
   	    	}
	    }
   		tStrB.append('™').append(tStrB_Num);

   		tOwner.setLayout(tStrB.toString());	    						//save the new layout string to DB
   		tOwner.persist();
   		
   		//----------------prepare for show-------------------
	   	List<BigTag> tBigTagsLeft = BigUtil.transferToTags(tAryTagStrsLeft, tOwnerName);
	   	List<Long> tTagIdsLeft = new ArrayList<Long>();					//prepare the info for view base on the string in db:
   		for(int i = 0; i < tBigTagsLeft.size(); i++){
   			tTagIdsLeft.add(tBigTagsLeft.get(i).getId());
   		}
   		
   		List<BigTag> tBigTagsRight = BigUtil.transferToTags(tAryTagStrsRight, tOwnerName);
	   	List<Long> tTagIdsRight = new ArrayList<Long>();
   		for(int i = 0; i < tBigTagsRight.size(); i++){
   			tTagIdsRight.add(tBigTagsRight.get(i).getId());
   		}
   		
        List<List> tContentListsLeft = new ArrayList<List>();								//prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>();								//prepare the contentList for each tag.
    	for(int i = 0; i < tBigTagsLeft.size(); i++){
    		tContentListsLeft.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsLeft.get(i), tOwner, BigAuthority.getAuthSet(tOwnerName, tOwner),
    				0, Integer.valueOf(tAryNumStrsLeft[i]).intValue()));
    	}
    	for(int i = 0; i < tBigTagsRight.size(); i++){
    		tContentListsRight.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsRight.get(i), tOwner, BigAuthority.getAuthSet(tOwnerName, tOwner),
    				0, Integer.valueOf(tAryNumStrsRight[i]).intValue()));
    	}
    	
        uiModel.addAttribute("spaceOwner", tOwnerName);
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
