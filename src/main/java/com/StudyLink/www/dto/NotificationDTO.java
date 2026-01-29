package com.StudyLink.www.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String type;
    private String message;

    @JsonProperty("isRead")
    private boolean isRead;

    private Long relatedId;
    private LocalDateTime createdAt;

    // Jackson이 boolean 필드의 'is' 접두사를 멋대로 처리하지 않도록 명시적 지정
    @JsonProperty("isRead")
    public boolean getIsRead() {
        return isRead;
    }

    @JsonProperty("isRead")
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
