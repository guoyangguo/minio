package com.autumnin.minio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Guoyang
 * @description MinIO的配置属性
 * @date 2022/11/5
 */
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {
    private String endpoint;

    private String accessKey;

    private String secretKey;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
