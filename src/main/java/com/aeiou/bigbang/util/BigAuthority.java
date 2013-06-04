package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.MessageSource;

import com.aeiou.bigbang.domain.UserAccount;


public class BigAuthority {
	
	public BigAuthority(MessageSource pMessageSource, Locale pLocale){
		messageSource = pMessageSource;
		locale = pLocale;
	}
	
	
	public String toString(){
		if(id == 0){
			return messageSource.getMessage("SHOW_TO_EVERY_ONE", null, locale);
		}else if(id == 1){
			return messageSource.getMessage("ONLY_MYSELF_CAN_SEE", null, locale);
		}else if(id == 2){
			return messageSource.getMessage("ALL_MY_TEAM_CAN_SEE", null, locale);
		}else if(id == 3){
			return messageSource.getMessage("ONLY_FOR_SELECTED_PERSON", null, locale);
		}else if(id == 11){
			return messageSource.getMessage("ONLY_FOR_RECEIVER", null, locale);
		}else{
			return null;
		}
	}

	public static List<BigAuthority> getAllOptions(MessageSource messageSource, Locale local){
		List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();
		for(Short i = 0; i < 3; i++){
			BigAuthority tBigAuthority = new BigAuthority(messageSource, local);
			tBigAuthority.setId(i);
			tArrayFR.add(tBigAuthority);
		}
		return tArrayFR;
	}
	
	public static List<BigAuthority> getRemarkOptions(MessageSource messageSource, Locale locale){
		List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();

		BigAuthority tBigAuthority = new BigAuthority(messageSource, locale);
		tBigAuthority.setId((short)0);
		tArrayFR.add(tBigAuthority);
		BigAuthority tBigAuthority2 = new BigAuthority(messageSource, locale);
		tBigAuthority2.setId((short)11);
		tArrayFR.add(tBigAuthority2);
		
		return tArrayFR;
	}
	
	public static Set<Integer> getAuthSet(UserAccount pCurUser, UserAccount pOwner){
		Set<Integer> tAuthSetFR = new HashSet<Integer>();
    	tAuthSetFR.add(Integer.valueOf(0));
    	if(pOwner.equals(pCurUser)){
    		tAuthSetFR.add(Integer.valueOf(1));
    		tAuthSetFR.add(Integer.valueOf(2));
    		tAuthSetFR.add(Integer.valueOf(3));
    		tAuthSetFR.add(Integer.valueOf(11));
    	}else if(pOwner.getListento().contains(pCurUser)){
    		tAuthSetFR.add(Integer.valueOf(2));
    	}else{//TODO: consider the case that visible to specific person.
    		
    	}
    	return tAuthSetFR;
	}
	

	private Short id;
	private MessageSource messageSource;
	private Locale locale;
	
	public Short getId() {
		return id;
	}
	public void setId(Short id) {
		this.id = id;
	}
}
