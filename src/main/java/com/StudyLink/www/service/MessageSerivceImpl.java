package com.StudyLink.www.service;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.entity.Message;
import com.StudyLink.www.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageSerivceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Override
    public MessageDTO insert(MessageDTO message) {
        Message savedMessage = null;

        if (message.getMessageType() == MessageDTO.MessageType.TEXT) {
            savedMessage = messageRepository.save(new Message(message));
        } else if (message.getMessageType() == MessageDTO.MessageType.IMAGE) {
            // 이미지 처리 로직
        }

        // 저장된 엔티티를 DTO로 변환해서 반환
        return savedMessage != null ? new MessageDTO(savedMessage) : null;
    }

    @Override
    public List<MessageDTO> loadMessage(long roomId) {
        return messageRepository.findByRoomId(roomId);
    }

    @Transactional
    @Override
    public MessageDTO readMessage(long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new EntityNotFoundException());
        message.setIsRead(true);
        messageRepository.save(message);
        return new MessageDTO(message);
    }
}
