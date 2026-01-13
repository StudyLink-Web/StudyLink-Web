package com.StudyLink.www.controller;

import com.StudyLink.www.dto.MapDataDTO;
import com.StudyLink.www.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping
    public String showMap() {
        return "map/map";
    }

    @PostMapping("/api/data")
    @ResponseBody
    public MapDataDTO.Response getMapData(@RequestBody MapDataDTO.Request request) {
        return mapService.getMapData(request);
    }
}
