package com.touchgrass.lambda;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.touchgrass.core.dao.EventDao;
import com.touchgrass.core.dao.ProfileDao;
import com.touchgrass.core.model.Event;
import com.touchgrass.core.model.Profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.core.sync.RequestBody;

/**
 * Lambda request handler for GetEvents API.
 */
public class GetSuggestedEvents implements RequestHandler<Map<String, Object>, String> {

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

        List<Event> events;
    }

    Gson gson;
    static EventDao events;
    static ProfileDao profiles;
    static AmazonS3 s3;

    LambdaLogger logger;

    public GetSuggestedEvents() {
        gson = new GsonBuilder().create();

        events = new EventDao();
        profiles = new ProfileDao();
        
        s3 = AmazonS3ClientBuilder.standard().build();
    }

    private void uploadEventsToS3(String json) {
        InputStream targetStream = new ByteArrayInputStream(json.getBytes());

        PutObjectRequest objectRequest = new PutObjectRequest("jsonbucket12345", "example-events.json", targetStream, new ObjectMetadata());

        s3.putObject(objectRequest);
    }

    private double doApiCall(String tags, String description) {
        String requestURL = "https://api.twinword.com/api/text/similarity/latest/";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest twRequest = HttpRequest.newBuilder()
            .uri(URI.create(requestURL))
            .header("content-type", "application/x-www-form-urlencoded")
            .header("X-Twaip-Key", "tLeItAJfjbO5egq4Ol4RWkVxMSS7FFmfzertzOkctAm6YcEUzUX/OQMOQ4kf1J5LvyE3MayZJ+gRcghlfenamQ==")
            .POST(HttpRequest.BodyPublishers.ofString("text1=" + URLEncoder.encode(tags, StandardCharsets.UTF_8)
                                                    + "&text2=" + URLEncoder.encode(description, StandardCharsets.UTF_8)))
            .build();
        
        HttpResponse response;
        try {
            response = client.send(twRequest, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.log(e.toString());
            return 0.0;
        }

        if (response.statusCode() != 200) {
            logger.log("Response code" + response.statusCode());
            return 0.0;
        }

        JSONObject bodyJson;

        try {
            bodyJson = (JSONObject) new JSONParser().parse((String) response.body());
        } catch (org.json.simple.parser.ParseException pe) {
            logger.log("Error parsing NLP API response: " + (String) response.body());
            return 0.0;
        }

        return (double) bodyJson.get("similarity");
    }
    
    @Override
    public String handleRequest(Map<String, Object> event, final Context context) {
        logger = context.getLogger();

        Request request;

        String response400 = gson.toJson(Response.builder()
            .code(400)
            .message("Bad request")
            .events(Collections.emptyList()).build()
        );

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

        Profile profile = profiles.getProfile(request.email);
        
        if (profile == null) {
            return response400;
        }

        String allTags = String.join(", ", profile.getTags());

        List<Event> eventList = new ArrayList<>(events.getAllEvents());

        for (Event e : eventList) {
            String query = e.getName() + ". " + e.getDescription();

            e.setRelevanceScore( (float) doApiCall(allTags, query) );
        }

        eventList.sort(Comparator.comparing(v -> v.getRelevanceScore()));
        
        String json = gson.toJson(eventList);
        uploadEventsToS3(json);

        return json;
    }
}
