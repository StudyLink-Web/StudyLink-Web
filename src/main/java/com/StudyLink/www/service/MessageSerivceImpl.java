package com.StudyLink.www.service;

import com.StudyLink.www.dto.MessageDTO;
import com.StudyLink.www.entity.Message;
import com.StudyLink.www.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageSerivceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Override
    public void insert(MessageDTO message) {
        if (message.getMessageType() == MessageDTO.MessageType.TEXT) {
            messageRepository.save(new Message(message));
        } else if (message.getMessageType() == MessageDTO.MessageType.IMAGE) {

        }
    }
}
