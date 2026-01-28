package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminExchangeRequestRejectDTO {
    private long id;
    private String reason;
}
