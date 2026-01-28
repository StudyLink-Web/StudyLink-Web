package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserChartDTO {
    private UserChartPeriodDTO day7;
    private UserChartPeriodDTO day30;
}