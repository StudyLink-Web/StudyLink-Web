package com.StudyLink.www.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExchangeChartDTO {
    private ExchangeChartPeriodDTO day7;
    private ExchangeChartPeriodDTO day30;
}
