package com.yasar.loghelp_sdk_java;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

public class SummarizerAppender extends AppenderBase<ILoggingEvent> {

    private final String ingestUrl;
    private final String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    public SummarizerAppender(String ingestUrl, String apiKey) {
        this.ingestUrl = ingestUrl;
        this.apiKey = apiKey;
    }

    @Override
    protected void append(ILoggingEvent event) {

        try {
            String stackTrace = "";
            if (event.getThrowableProxy() != null) {
                stackTrace = ch.qos.logback.classic.spi.ThrowableProxyUtil
                        .asString(event.getThrowableProxy());
            }

            String json = "{"
                    + "\"level\":\"" + event.getLevel() + "\","
                    + "\"logger\":\"" + event.getLoggerName() + "\","
                    + "\"thread\":\"" + event.getThreadName() + "\","
                    + "\"message\":\"" + escapeJson(event.getFormattedMessage()) + "\","
                    + "\"stackTrace\":\"" + escapeJson(stackTrace) + "\","
                    + "\"timestamp\":" + event.getTimeStamp()
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ingestUrl))
                    .header("Content-Type", "application/json")
                    .header("X-API-KEY", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // 🔥 THIS WAS MISSING
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        System.err.println("[LOGHELP SDK] Failed to send log: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            // Logging must NEVER break the app
            System.err.println("[LOGHELP SDK] Error: " + e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}