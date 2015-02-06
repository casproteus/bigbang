package com.aeiou.bigbang.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;

public class SortableList {

	List<SortableTwitter> sortableTwitters;
	private List<SortableRemark> sortableRemarks;
	
	public SortableList(List<Twitter> list){
		sortableTwitters = new ArrayList<SortableList.SortableTwitter>();
		for(Twitter tw: list){
			sortableTwitters.add(new SortableTwitter(tw));
		}
	}
	
	public SortableList(List<Remark> list, int type){
		sortableRemarks = new ArrayList<SortableList.SortableRemark>();
		for(Remark rm: list){
			sortableRemarks.add(new SortableRemark(rm));
		}
	}
	
	public List<Twitter> getTwitters(){
		Collections.sort(sortableTwitters);
		List<Twitter> list = new ArrayList<Twitter>();
		for(SortableTwitter st : sortableTwitters){
			list.add(st.getTw());
		}
		return list;
	}
	
	public List<Remark> getRemarks() {
		Collections.sort(sortableRemarks);
		List<Remark> list = new ArrayList<Remark>();
		for(SortableRemark st : sortableRemarks){
			list.add(st.getRm());
		}
		return list;
	}

	private class SortableTwitter implements Comparable<SortableTwitter>{
		private Twitter tw;
		
		public SortableTwitter(Twitter tw){
			this.setTw(tw);
		}

		@Override
		public int compareTo(SortableTwitter o) {
			return ((SortableTwitter)o).getTw().getLastupdate().compareTo(getTw().getLastupdate());
		}

		public Twitter getTw() {
			return tw;
		}
		public void setTw(Twitter tw) {
			this.tw = tw;
		}
	}
	
	private class SortableRemark implements Comparable<SortableRemark>{
		private Remark rm;
		
		public SortableRemark(Remark tw){
			this.setRm(tw);
		}

		@Override
		public int compareTo(SortableRemark o) {
			return ((SortableRemark)o).getRm().getRemarkTime().compareTo(getRm().getRemarkTime());
		}

		public Remark getRm() {
			return rm;
		}
		public void setRm(Remark rm) {
			this.rm = rm;
		}
	}
}
