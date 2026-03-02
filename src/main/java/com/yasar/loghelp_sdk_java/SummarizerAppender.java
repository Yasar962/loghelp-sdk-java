package com.yasar.loghelp_sdk_java;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

public class SummarizerAppender extends AppenderBase<ILoggingEvent> {

    private final String ingestUrl;
    private final String apiKey;
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
            String stackTrace = null;
            if (event.getThrowableProxy() != null) {
                stackTrace = ch.qos.logback.classic.spi.ThrowableProxyUtil
                        .asString(event.getThrowableProxy());
            }

            LogPayload payload = new LogPayload();
            payload.level = event.getLevel().toString();
            payload.logger = event.getLoggerName();
            payload.thread = event.getThreadName();
            payload.message = event.getFormattedMessage();
            payload.stackTrace = stackTrace;
            payload.timestamp = event.getTimeStamp();

            String json = MAPPER.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ingestUrl))
                    .header("Content-Type", "application/json")
                    .header("X-API-KEY", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());

        } catch (Exception e) {
            // NEVER throw from logger
            System.err.println("[LOGHELP SDK] Failed to send log: " + e.getMessage());
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