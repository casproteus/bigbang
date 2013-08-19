package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.List;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.services.quartz.UpdatingBalanceJobProcessor;
import com.aeiou.bigbang.services.synchronization.ClientSyncTool;

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

	public static boolean isSystemCommand(String pCommand, UserAccount pCurUser){
		if("5203_setDefaultValueForContents".equals(pCommand)){
			setDefaultValueForContents();
			return true;
		} else if("2745_setDefaultValueForTags".equals(pCommand)){
			setDefaultValueForTags();
			return true;
		} else if("1214_updateUserBalances".equals(pCommand)){
			SpringApplicationContext.getApplicationContext().getBean("updatingBalanceJobProcessor", UpdatingBalanceJobProcessor.class).updateBalance();
			return true;
		} else if(("1210_syncdb".equals(pCommand) || "1210_syncdb_ua".equals(pCommand) || "1210_syncdb_tg".equals(pCommand)
				 || "1210_syncdb_ms".equals(pCommand)  || "1210_syncdb_bg".equals(pCommand) || "1210_syncdb_rm".equals(pCommand)  || "1210_syncdb_bm".equals(pCommand))
				 && pCurUser != null){	//Looks like first run will see exception of line 137, SynchizationManager(persistant rool back). will be OK when run it the second time or the third time.
			new ClientSyncTool().startToSynch(pCurUser, pCommand);
			return true;
		} else if(pCommand != null && pCommand.startsWith("20130818_shufful") && pCurUser != null){
			String tName = pCommand.substring("20130818_shufful".length()).trim();
			String tTagName = null;				//if there's a space in param, means, user specify not only a username but also a tag.
			int tPos = tName.indexOf(' ');		//that also means if the username contains a space, then his bookmark can not be shuttled.
			if(tPos > 0){
				tName = tName.substring(0, tPos);
				tTagName = tName.substring(tPos + 1);
			}
			UserAccount tUA = UserAccount.findUserAccountByName(tName);
			if(tUA != null){
				List<Content> tList = Content.findContentsByPublisher(tUA, BigAuthority.getAuthSet(tUA,tUA), 0, 0, null);
				if(tList != null){
					for (int i = 0; i < tList.size(); i++){
						Content tBM = tList.get(i);
						if(tTagName != null && !tTagName.equals(tBM.getCommonBigTag().getTagName()))
							continue;	//if the content is not in the categray defined in parameter, then don't change.
						
						UserAccount tGhostUA = getGhostUA();
						tBM.setPublisher(tGhostUA);
						if(tBM.getUncommonBigTag() != null){
							BigTag tTag = BigTag.findBMTagByNameAndOwner(tBM.getUncommonBigTag().getTagName(), tGhostUA.getName());
							if(tTag != null)
								tBM.setUncommonBigTag(tTag);
						}
						tBM.persist();
					}
				}
			}
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
	
	//reset the columns to be displayed on personal space, by default, we display only user's own and his friends tags.
	//if user want to display more tags, he can customize it later easily.
	public static void resetLayoutString(UserAccount pUser){
    	if(pUser == null) return;
    	
		List<BigTag> tBigTags = BigTag.findBMTagsByOwner(pUser.getName()); 	//fetch out all tags of admin's or owner's and his team's, 
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
	
	//transfer string form "in layout string" format to normal format (clean tag name format).
	public static String getTagNameFromLayoutStr(String pLayoutString){
		StringBuilder tStrB = new StringBuilder(pLayoutString);
		if(tStrB.charAt(0) == '¶')
			tStrB = tStrB.deleteCharAt(0);
		
		char tEndChar = tStrB.charAt(tStrB.length() - 1);
		if(tEndChar == '¶' || tEndChar == '' || tEndChar == '†')
			tStrB = tStrB.deleteCharAt(tStrB.length() - 1);
		
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
			if(!(tAryNumStrsLeft.length ==1 && tAryNumStrsLeft[0].length() == 0))		//in case that when the left column or right column have no tag to show, the string will be ""
				for(int i = tAryNumStrsLeft.length - 1; i >= 0; i--){
					Integer.parseInt(tAryNumStrsLeft[i]);
				}
			if(!(tAryNumStrsRight.length == 1 && tAryNumStrsRight[0].length() == 0))	//and when a "" is splid, the array returned will have one element, and it's "".so we allow "".
				for(int i = tAryNumStrsRight.length - 1; i >= 0; i--){
					Integer.parseInt(tAryNumStrsRight[i]);	
				}
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
	
	private static List<UserAccount> tGhostUAList;
	private static UserAccount getGhostUA(){
		if(tGhostUAList == null){
			tGhostUAList = new ArrayList<UserAccount>();
			UserAccount tU0 = UserAccount.findUserAccountByName("mwang");
			UserAccount tU1 = UserAccount.findUserAccountByName("gchen");
			UserAccount tU2 = UserAccount.findUserAccountByName("sha");
			UserAccount tU3 = UserAccount.findUserAccountByName("xJin");
			UserAccount tU4 = UserAccount.findUserAccountByName("gZhou");
			UserAccount tU5 = UserAccount.findUserAccountByName("AustinL");
			UserAccount tU6 = UserAccount.findUserAccountByName("James");
			UserAccount tU7 = UserAccount.findUserAccountByName("Gustsao");
			UserAccount tU8 = UserAccount.findUserAccountByName("SYLi");
			UserAccount tU9 = UserAccount.findUserAccountByName("Bobchu");
			UserAccount tU10 = UserAccount.findUserAccountByName("JackM");
			UserAccount tU11 = UserAccount.findUserAccountByName("HQ.J");
			UserAccount tU12 = UserAccount.findUserAccountByName("NancyS");
			UserAccount tU13 = UserAccount.findUserAccountByName("JaneH");
			UserAccount tU14 = UserAccount.findUserAccountByName("HerryY");
			UserAccount tU15 = UserAccount.findUserAccountByName("MarryLi");
			UserAccount tU16 = UserAccount.findUserAccountByName("MichaelM");
			UserAccount tU17 = UserAccount.findUserAccountByName("Jack99");
			UserAccount tU18 = UserAccount.findUserAccountByName("Sam2013");
			UserAccount tU19 = UserAccount.findUserAccountByName("David8");
			tGhostUAList.add(tU0);
			tGhostUAList.add(tU1);
			tGhostUAList.add(tU2);
			tGhostUAList.add(tU3);
			tGhostUAList.add(tU4);
			tGhostUAList.add(tU5);
			tGhostUAList.add(tU6);
			tGhostUAList.add(tU7);
			tGhostUAList.add(tU8);
			tGhostUAList.add(tU9);
			tGhostUAList.add(tU10);
			tGhostUAList.add(tU11);
			tGhostUAList.add(tU12);
			tGhostUAList.add(tU13);
			tGhostUAList.add(tU14);
			tGhostUAList.add(tU15);
			tGhostUAList.add(tU16);
			tGhostUAList.add(tU17);
			tGhostUAList.add(tU18);
			tGhostUAList.add(tU19);
		}
		int randomIdx = (int) ((19 - 0) * Math.random() + 0);
		return tGhostUAList.get(randomIdx);
	}
	//SpringApplicationContext.getApplicationContext().getBean("themeResolver", CookieThemeResolver.class).setDefaultThemeName("2");

}