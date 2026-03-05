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

@Configuration
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

        System.out.println(">> [LOGHELP SDK] Successfully attached SummarizerAppender to Root Logger!");
        System.out.println(apiKey);
        System.out.println(ingestUrl);
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.setOrder(1);
        return registration;
    }
}