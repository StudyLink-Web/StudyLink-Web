package com.StudyLink.www.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/map")
@Slf4j
public class MapController {

    @GetMapping
    public String showMap() {
        log.info("📍 입시 지도 페이지 요청됨");
        return "map/map";
    }
}
