package com.aeiou.bigbang.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.aeiou.bigbang.domain.Customize;

public class BigType {

	private Locale locale;
	private Short id;
	
	public BigType(Locale pLocale){
		locale = pLocale;
	}
	
	public String toString(){
		String lang = "_" + (locale == null || locale.getLanguage() == null ? "" : locale.getLanguage());
		if(id == 0){
			Customize tCustomize = Customize.findCustomizeByKey("tag_for_bookmark" + lang);
			if(tCustomize == null){
				tCustomize = Customize.findCustomizeByKey("tag_for_bookmark");
			}
			return tCustomize == null ? "For Favorite" : tCustomize.getCusValue();
		}else if(id == 1){
			Customize tCustomize = Customize.findCustomizeByKey("tag_for_diary" + lang);
			if(tCustomize == null){
				tCustomize = Customize.findCustomizeByKey("tag_for_diary");
			}
			return tCustomize == null ? "For Note" : tCustomize.getCusValue();
		}else{
			return null;
		}
	}
	
	public static List<BigType> getAllOptions(Integer pType, Locale local){
		List<BigType> tArrayFR = new ArrayList<BigType>();
		if(pType == null){
			for(Short i = 0; i < 2; i++){
				BigType tBigAuthority = new BigType(local);
				tBigAuthority.setId(i);
				tArrayFR.add(tBigAuthority);
			}
		}else{
			BigType tBigAuthority = new BigType(local);
			tBigAuthority.setId(pType.shortValue());
			tArrayFR.add(tBigAuthority);
		}
		return tArrayFR;
	}
	
	public Short getId() {
		return id;
	}
	public void setId(Short id) {
		this.id = id;
	}
}
