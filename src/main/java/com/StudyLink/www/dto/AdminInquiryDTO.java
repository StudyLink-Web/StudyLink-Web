package com.StudyLink.www.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminInquiryDTO {
    private InquiryDTO inquiryDTO;
    private UsersDTO usersDTO;
}
