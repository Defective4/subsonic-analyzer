package io.github.defective4.audioanalyzer.app.proxy;

import java.util.Map;

import com.google.gson.JsonObject;

public interface ResponseModifier {
    void modify(Map<String, String> params, JsonObject obj) throws Exception;
}
