package com.yasar.loghelp_sdk_java;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    private static final String TRACE_ID_REQUEST_ATTR = "LOGHELP_TRACE_ID";

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false; // ensure filter runs on ERROR dispatch
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId;

        // If already generated earlier in this request lifecycle
        Object existing = request.getAttribute(TRACE_ID_REQUEST_ATTR);

        if (existing != null) {
            traceId = existing.toString();
        } else {

            // Try incoming header first (for microservice propagation)
            traceId = request.getHeader("X-Trace-Id");

            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }

            request.setAttribute(TRACE_ID_REQUEST_ATTR, traceId);
        }

        MDC.put(TRACE_ID, traceId);

        try {

            if (DispatcherType.REQUEST.equals(request.getDispatcherType())) {
                response.setHeader("X-Trace-Id", traceId);
            }

            System.out.println("TRACE ACTIVE: " + traceId + " | " + request.getRequestURI());

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}