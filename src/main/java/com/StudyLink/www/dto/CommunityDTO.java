package com.StudyLink.www.dto;

import lombok.*;
import java.time.LocalDateTime;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityDTO {

    private Long userId;        // user_id
    private String email;
    private String name;
    private String nickname;
    private String role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long pagenum;
    private Long bno;
}
