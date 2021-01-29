package com.mapbox.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MapboxDataManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapboxDataManageApplication.class, args);
    }

}
