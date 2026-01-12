package com.StudyLink.www.controller;

import com.StudyLink.www.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/room/*")
@RequiredArgsConstructor
@Slf4j
@Controller
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/list")
    public void list(){}

    @GetMapping("/room")
    public void room(){}
}
