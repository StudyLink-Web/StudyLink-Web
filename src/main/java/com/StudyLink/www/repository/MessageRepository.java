package com.StudyLink.www.repository;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<MessageDTO> findByRoomId(long roomId);

    void deleteByRoomId(long roomId);

    // roomId + mentorId로 삭제
    @Transactional
    @Modifying
    @Query("DELETE FROM Message m WHERE m.roomId = :roomId AND m.senderId = :mentorId")
    void deleteByRoomIdAndMentorId(long roomId, long mentorId);
}
