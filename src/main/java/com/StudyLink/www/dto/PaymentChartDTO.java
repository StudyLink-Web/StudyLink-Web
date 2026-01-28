package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentChartDTO {
    private PaymentChartPeriodDTO day7;
    private PaymentChartPeriodDTO day30;
}
