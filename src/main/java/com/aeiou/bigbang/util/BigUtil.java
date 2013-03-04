package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.MessageSource;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.UserAccount;

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

	public static void setDefaultValueForTags(){
		List<BigTag> tList = BigTag.findAllBigTags();
		for(int i = 0; i < tList.size(); i++){
			BigTag tBigTag = tList.get(i);
			if(tBigTag.getAuthority() == null){
				tBigTag.setAuthority(new Integer(0));
				tBigTag.persist();
			}else if(tBigTag.getAuthority() instanceof Integer){
				System.out.println("Y : " + tBigTag.getAuthority().intValue());
			}else{
				System.out.println("N : " + tBigTag.getAuthority());
				tBigTag.setAuthority(new Integer(0));
				tBigTag.persist();
			}
		}
	}
	
	public static void setDefaultValueForContents(){
		List<Content> tList = Content.findAllContents();
		for(int i = 0; i < tList.size(); i++){
			Content tContent = tList.get(i);
			if(tContent.getAuthority() == null){
				tContent.setAuthority(new Integer(0));
				tContent.persist();
			}else if(tContent.getAuthority() instanceof Integer){
				System.out.println("Y : " + tContent.getAuthority().intValue());
			}else{
				System.out.println("N : " + tContent.getAuthority());
				tContent.setAuthority(new Integer(0));
				tContent.persist();
			}
		}
	}
	
	public static boolean isSystemCommand(String pCommand){
		if("2745_setDefaultValueForContents".equals(pCommand)){
			setDefaultValueForContents();
			return true;
		}
		
		if("1214_setDefaultValueForTags".equals(pCommand)){
			setDefaultValueForTags();
			return true;
		}
		
		return false;
	}
	
	public static List<BigTag> transferToTags(String[] tAryTagStrs, String pOwnerName){
    	List<BigTag> tBigTags = new ArrayList<BigTag>();
    	for(int i = 0; i < tAryTagStrs.length; i++){
//    		System.out.println("i:" + i);
//    		System.out.println("tAryTagStrs[i]:" + tAryTagStrs[i]);
    		if(tAryTagStrs[i].endsWith("�") ||tAryTagStrs[i].endsWith("") ||tAryTagStrs[i].endsWith("�"))
    			tAryTagStrs[i] = tAryTagStrs[i].substring(0, tAryTagStrs[i].length() - 1);
    		
    		if(tAryTagStrs[i].startsWith("�")){
    			BigTag tTag = BigTag.findTagByNameAndOwner(tAryTagStrs[i].substring(1), "admin");
    			if(tTag != null)
    				tBigTags.add(tTag);
    			else{
    				tTag = BigTag.findTagByNameAndOwner(tAryTagStrs[i].substring(1), "administrator");
    				tBigTags.add(tTag);
    			}
    		}else{
    			BigTag tTag = BigTag.findTagByNameAndOwner(tAryTagStrs[i], pOwnerName);
    			if(tTag != null)
    				tBigTags.add(tTag);
    		}
    	}
    	return tBigTags;
    }
	
	public static void addDefaultUserTags(MessageSource messageSource, String pType){BigTag tBigTag1 = new BigTag();
	    tBigTag1.setTagName(messageSource.getMessage("admin_suggested_tag1", new Object[0], null));
	    tBigTag1.setType(pType);
	    tBigTag1.setAuthority(0);
	    tBigTag1.persist();
	    
	    BigTag tBigTag2 = new BigTag();
	    tBigTag2.setTagName(messageSource.getMessage("admin_suggested_tag2", new Object[0], null));
	    tBigTag2.setType(pType);
	    tBigTag2.setAuthority(0);
	    tBigTag2.persist();
	    
	    BigTag tBigTag3 = new BigTag();
	    tBigTag3.setTagName(messageSource.getMessage("admin_suggested_tag3", new Object[0], null));
	    tBigTag3.setType(pType);
	    tBigTag3.setAuthority(0);
	    tBigTag3.persist();
	    
	    BigTag tBigTag4 = new BigTag();
	    tBigTag4.setTagName(messageSource.getMessage("admin_suggested_tag4", new Object[0], null));
	    tBigTag4.setType(pType);
	    tBigTag4.setAuthority(0);
	    tBigTag4.persist();
	}
	
	public static void resetLayoutString(UserAccount pUser){
    	
		List<BigTag> tBigTags = BigTag.findTagsByOwner(pUser.getName()); 	//fetch out all tags of admin's, owner's and his team's, 
    	int tSize = tBigTags.size();									//Separate tags and IDs into 2 columns and prepare the Layout String.
    	
    	StringBuilder tStrB = new StringBuilder();
    	StringBuilder tStrB_Num = new StringBuilder();
		for(int j = 0; j < tSize/2; j++){
	    	tStrB.append(getTagInLayoutString(tBigTags.get(j)));
	    	
	    	tStrB_Num.append("8");
	    	
	    	if(j + 1 < tSize/2){
	    		tStrB.append('�');
    	    	tStrB_Num.append('�');
	    	}
    	}

		tStrB.append('�');
		tStrB_Num.append('�');
		
		for(int j = tSize/2; j < tSize; j++){
	    	tStrB.append(getTagInLayoutString(tBigTags.get(j)));
	    	
	    	tStrB_Num.append("8");
	    	
	    	if(j + 1 < tSize){
	    		tStrB.append('�');
    	    	tStrB_Num.append('�');
	    	}
    	}
		tStrB.append('�').append(tStrB_Num);

		pUser.setLayout(tStrB.toString());	    						//save to DB
		pUser.persist();
	}
	
	public static String getTagInLayoutString(BigTag pTag){
		
		StringBuilder tStrB = new StringBuilder();
		
		if("admin".equals(pTag.getType()) || "administrator".equals(pTag.getType()))
    		tStrB.append('�');
    	
    	tStrB.append(pTag.getTagName());
    	
    	switch (pTag.getAuthority()) {
		case 1:
			tStrB.append("�");
			break;
		case 2:
			tStrB.append("");
			break;
		case 3:
			tStrB.append("�");
			break;
		default:
			break;
		}
    	
    	return tStrB.toString();
	}

}
