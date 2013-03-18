package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.List;

public class BigType {
	private Short id;
	
	public Short getId() {
		return id;
	}
	public void setId(Short id) {
		this.id = id;
	}
	
	public String toString(){
		if(id == 0){
			return "tag_for_bookmark";
		}else if(id == 1){
			return "tag_for_diary";
		}else{
			return null;
		}
	}
	
	public static List<BigType> getAllOptions(Integer pType){
		List<BigType> tArrayFR = new ArrayList<BigType>();
		if(pType == null){
			for(Short i = 0; i < 2; i++){
				BigType tBigAuthority = new BigType();
				tBigAuthority.setId(i);
				tArrayFR.add(tBigAuthority);
			}
		}else{
			BigType tBigAuthority = new BigType();
			tBigAuthority.setId(pType.shortValue());
			tArrayFR.add(tBigAuthority);
		}
		return tArrayFR;
	}
}
