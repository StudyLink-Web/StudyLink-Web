package com.StudyLink.www.service;


import com.StudyLink.www.dto.MessageDTO;

import java.util.List;

public interface MessageService {

    MessageDTO insert(MessageDTO message);

    List<MessageDTO> loadMessage(long roomId);

    MessageDTO readMessage(long messageId);
}
