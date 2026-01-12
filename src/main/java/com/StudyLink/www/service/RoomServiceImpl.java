package com.StudyLink.www.service;

import com.StudyLink.www.repository.MessageRepository;
import com.StudyLink.www.repository.RoomRepository;
import com.StudyLink.www.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService{
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final SubjectRepository subjectRepository;


}
