package com.axvorquil.auth.service;

import com.axvorquil.auth.dto.ServiceHealthDto;
import com.axvorquil.auth.dto.SystemHealthDto;
import com.axvorquil.auth.repository.OrganizationRepository;
import com.axvorquil.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import jakarta.annotation.PreDestroy;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAggregatorService {

    private final RestTemplate         restTemplate;
    private final UserRepository       userRepository;
    private final OrganizationRepository orgRepository;

    @Value("${services.auth.url:http://localhost:8081}")
    private String authUrl;
    @Value("${services.security.url:http://localhost:8085}")
    private String securityUrl;
    @Value("${services.clinic.url:http://localhost:8082}")
    private String clinicUrl;
    @Value("${services.campaign.url:http://localhost:8083}")
    private String campaignUrl;
    @Value("${services.stock.url:http://localhost:8084}")
    private String stockUrl;
    @Value("${services.legal.url:http://localhost:8086}")
    private String legalUrl;
    @Value("${services.matchmaking.url:http://localhost:8087}")
    private String matchmakingUrl;
    @Value("${services.health.url:http://localhost:8088}")
    private String healthUrl;
    @Value("${services.vidya.url:http://localhost:8089}")
    private String vidyaUrl;

    // Dedicated pool — guarantees true parallelism regardless of ForkJoinPool size on small VMs
    private final ExecutorService healthPool = Executors.newFixedThreadPool(12);

    @PreDestroy
    public void shutdown() { healthPool.shutdownNow(); }

    private record ServiceDef(String name, String url) {}

    @Cacheable("systemHealth")
    public SystemHealthDto aggregate() {
        List<ServiceDef> services = List.of(
            new ServiceDef("Auth Service",     authUrl     + "/actuator/health"),
            new ServiceDef("Security Service", securityUrl + "/actuator/health"),
            new ServiceDef("Clinic Service",   clinicUrl   + "/actuator/health"),
            new ServiceDef("Campaign Service", campaignUrl + "/actuator/health"),
            new ServiceDef("Stock Service",    stockUrl    + "/actuator/health"),
            new ServiceDef("Legal AI Service",    legalUrl       + "/actuator/health"),
            new ServiceDef("Matchmaking Service", matchmakingUrl + "/actuator/health"),
            new ServiceDef("ArogyaAI Health Service", healthUrl  + "/actuator/health"),
            new ServiceDef("VidyaAI Exam Prep Service", vidyaUrl + "/actuator/health")
        );

        // Run all health checks in parallel — each has a 4s timeout so total ≤ ~5s
        List<CompletableFuture<ServiceHealthDto>> futures = services.stream()
                .map(svc -> CompletableFuture.supplyAsync(() -> checkService(svc.name(), svc.url()), healthPool))
                .collect(Collectors.toList());

        List<ServiceHealthDto> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            ServiceDef svc = services.get(i);
            try {
                results.add(futures.get(i).get(6, TimeUnit.SECONDS));
            } catch (Exception e) {
                results.add(ServiceHealthDto.builder()
                        .name(svc.name()).url(svc.url())
                        .status("DOWN").responseMs(5000L).details("Timeout").build());
            }
        }

        int upCount = (int) results.stream().filter(d -> "UP".equals(d.getStatus())).count();

        String overall = upCount == services.size() ? "UP"
                       : upCount == 0               ? "DOWN"
                       :                              "DEGRADED";

        return SystemHealthDto.builder()
                .overallStatus(overall)
                .upCount(upCount)
                .totalCount(services.size())
                .checkedAt(System.currentTimeMillis())
                .totalUsers((int) userRepository.count())
                .totalOrgs((int)  orgRepository.count())
                .services(results)
                .build();
    }

    private ServiceHealthDto checkService(String name, String url) {
        long start = System.currentTimeMillis();
        try {
            String resp = restTemplate.getForObject(url, String.class);
            long ms = System.currentTimeMillis() - start;
            boolean up = resp != null && resp.contains("\"status\":\"UP\"");
            return ServiceHealthDto.builder()
                    .name(name)
                    .url(url)
                    .status(up ? "UP" : "DEGRADED")
                    .responseMs(ms)
                    .details(up ? "Healthy" : "Unhealthy response")
                    .build();
        } catch (Exception e) {
            long ms = System.currentTimeMillis() - start;
            return ServiceHealthDto.builder()
                    .name(name)
                    .url(url)
                    .status("DOWN")
                    .responseMs(ms)
                    .details(e.getMessage())
                    .build();
        }
    }
}
