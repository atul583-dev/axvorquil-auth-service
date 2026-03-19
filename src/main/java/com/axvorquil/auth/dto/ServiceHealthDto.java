package com.axvorquil.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDto {
    private String name;
    private String url;
    private String status;      // UP | DOWN | DEGRADED | UNKNOWN
    private long   responseMs;
    private String version;
    private String details;
}
