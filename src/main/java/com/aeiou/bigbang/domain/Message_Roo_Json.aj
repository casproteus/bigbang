// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Message;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

privileged aspect Message_Roo_Json {
    
    public String Message.toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static Message Message.fromJsonToMessage(String json) {
        return new JSONDeserializer<Message>().use(null, Message.class).deserialize(json);
    }
    
    public static String Message.toJsonArray(Collection<Message> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<Message> Message.fromJsonArrayToMessages(String json) {
        return new JSONDeserializer<List<Message>>().use(null, ArrayList.class).use("values", Message.class).deserialize(json);
    }
    
}
