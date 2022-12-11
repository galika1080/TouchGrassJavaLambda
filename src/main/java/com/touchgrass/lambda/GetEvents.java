package com.touchgrass.lambda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.touchgrass.core.dao.EventDao;
import com.touchgrass.core.model.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Lambda request handler for GetEvents API.
 */
public class GetEvents implements RequestHandler<Map<String, Object>, String> {

    @Data
    @Builder
    @AllArgsConstructor
    private static class Response {
        int code;
        String message;

        List<Event> events;
    }

    Gson gson;
    static EventDao dao;

    LambdaLogger logger;

    public GetEvents() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        dao = new EventDao();
    }
    
    @Override
    public String handleRequest(Map<String, Object> event, final Context context) {
        logger = context.getLogger();

        return gson.toJson(dao.getAllEvents());
    }
}
