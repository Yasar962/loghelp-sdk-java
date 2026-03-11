package com.yasar.loghelp_sdk_java;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "loghelp_metric_start";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        request.setAttribute(START_TIME, System.nanoTime());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        try {

            Long startTime = (Long) request.getAttribute(START_TIME);
            if (startTime == null) return;

            long duration = (System.nanoTime() - startTime) / 1_000_000;

            String pattern = (String) request.getAttribute(
                    HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

            String finalPath = (pattern != null)
                    ? pattern
                    : request.getRequestURI();

            if (finalPath.startsWith("/error") ||
                    finalPath.startsWith("/actuator") ||
                    finalPath.startsWith("/favicon")) {
                return;
            }

            String traceId = (String) request.getAttribute(TraceIdFilter.TRACE_ATTR);

            if (traceId == null) {
                traceId = MDC.get("traceId");
            }

            MetricPayload metric = new MetricPayload(
                    finalPath,
                    request.getMethod(),
                    response.getStatus(),
                    duration,
                    traceId
            );
            System.out.println("LOGHELP METRIC SENT: " + finalPath);
            MetricSender.send(metric);
        } catch (Exception ignored) {
        }
    }
}