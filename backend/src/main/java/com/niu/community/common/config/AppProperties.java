package com.niu.community.common.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private List<String> corsAllowedOrigins;
    private String uploadDir;
    private String websocketPath;
    private Integer messageDefaultDays;
    private Integer hotPostDays;
    private Integer pageSize;
    private String aiServiceUrl;
    private String esIndexName;
}
