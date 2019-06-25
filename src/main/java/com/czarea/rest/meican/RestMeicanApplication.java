package com.czarea.rest.meican;

import com.czarea.rest.meican.config.MeiCanProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zhouzx
 */
@SpringBootApplication
@EnableConfigurationProperties(MeiCanProperties.class)
@EnableScheduling
public class RestMeicanApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestMeicanApplication.class, args);
    }

}
