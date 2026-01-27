package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminExchangeRequestDTO {
    private ExchangeRequestDTO exchangeRequestDTO;
    private UsersDTO usersDTO;
}
