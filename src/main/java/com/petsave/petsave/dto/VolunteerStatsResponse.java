package com.petsave.petsave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerStatsResponse {

    private long totalVolunteers;

    private long totalHoursLogged;

    private long totalTasksCompleted;

    private Map<String, Long> volunteersByBadgeLevel;
}
