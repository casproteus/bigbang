package com.aeiou.bigbang.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigUtil;
import com.aeiou.bigbang.util.SpringApplicationContext;

@RequestMapping("/customizes")
@Controller
@RooWebScaffold(path = "customizes", formBackingObject = Customize.class)
@RooWebJson(jsonObject = Customize.class)
public class CustomizeController {

	@Inject
	private UserContextService userContextService;
	
    @RequestMapping(params = "relayouttype", produces = "text/html")
    public String displayTagStatus(@RequestParam(value = "relayouttype", required = true) String relayouttype, 
    		HttpServletRequest request, Model uiModel) {
    	String tCurName = userContextService.getCurrentUserName();
    	if(tCurName ==  null){
    		return("login");
    	}
		UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);
		tCurName = tOwner.getName();
		
		List<BigTag> commonCheckedBMTags = BigTag.findBMTagsByOwner(null);				//the tags created by admin and administrators
		List<BigTag> uncommonCheckedBMTags = BigTag.findBMTagsByOwner(tCurName);		//the tags created by user and user's friends.
		String layoutString = tOwner.getLayout();
		List<BigTag> commonUnCheckedBMTags = new ArrayList<BigTag>();
		List<BigTag> uncommonUnCheckedBMTags = new ArrayList<BigTag>();
		
		for (int i = commonCheckedBMTags.size() - 1; i >= 0; i--){
			String tTagStr = BigUtil.getTagInLayoutString(commonCheckedBMTags.get(i));
			if(layoutString.indexOf(tTagStr) < 0){
				commonUnCheckedBMTags.add(commonCheckedBMTags.get(i));
				commonCheckedBMTags.remove(i);
			}
		}

		for (int i = uncommonCheckedBMTags.size() - 1; i >= 0; i--){
			String tTagStr = BigUtil.getTagInLayoutString(uncommonCheckedBMTags.get(i));
			if(layoutString.indexOf(tTagStr) < 0){
				uncommonUnCheckedBMTags.add(uncommonCheckedBMTags.get(i));
				uncommonCheckedBMTags.remove(i);
			}
		}

		uiModel.addAttribute("commonCheckedBMTags", commonCheckedBMTags);
		uiModel.addAttribute("uncommonCheckedBMTags", uncommonCheckedBMTags);
		
		uiModel.addAttribute("commonUnCheckedBMTags", commonUnCheckedBMTags);
		uiModel.addAttribute("uncommonUnCheckedBMTags", uncommonUnCheckedBMTags);
	    return "customizes/tagsDisplay";
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping(params = "updateTagsToShow", produces = "text/html")
    public String updateTagsToShow(@RequestParam(value = "updateTagsToShow", required = true) String relayouttype, 
    		HttpServletRequest request, Model uiModel) {

    	String tCurName = userContextService.getCurrentUserName();
    	if(tCurName ==  null){
    		return("login");
    	}
		UserAccount tOwner = UserAccount.findUserAccountByName(tCurName);
		tCurName = tOwner.getName();
    	
    	//get the layout info from DB. and split them into string[]
    	String[] tBigTagStrsLeft = null;
    	String[] tBigTagStrsRight = null;
    	String[] tNumStrsLeft = null;
    	String[] tNumStrsRight = null;
    	String tLayout = tOwner.getLayout();										
    	int p = tLayout == null ? -1 : tLayout.indexOf('™');
		if(p > -1){
    		String tTagStr = tLayout.substring(0, p);
    		String tSizeStr = tLayout.substring(p+1);
    		
    		p = tTagStr.indexOf('¬');
    		if(p >= 0){
	    		tBigTagStrsLeft = tTagStr.substring(0, p).split("¯"); //when an empty string is splited, the returned ary will be have one element(which is empty).
	    		tBigTagStrsRight = tTagStr.substring(p+1).split("¯");
    		}
    		p = tSizeStr.indexOf('¬');
    		if(p >= 0){
	    		tNumStrsLeft = tSizeStr.substring(0, p).split("¯");
	    		tNumStrsRight = tSizeStr.substring(p+1).split("¯");
    		}
		}//----------------------------------------
    	
		//no need to check if the layout info in DB is good, because when log in, we display personal page, when display personal page, we'll check the layout string and fix it.

		//if any one in existing list not checked, then remove it from list and number list
		//also remove it from the tListAllTag list, because we'll check the tags left, to see if there's any one setted as checked, we'll add them to the end.
		//List<String> tListAllTag = BigTag.findBMAllTagsStringByOwner(tCurName);
		//List<BigTag> tListAllTag = BigTag.findBMAllTagsByOwner(tCurName);
		@SuppressWarnings("rawtypes")
		Map tMap = new HashMap();
		tMap.putAll(request.getParameterMap());		//this way to make the tMap writable, and it contains all the checked item from page.
		StringBuilder tLayoutStrBuilder = new StringBuilder();
		StringBuilder tNumStrBuilder = new StringBuilder();
		boolean tmpFlag = false;					//use this flag to make the first time don't add "¯".
    	for(int i = 0; i < tBigTagStrsLeft.length; i++){
    		if(tBigTagStrsLeft[i].length() > 0 && tMap.get(BigUtil.getTagNameFromLayoutStr(tBigTagStrsLeft[i])) != null){
    			if(tmpFlag == true){
    				tLayoutStrBuilder.append("¯");
        			tLayoutStrBuilder.append(tBigTagStrsLeft[i]);
    				tNumStrBuilder.append("¯");
        			tNumStrBuilder.append(tNumStrsLeft[i]);
    			}else{
        			tLayoutStrBuilder.append(tBigTagStrsLeft[i]);
        			tNumStrBuilder.append(tNumStrsLeft[i]);
        			tmpFlag = true;
    			}
    			tMap.remove(BigUtil.getTagNameFromLayoutStr(tBigTagStrsLeft[i]));
    		}
    	}
    	tmpFlag = false;
    	tLayoutStrBuilder.append("¬");
    	tNumStrBuilder.append("¬");
    	for(int i = 0; i < tBigTagStrsRight.length; i++){
    		if(tBigTagStrsRight[i].length() > 0 && tMap.get(BigUtil.getTagNameFromLayoutStr(tBigTagStrsRight[i])) != null){
    			if(tmpFlag == true){
    				tLayoutStrBuilder.append("¯");
        			tLayoutStrBuilder.append(tBigTagStrsRight[i]);
    				tNumStrBuilder.append("¯");
        			tNumStrBuilder.append(tNumStrsRight[i]);
    			}else{
        			tLayoutStrBuilder.append(tBigTagStrsRight[i]);
        			tNumStrBuilder.append(tNumStrsRight[i]);
        			tmpFlag = true;
    			}
    			tMap.remove(BigUtil.getTagNameFromLayoutStr(tBigTagStrsRight[i]));
    		}
    	}
    	
    	//add new added tags to the end of both taglist and number list.
    	Object[] tKeys = tMap.keySet().toArray();
    	for (int i = 0; i < tKeys.length; i++){
    		if("on".equals(((String[])tMap.get(tKeys[i]))[0])){
    			if(tmpFlag == true){
	    			tLayoutStrBuilder.append("¯");
	    			tLayoutStrBuilder.append(BigUtil.getTagInLayoutString(BigTag.findBMTagByNameAndOwner((String)tKeys[i], tCurName)));
					tNumStrBuilder.append("¯");
	    			tNumStrBuilder.append("8");
    			}else{
        			tLayoutStrBuilder.append(BigUtil.getTagInLayoutString(BigTag.findBMTagByNameAndOwner((String)tKeys[i], tCurName)));
        			tNumStrBuilder.append("8");
        			tmpFlag = true;
    			}
    		}
    	}
    	
    	//save the new layout string into DB
    	tOwner.setLayout(tLayoutStrBuilder.append("™").append(tNumStrBuilder).toString()); 						//save the correct layout string back to DB
    	tOwner.persist();
    	
    	//go to personal page;
    	 PersonalController tController = SpringApplicationContext.getApplicationContext().getBean("personalController", PersonalController.class);
         return tController.index(tCurName, -1, -1, uiModel, request);
    }
}