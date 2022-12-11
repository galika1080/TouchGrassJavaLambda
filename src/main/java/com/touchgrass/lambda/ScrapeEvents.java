package com.touchgrass.lambda;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

// gson
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.touchgrass.core.dao.EventDao;
import com.touchgrass.core.model.Event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Lambda request handler for AddTagToUser API.
 */
public class ScrapeEvents implements RequestHandler<Map<String, Object>, String> {

    @Data
    private static class Request {
        String query;
    }

    @Data
    @AllArgsConstructor
    private static class Response {
        int code;
        String message;
    }

    Gson gson;
    static EventDao dao;

    LambdaLogger logger;

    public ScrapeEvents() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        dao = new EventDao();
    }

    @Override
    public String handleRequest(Map<String, Object> lambdaEvent, final Context context) {
        logger = context.getLogger();
        
        String response200 = gson.toJson(new Response(200, "OK"));
        String response400 = gson.toJson(new Response(400, "Bad request"));

        String requestURL = "https://serpapi.com/search?engine=google_events&q=champaign&api_key=3b56612ab24e7dbf6e69ff5c8b227a51e8cae032db66b4400c92429b37505203";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest serpRequest = HttpRequest.newBuilder()
            .uri(URI.create(requestURL))
            .GET().build();
        
        HttpResponse response;
        try {
            response = client.send(serpRequest, BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        if (response.statusCode() != 200) {
            System.out.println("Response code" + response.statusCode());
            return response400;
        }

        JSONObject bodyJson;
        try {
            bodyJson = (JSONObject) new JSONParser().parse((String) response.body());
        } catch (org.json.simple.parser.ParseException pe) {
            System.out.println(pe);
            return null;
        }

        JSONArray eventResults = (JSONArray) bodyJson.get("events_results");
        
        List<Event> parsedEvents = new ArrayList<Event>();
        for (Object o : eventResults) {
            JSONObject event = (JSONObject) o;

            String title = (String) event.get("title");
            String desc = (String) event.get("description");

            String link = (String) event.get("link");
            
            String address = "";
            JSONArray addressArr = (JSONArray) event.get("address");
            for (Object o1 : addressArr) {
                address += (String) o1 + ", ";
            }

            JSONObject dateObj = (JSONObject) event.get("date");
            String startDate = (String) dateObj.get("start_date");
            String when = (String) dateObj.get("when");

            parsedEvents.add(Event.builder()
                .name(title)
                .date(startDate)
                .description(desc)
                .url(link)
                .location(address).build());
        }

        for (Event e : parsedEvents) {
            dao.putEvent(e);
        }

        return response200;
    }
}
