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
    private final String apiKey; // Using x-api-key now

    // Java 21 Virtual Threads keep your main app fast
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    public SummarizerAppender(String ingestUrl, String apiKey) {
        this.ingestUrl = ingestUrl;
        this.apiKey = apiKey;
    }

    @Override
    protected void append(ILoggingEvent event) {
        // 1. Extract Stack Trace if it exists
        String stackTrace = "";
        if (event.getThrowableProxy() != null) {
            stackTrace = ch.qos.logback.classic.spi.ThrowableProxyUtil
                    .asString(event.getThrowableProxy());
        }

        // 2. Build a much richer JSON object
        // Note: In production, use a JSON library like Jackson to avoid string escaping issues!
        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"level\":\"").append(event.getLevel()).append("\",")
                .append("\"logger\":\"").append(event.getLoggerName()).append("\",")
                .append("\"thread\":\"").append(event.getThreadName()).append("\",")
                .append("\"message\":\"").append(escapeJson(event.getFormattedMessage())).append("\",")
                .append("\"stackTrace\":\"").append(escapeJson(stackTrace)).append("\",")
                .append("\"timestamp\":").append(event.getTimeStamp())
                .append("}");

        // ... existing HttpClient send logic ...
    }

    // Simple helper to prevent JSON breakage from quotes/newlines
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}