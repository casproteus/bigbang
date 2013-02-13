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
			return "SHOW_TO_EVERY_ONE";
		}else if(id == 1){
			return "ONLY_MYSELF_CAN_SEE";
		}else if(id == 2){
			return "ALL_MY_TEAM_CAN_SEE";
		}else if(id == 3){
			return "ONLY_FOR_SELECTED_PERSON";
		}else{
			return null;
		}
	}
	public static List<BigAuthority> getAllOptions(){
		List<BigAuthority> tArrayFR = new ArrayList<BigAuthority>();
		for(Short i = 0; i < 4; i++){
			BigAuthority tBigAuthority = new BigAuthority();
			tBigAuthority.setId(i);
			tArrayFR.add(tBigAuthority);
		}
		return tArrayFR;
	}
}
