package com.StudyLink.www.repository;

import com.StudyLink.www.entity.ChatBotSession;
import com.StudyLink.www.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatBotSessionRepository extends JpaRepository<ChatBotSession, Long> {
    // 사용자별 세션 목록을 최신순으로 조회
    List<ChatBotSession> findByUserOrderByUpdatedAtDesc(Users user);
}
