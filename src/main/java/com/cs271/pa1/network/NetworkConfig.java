package com.cs271.pa1.network;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkConfig {
    @Bean
    public NetworkManager networkManager() {
        return new NetworkManager();
    }
}
