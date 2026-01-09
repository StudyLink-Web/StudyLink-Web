package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    @JsonProperty("messageId")
    private Long messageId;
    @JsonProperty("roomId")
    private Long roomId;
    @JsonProperty("senderId")
    private Long senderId;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    @JsonProperty("messageType")
    private MessageType messageType;
    @JsonProperty("content")
    private String content;
    @JsonProperty("fileUuid")
    private String fileUuid;
    @JsonProperty("isRead")
    private Boolean isRead;

    public enum MessageType {
        TEXT,
        FILE,
        IMAGE
    }

    public MessageDTO(Message message) {
        this.messageId = message.getMessageId();
        this.roomId = message.getRoomId();
        this.senderId = message.getSenderId();
        this.createdAt = message.getCreatedAt();
        this.messageType = MessageType.valueOf(message.getMessageType().name()); // Enum 변환
        this.content = message.getContent();
        this.fileUuid = message.getFileUuid();
        this.isRead = message.getIsRead();
    }
}
