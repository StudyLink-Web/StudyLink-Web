package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaymentChartPeriodDTO {
    private List<String> labels;
    private List<Long> dailyAmount;
    private List<Long> cumulativeAmount;
}