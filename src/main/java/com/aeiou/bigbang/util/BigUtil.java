package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.MessageSource;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.quartz.UpdatingBalanceJobProcessor;

public class BigUtil {

	public static String getUTFString(String pString){
		byte tByteAry[];
		try{
			tByteAry = pString.getBytes("ISO-8859-1");
			pString = new String(tByteAry, "UTF-8");
		}catch(Exception e){
			//so it's not "ISO-8859-1" encoded.
		}
		return pString;
	}

	public static boolean isSystemCommand(String pCommand){
		if("5203_setDefaultValueForContents".equals(pCommand)){
			setDefaultValueForContents();
			return true;
		} else if("2745_setDefaultValueForTags".equals(pCommand)){
			setDefaultValueForTags();
			return true;
		} else if("1214_updateUserBalances".equals(pCommand)){
			SpringApplicationContext.getApplicationContext().getBean("updatingBalanceJobProcessor", UpdatingBalanceJobProcessor.class).updateBalance();
			return true;
		} else if("1210_syncdb".equals(pCommand)){
			
			return true;
		}
		
		return false;
	}
	
	public static List<BigTag> transferToTags(String[] tAryTagStrs, String pOwnerName){
    	List<BigTag> tBigTags = new ArrayList<BigTag>();
    	for(int i = 0; i < tAryTagStrs.length; i++){
    		//System.out.println("i:" + i);
    		//System.out.println("tAryTagStrs[i]:" + tAryTagStrs[i]);
    		if(tAryTagStrs[i].endsWith("¶") ||tAryTagStrs[i].endsWith("") ||tAryTagStrs[i].endsWith("†"))
    			tAryTagStrs[i] = tAryTagStrs[i].substring(0, tAryTagStrs[i].length() - 1);
    		
    		if(tAryTagStrs[i].startsWith("¶")){
    			BigTag tTag = BigTag.findBMTagByNameAndOwner(tAryTagStrs[i].substring(1), "admin");
    			if(tTag != null)
    				tBigTags.add(tTag);
    			else{
    				tTag = BigTag.findBMTagByNameAndOwner(tAryTagStrs[i].substring(1), "administrator");
    				if(tTag != null)
    					tBigTags.add(tTag);
    			}
    		}else{
    			BigTag tTag = BigTag.findBMTagByNameAndOwner(tAryTagStrs[i], pOwnerName);
    			if(tTag != null)
    				tBigTags.add(tTag);
    		}
    	}
    	return tBigTags;
    }
	
	
	
	/**
	 * update the lastupdate field of twitter.
	 */
	public static void refreshULastUpdateTimeOfTwitter(Remark remark){
		remark = Remark.findRemark(remark.getId());	//this remark may got from webpage, and has no some field like "remarkto"
        Twitter tTwitter = remark.getRemarkto();
        tTwitter.setLastupdate(remark.getRemarkTime());
        tTwitter.merge();
	}
	
	public static void resetLayoutString(UserAccount pUser){
    	if(pUser == null) return;
    	
		List<BigTag> tBigTags = BigTag.findBMTagsByOwner(pUser.getName()); 	//fetch out all tags of admin's, owner's and his team's, 
    	int tSize = tBigTags.size();									//Separate tags and IDs into 2 columns and prepare the Layout String.
    	
    	StringBuilder tStrB = new StringBuilder();
    	StringBuilder tStrB_Num = new StringBuilder();
		for(int j = 0; j < tSize/2; j++){
	    	tStrB.append(getTagInLayoutString(tBigTags.get(j)));
	    	
	    	tStrB_Num.append("8");
	    	
	    	if(j + 1 < tSize/2){
	    		tStrB.append('¯');
    	    	tStrB_Num.append('¯');
	    	}
    	}

		tStrB.append('¬');
		tStrB_Num.append('¬');
		
		for(int j = tSize/2; j < tSize; j++){
	    	tStrB.append(getTagInLayoutString(tBigTags.get(j)));
	    	
	    	tStrB_Num.append("8");
	    	
	    	if(j + 1 < tSize){
	    		tStrB.append('¯');
    	    	tStrB_Num.append('¯');
	    	}
    	}
		tStrB.append('™').append(tStrB_Num);

		pUser.setLayout(tStrB.toString());	    						//save to DB
		pUser.persist();
	}
	
	public static String getTagInLayoutString(BigTag pTag){
		
		StringBuilder tStrB = new StringBuilder();
		
		if("admin".equals(pTag.getType()) || "administrator".equals(pTag.getType()))
    		tStrB.append('¶');
    	
    	tStrB.append(pTag.getTagName());
    	
    	switch (pTag.getAuthority()) {
		case 1:
			tStrB.append("¶");
			break;
		case 2:
			tStrB.append("");
			break;
		case 3:
			tStrB.append("†");
			break;
		default:
			break;
		}
    	
    	return tStrB.toString();
	}
	
    public static boolean notCorrect(String[] tAryTagStrsLeft, String[] tAryTagStrsRight, String[] tAryNumStrsLeft, String[] tAryNumStrsRight){
    	if((tAryTagStrsLeft == null || tAryTagStrsLeft.length == 0) && (tAryTagStrsRight == null || tAryTagStrsRight.length == 0))
    		return true;
		if((tAryNumStrsLeft == null || tAryNumStrsLeft.length == 0) && (tAryNumStrsRight == null || tAryNumStrsRight.length == 0))
			return true;
		if(tAryTagStrsLeft.length != tAryNumStrsLeft.length || tAryTagStrsRight.length != tAryNumStrsRight.length)
			return true;
		try{
			for(int i = tAryNumStrsLeft.length - 1; i >= 0; i--)
				Integer.parseInt(tAryNumStrsLeft[i]);			
			for(int i = tAryNumStrsRight.length - 1; i >= 0; i--)
				Integer.parseInt(tAryNumStrsRight[i]);			
		}catch(Exception e){
			return true;
		}
		
		return false;
    }
    
	private static void setDefaultValueForTags(){
		List<BigTag> tList = BigTag.findAllBigTags();
		for(int i = 0; i < tList.size(); i++){
			BigTag tBigTag = tList.get(i);
			
			if(tBigTag.getAuthority() == null){
				tBigTag.setAuthority(new Integer(0));
				tBigTag.persist();
			}else if(!(tBigTag.getAuthority() instanceof Integer)){
				//System.out.println("N : " + tBigTag.getAuthority());
				tBigTag.setAuthority(new Integer(0));
				tBigTag.persist();
			}
			
			if(tBigTag.getOwner() == null){
				tBigTag.setOwner(new Integer(0));
				tBigTag.persist();
			}
		}
	}
	
	private static void setDefaultValueForContents(){
		List<Content> tList = Content.findAllContents();
		for(int i = 0; i < tList.size(); i++){
			Content tContent = tList.get(i);
			if(tContent.getAuthority() == null){
				tContent.setAuthority(new Integer(0));
				tContent.persist();
			}else if(tContent.getAuthority() instanceof Integer){
				//System.out.println("Y : " + tContent.getAuthority().intValue());
			}else{
				//System.out.println("N : " + tContent.getAuthority());
				tContent.setAuthority(new Integer(0));
				tContent.persist();
			}
		}
	}

}