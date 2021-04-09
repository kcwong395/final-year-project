package com.fyp.shoemaker.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestUtil {

    public static String initiateRequest(String method, String strUrl, String requestBody) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = null;

        switch(method) {
            case "GET":
                request = HttpRequest.newBuilder().uri(URI.create(strUrl)).build();
                break;
            case "POST":
                request = HttpRequest.newBuilder()
                        .uri(URI.create(strUrl))
                        .setHeader("Content-type", "application/json;charset=utf-8")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
                break;
            default:
                System.out.println("Unexpected Method: " + method);
        }

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

}
