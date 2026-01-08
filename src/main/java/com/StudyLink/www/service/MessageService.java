package com.StudyLink.www.service;


import com.StudyLink.www.dto.MessageDTO;

import java.util.List;

public interface MessageService {

    void insert(MessageDTO message);

    List<MessageDTO> loadMessage(long roomId);
}
