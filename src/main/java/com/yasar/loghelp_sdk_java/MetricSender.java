package com.yasar.loghelp_sdk_java;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

public class MetricSender {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static HttpClient httpClient;

    private static String ingestUrl;
    private static String apiKey;

    public static void init(String url, String key) {

        ingestUrl = url;
        apiKey = key;

        httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    public static void send(MetricPayload payload) {

        try {

            String json = MAPPER.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ingestUrl))
                    .header("Content-Type", "application/json")
                    .header("X-API-KEY", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}