package com.autumnin.minio.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Guoyang
 * @description MinIO配置
 * @date 2022/11/5
 */
@Configuration
@EnableConfigurationProperties(MinIOProperties.class)
public class MinIOConfig {
    private final MinIOProperties properties;

    public MinIOConfig(MinIOProperties properties) {
        this.properties = properties;
    }


    /**
     * 注入 MinIOClient
     *
     * @return MinIOClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

}
