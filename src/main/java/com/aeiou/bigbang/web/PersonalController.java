package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.HashSet;
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

@RequestMapping("/")
@Controller
public class PersonalController{

	@Inject
	private UserContextService userContextService;
	
    @RequestMapping(value = "/{spaceOwner}", produces = "text/html")
    public String index(@PathVariable("spaceOwner") String spaceOwner, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  Model uiModel) {
    	if(BigUtil.isSystemCommand(spaceOwner))										//secrete commands goes here.
    		 return "public/index";

    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);			//make sure the owner exist, and set the name on title
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner); //bet it might still not UTF8 encoded.
    		if(tOwner == null)
    			return null;
    	}
    	spaceOwner = tOwner.getName();

		String tCurName = userContextService.getCurrentUserName();				//the current user.
		UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);
		
    	String[] tAryTagStrsLeft = null;
    	String[] tAryTagStrsRight = null;
    	String[] tAryNumStrsLeft = null;
    	String[] tAryNumStrsRight = null;
    	List<BigTag> tBigTagsLeft = new ArrayList<BigTag>();
    	List<BigTag> tBigTagsRight = new ArrayList<BigTag>();
    	List<Long> tTagIdsLeft = new ArrayList<Long>();
    	List<Long> tTagIdsRight = new ArrayList<Long>();
    	
    	String tLayout = tOwner.getLayout();										//get the layout info from DB.
    	int p = tLayout == null ? -1 : tLayout.indexOf('™');
		if(p > -1){
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
		}
    																				//if the layout info in DB is not good, create it from beginning.
    	if(((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0) && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
    			|| ((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0) && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
    			|| (tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)){
    		
    		List<BigTag> tBigTags = BigTag.findTagsByOwner(spaceOwner); 	//fetch out all tags of admin's, owner's and his team's, 
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

    	    	tStrB.append(BigUtil.getTagInLayoutString(tTag));

    	    	tAryNumStrsRight[j - tSize/2] = "8";
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
    		tBigTagsLeft = BigUtil.transferToTags(tAryTagStrsLeft, spaceOwner);
    		for(int i = 0; i < tBigTagsLeft.size(); i++){
    			tTagIdsLeft.add(tBigTagsLeft.get(i).getId());
    		}
    		
    		tBigTagsRight = BigUtil.transferToTags(tAryTagStrsRight, spaceOwner);
    		for(int i = 0; i < tBigTagsRight.size(); i++){
    			tTagIdsRight.add(tBigTagsRight.get(i).getId());
    		}
    	}
																						//final adjust---not all tags should be shown to curUser:
		if(tCurName == null){										//not logged in
			for(int i = tBigTagsLeft.size()-1; i >=0 ; i--){
				if(tBigTagsLeft.get(i).getAuthority() != 0){
					tBigTagsLeft.remove(i);
					tTagIdsLeft.remove(i);
				}
			}
			for(int i = tBigTagsRight.size()-1; i >=0 ; i--){
				if(tBigTagsRight.get(i).getAuthority() != 0){
					tBigTagsRight.remove(i);
					tTagIdsRight.remove(i);
				}
			}
		}else{
			tCurName = tCurUser.getName();
			if(!tCurName.equals(spaceOwner)){						//has logged in but not self.
				if(tOwner.getListento().contains(tCurUser)){					//it's team member
					for(int i = tBigTagsLeft.size()-1; i >=0 ; i--){
						if(tBigTagsLeft.get(i).getAuthority() != 0 && tBigTagsLeft.get(i).getAuthority() != 2){
							tBigTagsLeft.remove(i);
							tTagIdsLeft.remove(i);
						}
					}
					for(int i = tBigTagsRight.size()-1; i >=0 ; i--){
						if(tBigTagsRight.get(i).getAuthority() != 0 && tBigTagsRight.get(i).getAuthority() != 2){
							tBigTagsRight.remove(i);
							tTagIdsRight.remove(i);
						}
					}
				}else{															//it's someone else TODO: consider about case 3 in future.
					for(int i = tBigTagsLeft.size()-1; i >=0 ; i--){
						if(tBigTagsLeft.get(i).getAuthority() != 0){
							tBigTagsLeft.remove(i);
							tTagIdsLeft.remove(i);
						}
					}
					for(int i = tBigTagsRight.size()-1; i >=0 ; i--){
						if(tBigTagsRight.get(i).getAuthority() != 0){
							tBigTagsRight.remove(i);
							tTagIdsRight.remove(i);
						}
					}
				}
			}
		}
        List<List> tContentListsLeft = new ArrayList<List>();								//prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>();								//prepare the contentList for each tag.
    	for(int i = 0; i < tBigTagsLeft.size(); i++){
    		tContentListsLeft.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsLeft.get(i), tOwner, BigAuthority.getAuthSet(tCurUser, tOwner),
    				0, Integer.valueOf(tAryNumStrsLeft[i]).intValue()));
    	}
    	for(int i = 0; i < tBigTagsRight.size(); i++){
    		tContentListsRight.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsRight.get(i), tOwner, BigAuthority.getAuthSet(tCurUser, tOwner),
    				0, Integer.valueOf(tAryNumStrsRight[i]).intValue()));
    	}

        uiModel.addAttribute("spaceOwner", spaceOwner);
        uiModel.addAttribute("spaceOwnerId", tOwner.getId());
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
