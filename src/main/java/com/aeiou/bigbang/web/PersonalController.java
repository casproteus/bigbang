package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.theme.CookieThemeResolver;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Twitter;
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
    public String index(@PathVariable("spaceOwner") String spaceOwner, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel, HttpServletRequest request) {
		String tCurName = userContextService.getCurrentUserName();				//the current user.
		UserAccount tCurUser = tCurName == null ? null : UserAccount.findUserAccountByName(tCurName);

		if(BigUtil.isSystemCommand(spaceOwner, tCurUser))										//secrete commands goes here.
    		 return "public/index";

    	UserAccount tOwner = UserAccount.findUserAccountByName(spaceOwner);			//make sure the owner exist, and set the name on title
    	if(tOwner == null){
    		spaceOwner = BigUtil.getUTFString(spaceOwner);
    		tOwner = UserAccount.findUserAccountByName(spaceOwner); //bet it might still not UTF8 encoded.
    		if(tOwner == null)
    			return null;
    	}
    	
    	//if the owner has setted theme, then use the theme! (will effect only on this request)
    	int tTheme = tOwner.getTheme();
    	if(tTheme != 0)
    		request.setAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME, String.valueOf(tTheme));
    	
    	spaceOwner = tOwner.getName();

    	String[] tBigTagStrsLeft = null;
    	String[] tBigTagStrsRight = null;
    	String[] tNumStrsLeft = null;
    	String[] tNumStrsRight = null;
    	List<BigTag> tBigTagsLeft = new ArrayList<BigTag>();
    	List<BigTag> tBigTagsRight = new ArrayList<BigTag>();
    	List<Long> tTagIdsLeft = new ArrayList<Long>();
    	List<Long> tTagIdsRight = new ArrayList<Long>();
    	
    	String tLayout = tOwner.getLayout();										//get the layout info from DB.
    	int p = tLayout == null ? -1 : tLayout.indexOf(BigUtil.SEP_TAG_NUMBER);
		if(p > -1){
    		String tTagStr = tLayout.substring(0, p);
    		String tSizeStr = tLayout.substring(p+1);
    		
    		p = tTagStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
    		if(p >= 0){
	    		tBigTagStrsLeft = tTagStr.substring(0, p).split(BigUtil.SEP_ITEM);
	    		tBigTagStrsRight = tTagStr.substring(p+1).split(BigUtil.SEP_ITEM);
    		}
    		p = tSizeStr.indexOf(BigUtil.SEP_LEFT_RIGHT);
    		if(p >= 0){
	    		tNumStrsLeft = tSizeStr.substring(0, p).split(BigUtil.SEP_ITEM);
	    		tNumStrsRight = tSizeStr.substring(p+1).split(BigUtil.SEP_ITEM);
    		}
		}
    																				//if the layout info in DB is not good, create it from beginning.
    	if(BigUtil.notCorrect(tBigTagStrsLeft, tBigTagStrsRight, tNumStrsLeft, tNumStrsRight)){
    		
    		List<BigTag> tBigTags = BigTag.findBMTagsByOwner(spaceOwner); 	//fetch out all tags owner's and his team's, 
    		List<Long> tTagIds = new ArrayList<Long>();						//then adjust it. @note: don't know if we can use AthenSet to move this into JPQL, because 
	    	for(int i = 0; i < tBigTags.size(); i++){						//here, we need to compare the tag names, to avoid duplication.
	    		tTagIds.add(tBigTags.get(i).getId());
	    	}					
	    	int tSize = tBigTags.size();									//Separate tags and IDs into 2 columns and prepare the Layout String.
	    	tNumStrsLeft = new String[tSize/2];
	    	tNumStrsRight = new String[tSize - tSize/2] ;
	    	
	    	StringBuilder tStrB = new StringBuilder();
	    	StringBuilder tStrB_Num = new StringBuilder();
    		for(int j = 0; j < tSize/2; j++){
    			BigTag tTag = tBigTags.get(j);
    	    	tBigTagsLeft.add(tBigTags.get(j));
    	    	tTagIdsLeft.add(tTagIds.get(j));
    	    	
    	    	tStrB.append(BigUtil.getTagInLayoutString(tTag));

    	    	tNumStrsLeft[j] = "8";
    	    	tStrB_Num.append(tNumStrsLeft[j]);
    	    	
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

    	    	tNumStrsRight[j - tSize/2] = "8";
    	    	tStrB_Num.append(tNumStrsRight[j - tSize/2]);
    	    	
    	    	if(j + 1 < tSize){
    	    		tStrB.append(BigUtil.SEP_ITEM);
        	    	tStrB_Num.append(BigUtil.SEP_ITEM);
    	    	}
	    	}
    		tStrB.append(BigUtil.SEP_TAG_NUMBER).append(tStrB_Num);

    		tOwner.setLayout(tStrB.toString());	    						//save the correct layout string back to DB
    		tOwner.persist();
    	}else{																			//prepare the info for view base on the string in db:
    		tBigTagsLeft = BigUtil.transferToTags(tBigTagStrsLeft, spaceOwner);
    		for(int i = 0; i < tBigTagsLeft.size(); i++){
    				tTagIdsLeft.add(tBigTagsLeft.get(i).getId());   //it can not be null, even if admin changed the name of the tags, cause it's handled in BigtUtil
    		}
    		
    		tBigTagsRight = BigUtil.transferToTags(tBigTagStrsRight, spaceOwner);
    		for(int i = 0; i < tBigTagsRight.size(); i++){
    				tTagIdsRight.add(tBigTagsRight.get(i).getId()); //it can not be null, even if admin changed the name of the tags, cause it's handled in BigtUtil
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
				
				//Determine if the add_as_friend/unfollow links should be displayed.
            	if(tCurUser.getListento().contains(tOwner)){
        			uiModel.addAttribute("nothireable", "true");
        		}else{
        			uiModel.addAttribute("notfireable", "true");
        		}
			}
		}
		
		Set<Integer> tAuthSet = BigAuthority.getAuthSet(tCurUser, tOwner);
        List<List> tContentListsLeft = new ArrayList<List>();								//prepare the contentList for each tag.
        List<List> tContentListsRight = new ArrayList<List>();								//prepare the contentList for each tag.
    	for(int i = 0; i < tBigTagsLeft.size(); i++){
    		tContentListsLeft.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsLeft.get(i), tOwner, tAuthSet,
    				0, Integer.valueOf(tNumStrsLeft[i]).intValue(), null));
    	}
    	for(int i = 0; i < tBigTagsRight.size(); i++){
    		tContentListsRight.add(
    				Content.findContentsByTagAndSpaceOwner(tBigTagsRight.get(i), tOwner, tAuthSet,
    				0, Integer.valueOf(tNumStrsRight[i]).intValue(), null));
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
        
        //====================prepare content for twitter area ============================
    	List<Twitter> twitterLeft = Twitter.findTwitterByPublisher(tOwner, tAuthSet, 0, 8, null);
    	//for this part it's alittle complex: it's about to display the twitters of the owner's friends. not the owner's, so it's not
    	//like if the logged in user is owner, then display all, if it's owner's friend friends display more, if it's stranger, then display only public ones.
    	//so, can not use the tauthset directly. the logic should be:
    	//if current user is owner, then display public and visible to friend ones, otherwise, display only public ones
    	tAuthSet = BigAuthority.getAuthSetForTwitterOfFriends(tCurUser, tOwner);
    	List<Twitter> twitterRight = null;
    	List<Twitter> twitterRightFix = null;
    	if(twitterLeft.size() == 0){
    		twitterRight = Twitter.findTwitterByOwner(tOwner, tAuthSet, 9, 8, null);
    		twitterRightFix = Twitter.findTwitterByOwner(tOwner, tAuthSet, 0, 9, null);
    	}else{
    		twitterRight = Twitter.findTwitterByOwner(tOwner, tAuthSet, 0, twitterLeft.size(), null);
    	}
        uiModel.addAttribute("twitterLeft", twitterLeft);
        uiModel.addAttribute("twitterRight", twitterRight);
        uiModel.addAttribute("twitterRightFix", twitterRightFix);
        //---------------------------------------------------------------------------------
        //to save the reqeust to requestCache;
    	/**If I add @ModelAttribute(), and add HttpServletRequest and HttpServletResponse in params, 
    	 * then will throw out exception (can not resolve a view with name tao), and this method will be called twice.
    	 * in the second time, the spaceOwner is null, so return a null....so can not use this way.
    	 */
        //BigAuthenticationSuccessHandler tHandler = SpringApplicationContext.getApplicationContext().getBean("authenticationSuccessHandler", BigAuthenticationSuccessHandler.class);
        //tHandler.getRequestCache().saveRequest(request, response);
        return "public/index";
    }
}
