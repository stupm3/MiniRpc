package com.stupm.core.config;

import lombok.Data;

@Data
public class RegistryConfig {
    private String registry ="zookeeper";

    private String address = "127.0.0.1:2181";

    private String username;

    private String password;

    private Long timeOut = 10000L;

}
