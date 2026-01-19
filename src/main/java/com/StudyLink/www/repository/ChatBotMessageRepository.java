package com.StudyLink.www.repository;

import com.StudyLink.www.entity.ChatBotMessage;
import com.StudyLink.www.entity.ChatBotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatBotMessageRepository extends JpaRepository<ChatBotMessage, Long> {
    // 세션별 모든 메시지를 시간순으로 조회
    List<ChatBotMessage> findBySessionOrderByCreatedAtAsc(ChatBotSession session);
    
    // 세션 삭제 시 관련 메시지를 한 번에 삭제하기 위한 메서드 (필요시)
    void deleteBySession(ChatBotSession session);
}
