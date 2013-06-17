// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Twitter;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

privileged aspect Twitter_Roo_Json {
    
    public String Twitter.toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static Twitter Twitter.fromJsonToTwitter(String json) {
        return new JSONDeserializer<Twitter>().use(null, Twitter.class).deserialize(json);
    }
    
    public static String Twitter.toJsonArray(Collection<Twitter> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<Twitter> Twitter.fromJsonArrayToTwitters(String json) {
        return new JSONDeserializer<List<Twitter>>().use(null, ArrayList.class).use("values", Twitter.class).deserialize(json);
    }
    
}
