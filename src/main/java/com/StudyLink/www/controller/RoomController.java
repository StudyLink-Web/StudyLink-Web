package com.StudyLink.www.controller;

import com.StudyLink.www.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/room/*")
@RequiredArgsConstructor
@Slf4j
@Controller
public class RoomController {
    private final RoomService roomService;

}
