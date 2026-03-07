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
    private static final String TRACE_ATTR = "LOGHELP_TRACE_ID";

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Check if we already have it in attributes (persists across dispatches)
        String traceId = (String) request.getAttribute(TRACE_ATTR);

        if (traceId == null) {
            // 2. Fallback to Header
            traceId = request.getHeader("X-Trace-Id");

            // 3. Generate new if both are missing
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }

            // 4. CRITICAL: Store it in the request object so it survives
            // the transition from /test/throw-error to /error
            request.setAttribute(TRACE_ATTR, traceId);
        }

        // Always put it in MDC for the current thread
        MDC.put(TRACE_ID, traceId);

        try {
            if (DispatcherType.REQUEST.equals(request.getDispatcherType())) {
                response.setHeader("X-Trace-Id", traceId);
            }
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            // This catch block is great for your analyzer!
            // It logs BEFORE the finally block clears the MDC.
            org.slf4j.LoggerFactory.getLogger("LOGHELP_ERROR_CAPTURE")
                    .error("LOGHELP_EXCEPTION_CAUGHT: [Method: {}] [URI: {}] - Message: {}",
                            request.getMethod(),
                            request.getRequestURI(),
                            ex.getMessage(),
                            ex);
            throw ex;
        } finally {
            // Clear it so the thread is clean for the next user
            MDC.remove(TRACE_ID);
        }
    }
}