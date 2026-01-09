package com.StudyLink.www.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 챗봇 대화 세션 (채팅방)
 */
@Entity
@Table(name = "chat_chatbot_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class ChatBotSession extends TimeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(length = 255)
    private String title;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatBotMessage> messages;
}
