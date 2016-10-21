package com.aeiou.bigbang.services.synchronization;

import java.util.ArrayList;
import java.util.List;

import com.aeiou.bigbang.domain.UserAccount;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import flexjson.JSONDeserializer;

public class ClientSyncTool {

    SynchnizationManager synchnizationManager = new SynchnizationManager();

    public int startToSynch(
            UserAccount tCurUser,
            String pCommand) {
        ClientConfig config = new DefaultClientConfig();
        // SSLContext ctx = SSLContext.getInstance("SSL");
        // ctx.init(null, myTrustManager, null);
        // config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hostnameVerifier,
        // ctx));
        Client client = Client.create(config); // if we need a ssh, can go on work in this part.

        // WebResource webResource = client.resource("http://localhost/bigbang/useraccounts/" + pCommand); //have to add
        // an value: /jsonArray, otherwise, the
        WebResource webResource = client.resource("http://cn.sharethegoodones.com/useraccounts/" + pCommand); // have to
                                                                                                              // add an
                                                                                                              // value:
                                                                                                              // /{pCommand},
                                                                                                              // to
                                                                                                              // distinguish
        webResource.accept("application/json"); // other methods in controller.
        // webResource.method("POST"); //this method will call onto the remote server, duplicated with
        // webResource.post();

        ClientResponse response =
                webResource.type("application/json").post(ClientResponse.class,
                        synchnizationManager.getRecentlyAddedContent(tCurUser.getName(), pCommand));
        // ClientResponse response = webResource.post(ClientResponse.class);
        if (200 == response.getStatus()) {
            List<String> tList =
                    new JSONDeserializer<List<String>>().use(null, ArrayList.class).use("values", String.class)
                            .deserialize((String) response.getEntity(String.class));
            return synchnizationManager.saveContentIntoLocalDB(tList, pCommand);
        }
        return -1;
    }

}
