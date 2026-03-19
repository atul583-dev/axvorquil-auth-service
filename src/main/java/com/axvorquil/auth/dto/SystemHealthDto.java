package com.axvorquil.auth.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDto {
    private String               overallStatus;   // UP | DEGRADED | DOWN
    private int                  upCount;
    private int                  totalCount;
    private long                 checkedAt;
    private int                  totalUsers;
    private int                  totalOrgs;
    private List<ServiceHealthDto> services;
}
