package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.List;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Content;

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
    		if(tAryTagStrs[i].startsWith("¶")){
    			BigTag tTag = BigTag.findTagByNameAndOwner(tAryTagStrs[i].substring(1), "admin");
    			if(tTag != null)
    				tBigTags.add(tTag);
    		}else{
    			BigTag tTag = BigTag.findTagByNameAndOwner(tAryTagStrs[i], pOwnerName);
    			if(tTag != null)
    				tBigTags.add(tTag);
    		}
    	}
    	return tBigTags;
    }
}
