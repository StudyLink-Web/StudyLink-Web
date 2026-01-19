package com.StudyLink.www.controller;

import com.StudyLink.www.dto.FavoriteDTO;
import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.service.FavoriteService;
import com.StudyLink.www.service.RoomService;
import com.StudyLink.www.service.StudentProfileService;
import com.StudyLink.www.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/room/*")
@RequiredArgsConstructor
@Slf4j
@Controller
public class RoomController {
    private final RoomService roomService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final StudentProfileService studentProfileService;

    @GetMapping("/list")
    public void list(Authentication authentication, Model model) {
        List<RoomDTO> privateRoomList = new ArrayList<>();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String identifier = authentication.getName();
            long mentorId = userService.findUserIdByIdentifier(identifier);
            privateRoomList = roomService.getPrivateRoomList(mentorId);
        }

        log.info(">>> privateRoomList {}", privateRoomList);
        model.addAttribute("privateRoomList", privateRoomList);

        List<RoomDTO> roomList = roomService.getRoomList();
        log.info(">>> roomList {}", roomList);
        model.addAttribute("roomList", roomList);
    }

    @PostMapping("/enterRoom")
    public String room(@RequestParam String username, Model model) {
        log.info(">>> username {}", username);

        long studentId = userService.findUserIdByIdentifier(username);
        log.info(">>> studentId {}", studentId);
        model.addAttribute("senderId", studentId);

        RoomDTO roomDTO = roomService.createRoom(studentId);
        log.info(">>> roomDTO {}", roomDTO);
        model.addAttribute("roomId", roomDTO.getRoomId());

        List<SubjectDTO> subjectList = roomService.getSubjectDTOList();
        model.addAttribute("subjectList", subjectList);
        log.info(">>> subjectList {}", subjectList);

        List<FavoriteDTO> favoriteList = favoriteService.getFavoritesByStudent(studentId)
                .stream()
                .map(FavoriteDTO::new)
                .toList();
        model.addAttribute("favoriteList", favoriteList);
        log.info(">>> favoriteList {}", favoriteList);

        model.addAttribute("point", 1500);

        return "/room/room";
    }

    @PostMapping("/register")
    public String room(long roomId, int subjectId, int point, @RequestParam(required = false) Long mentorId) {
        log.info(">>> subjectId {}", subjectId);
        log.info(">>> mentorId {}", mentorId);
        log.info(">>> point {}", point);

        roomService.update(roomId, subjectId, mentorId, point);
        return "redirect:/room/list";
    }
}
