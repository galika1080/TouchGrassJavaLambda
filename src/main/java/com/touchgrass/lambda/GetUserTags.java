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
import com.touchgrass.core.dao.ProfileDao;
import com.touchgrass.core.model.Profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Lambda request handler for AddTagToUser API.
 */
public class GetUserTags implements RequestHandler<Map<String, Object>, String> {

    @Data
    private static class Request {
        String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class Response {
        int code;
        String message;

        List<String> tags;
    }

    Gson gson;
    static ProfileDao dao;

    LambdaLogger logger;

    public GetUserTags() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        dao = new ProfileDao();
    }
    
    @Override
    public String handleRequest(Map<String, Object> event, final Context context) {
        logger = context.getLogger();
        
        String response400 = gson.toJson(Response.builder()
            .code(400)
            .message("Bad request")
            .tags(Collections.emptyList()).build()
        );
        
        Request request;

        try {
            String bodyJson = String.valueOf(event.get("body"));
            request = gson.fromJson(bodyJson, Request.class);
        } catch (JsonParseException jse) {
            logger.log("Error: request body could not be parsed\n");

            return response400;
        }

        if (request.email == null) {
            return response400;
        }

        Profile profile = dao.getProfile(request.email);
        
        if (profile == null) {
            return response400;
        }

        return gson.toJson(Response.builder()
            .code(200)
            .message("OK")
            .tags(new ArrayList<String>(profile.getTags())).build()
        );
    }
}
