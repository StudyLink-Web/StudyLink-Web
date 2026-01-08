package com.StudyLink.www.repository;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<MessageDTO> findByRoomId(long roomId);
}
