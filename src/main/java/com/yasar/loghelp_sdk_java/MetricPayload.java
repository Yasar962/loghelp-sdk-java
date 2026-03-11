package com.yasar.loghelp_sdk_java;

import com.fasterxml.jackson.annotation.JsonProperty;


public class MetricPayload {

    public String endpoint;

    public String method;
    public int statusCode;

    public long durationMs;

    public String traceId;

    public long timestamp;

    public MetricPayload() {}

    public MetricPayload(String endpoint, String method, int statusCode, long durationMs, String traceId) {
        this.endpoint = endpoint;
        this.method = method;
        this.statusCode = statusCode;
        this.durationMs = durationMs;
        this.traceId = traceId;
        this.timestamp = System.currentTimeMillis();
    }
}