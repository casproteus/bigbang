package com.aeiou.bigbang.services.synchronization;

//import javax.net.ssl.SSLContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class MainApplication {
	public static void main(String[] args){
		ClientConfig config = new DefaultClientConfig();
//		SSLContext ctx = SSLContext.getInstance("SSL");
//		ctx.init(null, myTrustManager, null);
//		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
//		                             new HTTPSProperties(hostnameVerifier, ctx));
		Client client = Client.create(config);				//if we need a ssh, can go on work in this part.
		//have to add an value: /jsonArray, otherwise, the other method in controller will be called.
		WebResource webResource = client.resource("http://localhost/bigbang/useraccounts/jsonArray");
		webResource.accept("application/json");
		//webResource.method("POST");	//this method will call onto the remote server, duplicated with webResource.post();
		
		//then call the post method to get a response object : 
		ClientResponse response = webResource.post(ClientResponse.class);// (the requestPOJO array will be translated into json)
		System.out.println("status:" + response.getStatus());
		System.out.println("message:" + response.getEntity(String.class));
	}
}
