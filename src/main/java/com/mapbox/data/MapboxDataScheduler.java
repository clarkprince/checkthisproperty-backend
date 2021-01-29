package com.mapbox.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MapboxDataScheduler {

    Logger logger = LoggerFactory.getLogger(MapboxDataScheduler.class);

    @Autowired
    private MapboxDataManager mapboxDataManager;

    @Scheduled(cron = "${files.cron.expression}")
    public void run() {
        logger.info("Start Data Processing");
        try {
            mapboxDataManager.processFile();
        } catch (IOException e) {
            logger.error("Error Occurred while processing {}", e);
        }
    }
}
