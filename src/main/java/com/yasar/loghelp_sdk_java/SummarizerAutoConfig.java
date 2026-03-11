package com.yasar.loghelp_sdk_java;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
public class SummarizerAutoConfig {

    @Value("${loghelp.summarizer.url}")
    private String ingestUrl;

    @Value("${loghelp.summarizer.api-key}") // 1. Add this property
    private String apiKey;

    @PostConstruct
    public void forceAttachAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        SummarizerAppender appender = new SummarizerAppender(ingestUrl, apiKey);
        appender.setContext(context);
        appender.setName("LOGHELP_SUMMARIZER");
        appender.start();

        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);

        LoggerFactory.getLogger(SummarizerAutoConfig.class)
                .info("LogHelp SDK initialized");

        MetricSender.init(ingestUrl, apiKey);

        System.out.println(ingestUrl);
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }

    @Bean
    public WebMvcConfigurer loghelpWebMvcConfigurer(PerformanceInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}