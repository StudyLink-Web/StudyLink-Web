package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 챗봇 대화 메시지 내역
 */
@Entity
@Table(name = "chat_chatbot_message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "session")
public class ChatBotMessage extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatBotSession session;

    @Column(length = 20, nullable = false)
    private String role; // "USER" or "BOT"

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

}
