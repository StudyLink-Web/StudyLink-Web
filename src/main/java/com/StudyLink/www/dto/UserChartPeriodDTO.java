package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserChartPeriodDTO {
    private List<String> labels;
    private List<Integer> daily;
    private List<Integer> cumulative;
}
