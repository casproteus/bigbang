package com.aeiou.bigbang.web;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.secutiry.UserContextService;
import com.aeiou.bigbang.util.BigAuthority;
import com.aeiou.bigbang.util.BigUtil;

@RequestMapping("/bigtags")
@Controller
@RooWebScaffold(path = "bigtags", formBackingObject = BigTag.class)
public class BigTagController {
	@Inject
	private UserContextService userContextService;

	void populateEditForm(Model uiModel, BigTag bigTag) {
		uiModel.addAttribute("bigTag", bigTag);
        uiModel.addAttribute("authorities",BigAuthority.getAllOptions());
    }
	
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid BigTag bigTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (StringUtils.isEmpty(bigTag.getTagName())) {
            populateEditForm(uiModel, bigTag);
            return "bigtags/create";
        }
        
        String tCurName = userContextService.getCurrentUserName();
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tCurName);
        tCurName = tUserAccount.getName();	//because we allow user to login with capital characters
        if(StringUtils.isEmpty(bigTag.getType())){
        	bigTag.setType(tCurName);
        }
        uiModel.asMap().clear();
        bigTag.persist();
        
        //update the layout string of useraccount
        String tLayout = tUserAccount.getLayout();
   		int p = tLayout == null ? -1 : tLayout.indexOf('™');
   		if(p > -1){
			String tTagStr = tLayout.substring(0, p);
			String tSizeStr = tLayout.substring(p+1);
			StringBuilder tStrB = new StringBuilder();
			tStrB.append(tTagStr).append("¯");

			tStrB.append(BigUtil.getTagInLayoutString(bigTag));
		
			tStrB.append("™").append(tSizeStr).append("¯").append("8");
			tUserAccount.setLayout(tStrB.toString());
			tUserAccount.persist();
   		}else{
   			BigUtil.resetLayoutString(tUserAccount);
   		}
        return "redirect:/bigtags/" + encodeUrlPathSegment(bigTag.getId().toString(), httpServletRequest);
    }
	
	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid BigTag bigTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
		if (StringUtils.isEmpty(bigTag.getTagName())) {
            populateEditForm(uiModel, bigTag);
            return "bigtags/update";
        }
        
        //update the layout string of useraccount
		BigTag tBigTag = BigTag.findBigTag(bigTag.getId());
        UserAccount tUserAccount = UserAccount.findUserAccountByName(tBigTag.getType());
        String tLayout = tUserAccount == null ? null : tUserAccount.getLayout();
        int p = tLayout == null ? -1 : tLayout.indexOf('™');
   		if(p > -1){
   		   	String[] tAryTagStrsLeft = null;								//for generating the new layout string.
   		   	String[] tAryTagStrsRight = null;
   		   	String[] tAryNumStrsLeft = null;
   		   	String[] tAryNumStrsRight = null;
   		   	
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
   			
			//if the layout info in DB is not good, reset it.
			if(((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0) && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
					|| ((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0) && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
					|| (tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)){
				BigUtil.resetLayoutString(tUserAccount);
			}else{
	   			//---------adjusting the Sting Arys-------------
	   			//to find out the column and position
	   			tTagStr = BigUtil.getTagInLayoutString(tBigTag);
	   			String tTagStrNEW = BigUtil.getTagInLayoutString(bigTag);
	   			
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
	   			
   				if(tIsInLeftColumn){
   					String[] tAryTagStrsLeft2 = new String[tAryTagStrsLeft.length];
   					String[] tAryNumStrsLeft2 = new String[tAryNumStrsLeft.length];
   					for(int j = 0; j < tAryTagStrsLeft.length; j++){
   						if(j != tPos){
   							tAryTagStrsLeft2[j] = tAryTagStrsLeft[j];
   							tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
   						}else{
   							tAryTagStrsLeft2[j] = tTagStrNEW;
   							tAryNumStrsLeft2[j] = tAryNumStrsLeft[j];
   						}
   					}
   					tAryTagStrsLeft = tAryTagStrsLeft2;
   					tAryNumStrsLeft = tAryNumStrsLeft2;				
   				}else{
   					String[] tAryTagStrsRight2 = new String[tAryTagStrsRight.length];
   					String[] tAryNumStrsRight2 = new String[tAryNumStrsRight.length];
   					for(int j = 0; j < tAryTagStrsRight.length; j++){
   						if(j != tPos){
   							tAryTagStrsRight2[j] = tAryTagStrsRight[j];
   							tAryNumStrsRight2[j] = tAryNumStrsRight[j];
   						}else{
   							tAryTagStrsRight2[j] = tTagStrNEW;
   							tAryNumStrsRight2[j] = tAryNumStrsRight[j];
   						}
   					}
   					tAryTagStrsRight = tAryTagStrsRight2;
   					tAryNumStrsRight = tAryNumStrsRight2;
   				}
   			
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
	
	   	   		tUserAccount.setLayout(tStrB.toString());	    						//save the new layout string to DB
	   	   		tUserAccount.persist();
			}
   		}else{
   			BigUtil.resetLayoutString(tUserAccount);
   		}

        uiModel.asMap().clear();
        bigTag.merge();
        return "redirect:/bigtags/" + encodeUrlPathSegment(bigTag.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        BigTag bigTag = BigTag.findBigTag(id);
        bigTag.remove();
        
        //update the layout string of useraccount
        UserAccount tUserAccount = UserAccount.findUserAccountByName(bigTag.getType());
        String tLayout = tUserAccount == null ? null : tUserAccount.getLayout();
        int p = tLayout == null ? -1 : tLayout.indexOf('™');
   		if(p > -1){
   		   	String[] tAryTagStrsLeft = null;								//for generating the new layout string.
   		   	String[] tAryTagStrsRight = null;
   		   	String[] tAryNumStrsLeft = null;
   		   	String[] tAryNumStrsRight = null;
   		   	
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
   			
			//if the layout info in DB is not good,  reset it.
			if(((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0) && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
					|| ((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0) && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
					|| (tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)){
				BigUtil.resetLayoutString(tUserAccount);
			}else{
	   			//---------adjusting the Sting Arys-------------
	   			//to find out the column and position
	   			tTagStr = BigUtil.getTagInLayoutString(bigTag);	   			
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
	
	   	   		tUserAccount.setLayout(tStrB.toString());	    						//save the new layout string to DB
	   	   		tUserAccount.persist();
			}
   		}else{
   			BigUtil.resetLayoutString(tUserAccount);
   		}
   		
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/bigtags";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        String tCurName = userContextService.getCurrentUserName();
        if(tCurName == null)
        	return "login";
        tCurName = UserAccount.findUserAccountByName(tCurName).getName();
        
		int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
       
        float nrOfPages;
    	if(tCurName.equals("admin")){
    		uiModel.addAttribute("bigtags", BigTag.findBigTagEntries(firstResult, sizeNo));
    		nrOfPages = (float) BigTag.countBigTags() / sizeNo;
	        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
    	}else{
	        uiModel.addAttribute("bigtags", BigTag.findTagsByPublisher(tCurName, firstResult, sizeNo));
	        nrOfPages = (float) BigTag.countTagsByPublisher(tCurName) / sizeNo;
    	}
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "bigtags/list";
    }

}
