package com.aeiou.bigbang.services.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.UserAccount;
import com.aeiou.bigbang.util.BigUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import flexjson.JSONDeserializer;

@Component("checkAliveJobProcessor")
public class StgoCheckProcessor {

	private final Log log = LogFactory.getLog(getClass());

	/** 
	 */
	public synchronized void stgoCheck() {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);				//if we need a ssh, can go on work in this part.
		
		//get the string out from customizes
		Customize tCustomize = Customize.findCustomizeByKey("stgo");
		if(tCustomize == null)
			return;
		
		String[] urls = tCustomize.getCusValue().split(",");
		for(int i = 0; i < urls.length; i++){
			String tURL = urls[i].trim();
			if(tURL.endsWith("/"))
				tURL = tURL.substring(0, tURL.length() - 1);
			if(!tURL.startsWith("http://") && !tURL.startsWith("https://"))
				tURL = "http://" + tURL;
			
			WebResource webResource = client.resource(tURL + "/stgocheck/stgo");		//have to add an value: /{pCommand}, to distinguish
			webResource.accept("application/json");																		// other methods in controller.
			try{
				ClientResponse response = webResource.type("application/json").post(ClientResponse.class);
				if(200 == response.getStatus()){
					//DO nothing.
				}else{
					//@TDOO: what if the website not OK? can I catch all the case here? including server 
					BigUtil.sendMessage("info@sharethegoodones.com", 
							"SERVER STOPED!--" +  tURL, 
							"tao@sharethegoodones.com", 
							"just found the server not normal, please check it out!");
				}
			}catch(Exception e){
				BigUtil.sendMessage("info@sharethegoodones.com", 
						"SERVER STOPED!--" +  tURL, 
						"tao@sharethegoodones.com", 
						"just found the server crashed, please check it out!");
			}
		}
	}
}
