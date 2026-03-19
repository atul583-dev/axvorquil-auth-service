package com.axvorquil.auth.dto;

import lombok.Data;

import java.util.Map;

@Data
public class OnboardingStepRequest {
    private int step;
    private Map<String, Object> data;
}
