package com.StudyLink.www.dto;

import com.StudyLink.www.entity.Message;
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
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private LocalDateTime createdAt;
    private MessageType messageType;
    private String content;
    private String fileUuid;
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
