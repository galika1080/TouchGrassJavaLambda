package com.touchgrass.lambda;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

// gson
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.touchgrass.core.dao.ProfileDao;
import com.touchgrass.core.model.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Lambda request handler for AddTagToUser API.
 */
public class SetUserTags implements RequestHandler<Map<String, Object>, String> {

    @Data
    private static class Request {
        String email;
        List<String> tags;
    }

    @Data
    @AllArgsConstructor
    private static class Response {
        int code;
        String message;
    }

    Gson gson;
    static ProfileDao dao;

    LambdaLogger logger;

    public SetUserTags() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        dao = new ProfileDao();
    }

    @Override
    public String handleRequest(Map<String, Object> event, final Context context) {
        logger = context.getLogger();
        
        String response200 = gson.toJson(new Response(200, "OK"));
        String response400 = gson.toJson(new Response(400, "Bad request"));
        
        Request request;

        try {
            String bodyJson = String.valueOf(event.get("body"));
            request = gson.fromJson(bodyJson, Request.class);
        } catch (JsonParseException jse) {
            logger.log("Error: request body could not be parsed\n");

            return response400;
        }

        if (request.email == null || request.tags == null) {
            return response400;
        }

        dao.putProfile(
            Profile.builder()
                .email(request.getEmail())
                .tags(new HashSet<String>(request.tags)).build()
        );

        return response200;
    }
}
