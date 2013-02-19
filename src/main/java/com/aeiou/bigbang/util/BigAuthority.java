package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.List;


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
}
