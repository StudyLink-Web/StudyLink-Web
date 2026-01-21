package com.StudyLink.www.controller;

import com.StudyLink.www.service.PortoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/portone")
@RequiredArgsConstructor
@Slf4j
public class PortoneController {
    private final PortoneService portoneService;

    @GetMapping("/token")
    @ResponseBody
    public String testToken() {
        return portoneService.getAccessToken();
    }
}
