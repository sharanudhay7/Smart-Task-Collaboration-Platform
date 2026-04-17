package com.smarttask.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.pagination")
@Data
public class PaginationConfig {
    private int defaultPage;
    private int defaultSize;
    private String defaultSort;
    private String defaultDirection;
}