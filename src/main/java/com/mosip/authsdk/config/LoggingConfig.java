package com.mosip.authsdk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

import java.nio.file.Paths;

@Configuration
public class LoggingConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Bean
    public void configureLogging(MosipAuthProperties config) {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();

            // Set log file path
            String logFilePath = config.getLogging().getLogFilePath();
            System.setProperty("LOG_FILE", Paths.get(logFilePath).toString());

            // Set log level
            Level logLevel = Level.toLevel(config.getLogging().getLogLevel(), Level.DEBUG);
            context.getLogger("com.mosip").setLevel(logLevel);

            configurator.doConfigure(getClass().getResourceAsStream("/logback.xml"));
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        } catch (Exception e) {
            logger.error("Error configuring logging", e);
        }
    }
}
