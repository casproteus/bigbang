package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aeiou.bigbang.domain.UserAccount;


public class BigAuthority {
	private Short id;
	
	public Short getId() {
		return id;
	}
	public void setId(Short id) {
		this.id = id;
	}
	
	public String toString(){
		if(id == 0){
			return "visible_to_everyone";
		}else if(id == 1){
			return "visible_to_only_myself";
		}else if(id == 2){
			return "visible_to_team_members";
		}else if(id == 3){
			return "visible_to_specific_person";
		}else if(id == 11){
			return "visible_only_to_author";
		}else{
			return null;
		}
	}

	public static List<BigAuthority> getAllOptions(){
		List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();
		for(Short i = 0; i < 3; i++){
			BigAuthority tBigAuthority = new BigAuthority();
			tBigAuthority.setId(i);
			tArrayFR.add(tBigAuthority);
		}
		return tArrayFR;
	}
	
	public static List<BigAuthority> getRemarkOptions(){
		List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();

		BigAuthority tBigAuthority = new BigAuthority();
		tBigAuthority.setId((short)0);
		tArrayFR.add(tBigAuthority);
		BigAuthority tBigAuthority2 = new BigAuthority();
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
    	}else if(pOwner.getListento().contains(pCurUser)){
    		tAuthSetFR.add(Integer.valueOf(2));
    	}else{//TODO: consider the case that visible to specific person.
    		
    	}
    	return tAuthSetFR;
	}
}
