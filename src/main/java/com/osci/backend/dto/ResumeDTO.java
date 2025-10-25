package com.osci.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String position;
    private String summary;
    private Integer yearsOfExperience;
    private String skills;
    private String requestIp;
}