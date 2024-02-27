package com.senfan.senfanapiclientsdk;

import com.senfan.senfanapiclientsdk.client.SenfanAPIClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("senfanapi.clent")
@Data
@ComponentScan
public class SenfanApiClientConfig {
    private String accessKey;
    private String secretKey;
    private String gatewayHost;
    @Bean
    public SenfanAPIClient senfanAPIClient(){
        return new SenfanAPIClient(accessKey,secretKey,gatewayHost);
    }
}