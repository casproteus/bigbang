package com.aeiou.bigbang.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class RemarkAppender extends JFrame implements ActionListener, ListSelectionListener{
	String path = "http://my-bookmark.rhcloud.com/useraccounts/";
	//String path = "http://localhost/bigbang/useraccounts/";
	
	JTextPane editor;
	JTextPane twitterContent;
	JButton okBtn;
	JList blogList;
	JList remarkList;
	JSplitPane jsplidpane1;
	JSplitPane jsplidpane2;
	JSplitPane jsplidpane3;
	Calendar ca = Calendar.getInstance();
	UserAccount ua = new UserAccount();
	JScrollPane scrollPaneCenterNorth;
	List<Remark> allRemark;
	
	public RemarkAppender(){
		twitterContent = new JTextPane();
		editor = new JTextPane();
		okBtn = new JButton();
		blogList = new JList(getAllTopics().toArray());
		remarkList = new JList();
		JScrollPane scrollPaneTop = new JScrollPane();
		JScrollPane scrollPaneLeft = new JScrollPane();
		scrollPaneCenterNorth = new JScrollPane();
		JScrollPane scrollPaneCenterSouth = new JScrollPane();
		JPanel panelSouth = new JPanel();
		jsplidpane1 = new JSplitPane();
		jsplidpane2 = new JSplitPane();
		jsplidpane3 = new JSplitPane();
		
		//data preparation
		allRemark = getAllRemarks();
		
		ua.setName("tao");
		
		//button texts
		okBtn.setText("Save");
		twitterContent.setContentType("text/html");
		editor.setContentType("text/html");
		panelSouth.setLayout(new FlowLayout());
		jsplidpane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jsplidpane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jsplidpane1.setOneTouchExpandable(true);
		jsplidpane1.setDividerSize(6);
		jsplidpane2.setOneTouchExpandable(true);
		jsplidpane2.setDividerSize(6);
		jsplidpane3.setOneTouchExpandable(true);
		jsplidpane3.setDividerSize(6);

		jsplidpane1.setDividerLocation(160);
		jsplidpane2.setDividerLocation(160);
		jsplidpane3.setDividerLocation(160);
		
		getContentPane().add(jsplidpane3);
		
		scrollPaneTop.setViewportView(twitterContent);
		jsplidpane3.add(scrollPaneTop, JSplitPane.TOP);
		
		scrollPaneLeft.setViewportView(blogList);
		jsplidpane2.add(scrollPaneLeft, JSplitPane.TOP);
		
		//splid Vertical
		jsplidpane1.add(scrollPaneCenterNorth, JSplitPane.TOP);
		jsplidpane1.add(scrollPaneCenterSouth, JSplitPane.BOTTOM);
		scrollPaneCenterNorth.setViewportView(remarkList);
		scrollPaneCenterNorth.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneCenterSouth.setViewportView(editor);

		jsplidpane2.add(jsplidpane1, JSplitPane.BOTTOM);
		jsplidpane3.add(jsplidpane2, JSplitPane.BOTTOM);
		
		//south panel
		panelSouth.add(okBtn);
		getContentPane().add(panelSouth, BorderLayout.SOUTH);
		
		//listeners
		okBtn.addActionListener(this);
		blogList.addListSelectionListener(this);
		
		//other
		blogList.setCellRenderer(new ListCellRenderer() {
			
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				DefaultListCellRenderer dcr = new DefaultListCellRenderer();
				
				dcr.setText(((Twitter)value).getTwtitle());
				dcr.setBackground(index%2 == 0 ? Color.white : Color.lightGray);
				if(isSelected){
					dcr.setBackground(Color.green);
				}
				
				return dcr;
			}
		});
	}
	
	private List<Twitter> getAllTopics(){
		Client client = Client.create(new DefaultClientConfig());
		WebResource webResource = client.resource(path + "1210_syncdb_bg");		//have to add an value: /{pCommand}, to distinguish
		webResource.accept("application/json");											// other methods in controller.
		
				
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, null);
		if(200 == response.getStatus()){
			String responseEntity = (String)response.getEntity(String.class);
			JSONDeserializer<List<String>> deserializer = new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class);
			List<String> tList = deserializer.deserialize(responseEntity);
			List<Twitter> tBlogList =  new JSONDeserializer<List<Twitter>>().use(null, ArrayList.class).use("values", Twitter.class).deserialize(tList.get(0));
			SortableList sl = new SortableList(tBlogList);
			return sl.getTwitters();
		}
		return null;
	}
	
	private List<Remark> getAllRemarks(){
		Client client = Client.create(new DefaultClientConfig());
		WebResource webResource = client.resource(path +"1210_syncdb_rm");		//have to add an value: /{pCommand}, to distinguish
		webResource.accept("application/json");											// other methods in controller.
		
				
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, null);
		if(200 == response.getStatus()){
			String responseEntity = (String)response.getEntity(String.class);
			JSONDeserializer<List<String>> deserializer = new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class);
			List<String> tList = deserializer.deserialize(responseEntity);
			List<Remark> tRemarkList =  new JSONDeserializer<List<Remark>>().use(null, ArrayList.class).use("values", Remark.class).deserialize(tList.get(0));
			return tRemarkList;
		}
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(blogList.getSelectedValue() == null){
			jsplidpane2.setDividerLocation(100);
			blogList.setSelectedIndex(0);
			return;
		}
		if(editor.getText() == null || editor.getText().length() == 0){
			jsplidpane1.setDividerLocation(100);
			editor.requestFocus();
			return;
		}
		
		Client client = Client.create(new DefaultClientConfig());
		WebResource webResource = client.resource(path + "1210_syncdb_rm");		//have to add an value: /{pCommand}, to distinguish
		webResource.accept("application/json");											// other methods in controller.
		Remark rm = new Remark();
		rm.setAuthority(0);
		rm.setContent(editor.getText());
		
		rm.setPublisher(ua);
		rm.setRemarkTime(new Date());
		
		Twitter tw = (Twitter)blogList.getSelectedValue();
		rm.setRemarkto(tw);
		
		ArrayList<Remark> al = new ArrayList<Remark>();
		al.add(rm);
				
		String tRemarkJsonAryStr = Remark.toJsonArray(al);
        ArrayList<String> alc = new ArrayList<String>();
        alc.add(tRemarkJsonAryStr); 
        
		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, 
				new JSONSerializer().exclude("*.class").serialize(alc)
				);
		if(200 == response.getStatus()){
			List<String> tList = new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class).deserialize((String)response.getEntity(String.class));
			allRemark = getAllRemarks();
			valueChanged(null);
			
			editor.setText("");
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource() == remarkList){
			Remark rm = (Remark)remarkList.getSelectedValue();
			twitterContent.setText(rm.getContent());
		}else if(e.getSource() == blogList){
			Twitter tw = (Twitter)blogList.getSelectedValue();
			twitterContent.setText(tw.getTwitent());
			
			remarkList = new JList(getReleventRemarks(tw).toArray());
			remarkList.setCellRenderer(new ListCellRenderer() {
				
				@Override
				public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
					JTextPane textPane = new JTextPane();
					textPane.setContentType("text/html");
			
					textPane.setText(((Remark)value).getContent());
					textPane.invalidate();
					textPane.revalidate();
					textPane.validate();
					//TODO: here don't know how to set to the right height, so some text will not be visible when shrink.
					textPane.setPreferredSize(new Dimension(remarkList.getWidth(), textPane.getPreferredSize().height));
					textPane.setBackground(index%2 == 0 ? Color.white : Color.lightGray);
					if(isSelected){
						textPane.setBackground(Color.green);
					}
					
					return textPane;
				}
			});
			remarkList.addListSelectionListener(this);
			scrollPaneCenterNorth.setViewportView(remarkList);
		}
	}
	
	private List<Remark> getReleventRemarks(Twitter tw){
		List<Remark> remarks = new ArrayList<Remark>();
		for(Remark rm : allRemark){
			if(rm.getRemarkto() != null && rm.getRemarkto().getTwtitle() != null){
				if(rm.getRemarkto().getTwtitle().equals(tw.getTwtitle()) ){
					remarks.add(rm);
				}
			}
		}
		SortableList sl = new SortableList(remarks, 1);
		return sl.getRemarks();
	}
	
//============================main==========================================
	public static void main(String[] args){
		RemarkAppender frame = new RemarkAppender();
		frame.setBounds(0, 0, 1200, 740);
		frame.setVisible(true);
	}
}
