// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.domain;

import com.aeiou.bigbang.domain.Content;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

privileged aspect Content_Roo_Json {

    public String Content.toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

    public String Content.toJson(
            String[] fields) {
        return new JSONSerializer().include(fields).exclude("*.class").serialize(this);
    }

    public static Content Content.fromJsonToContent(
            String json) {
        return new JSONDeserializer<Content>().use(null, Content.class).deserialize(json);
    }

    public static String Content.toJsonArray(
            Collection<Content> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

    public static String Content.toJsonArray(
            Collection<Content> collection,
            String[] fields) {
        return new JSONSerializer().include(fields).exclude("*.class").serialize(collection);
    }

    public static Collection<Content>Content.fromJsonArrayToContents(
            String json) {
        return new JSONDeserializer<List<Content>>().use("values", Content.class).deserialize(json);
    }

}
