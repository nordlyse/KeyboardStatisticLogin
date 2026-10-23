package com.nordlyse.wordfrequency.keystrike;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeystrikeSettings.class)
class KeystrikeConfiguration {

    @Bean
    Clock keystrikeClock() {
        return Clock.systemUTC();
    }
}
