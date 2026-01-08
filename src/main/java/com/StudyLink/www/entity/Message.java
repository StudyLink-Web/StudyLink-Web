package com.StudyLink.www.entity;

import com.StudyLink.www.dto.MessageDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "messages")
@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long senderId;

    @Column
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Lob // mysql 기준 TEXT 자료형으로 저장
    @Column
    private String content;

    @Column(length = 100)
    private String fileUuid;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isRead;

    public enum MessageType {
        TEXT,
        FILE,
        IMAGE
    }

    public Message(MessageDTO messageDTO) {
        this.messageId = messageDTO.getMessageId();
        this.roomId = messageDTO.getRoomId();
        this.senderId = messageDTO.getSenderId();
        this.createdAt = messageDTO.getCreatedAt();
        this.messageType = MessageType.valueOf(messageDTO.getMessageType().name());
        this.content = messageDTO.getContent();
        this.fileUuid = messageDTO.getFileUuid();
        this.isRead = messageDTO.getIsRead();
    }
}