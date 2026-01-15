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
    public void list(Authentication authentication, Model model){
        List<RoomDTO> privateRoomList = new ArrayList<>();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            long mentorId = userService.findUserIdByUsername(username);
            privateRoomList = roomService.getPrivateRoomList(mentorId);
        }
        // 멘토 개인 방
        log.info(">>> privateRoomList {}", privateRoomList);
        model.addAttribute("privateRoomList", privateRoomList);


        // 공개 방
        List<RoomDTO> roomList = roomService.getRoomList();
        log.info(">>> roomList {}", roomList);
        model.addAttribute("roomList", roomList);
    }

    // list에서 room으로 입장하는 경우(학생이 첫 생성시)
    @PostMapping("/enterRoom")
    public String room(@RequestParam String username, Model model){
        log.info(">>> username {}", username);

        // username -> studentId
        long studentId = userService.findUserIdByUsername(username);
        log.info(">>> studentId {}", studentId);
        model.addAttribute("senderId", studentId);

        // 방 생성
        RoomDTO roomDTO = roomService.createRoom(studentId);
        log.info(">>> roomDTO {}", roomDTO);
        model.addAttribute("roomId", roomDTO.getRoomId());



        // 필요한 데이터 보내기
        // 과목
        List<SubjectDTO> subjectList = roomService.getSubjectDTOList();
        model.addAttribute("subjectList", subjectList);
        log.info(">>> subjectList {}", subjectList);



        // 찜 멘토
        List<FavoriteDTO> favoriteList = favoriteService.getFavoritesByStudent(studentId).stream().map(FavoriteDTO::new).toList();
        model.addAttribute("favoriteList", favoriteList);
        log.info(">>> favoriteList {}", favoriteList);

        // 학생 보유 point
//        StudentProfile studentProfile = studentProfileService.getStudentProfile(studentId)
//                .orElseThrow(() -> new IllegalArgumentException("학생 프로필을 찾을 수 없습니다."));
//        int point = studentProfile.getBonusPoint() + studentProfile.getChargedPoint();
        model.addAttribute("point", 1500);
        //log.info(">>> point {}", point);

        return "/room/room";
    }

    // room에서 등록후 list로 돌아가는 경우
    @PostMapping("/register")
    public String room(long roomId, int subjectId, int point, @RequestParam(required = false) Long mentorId){
        log.info(">>> subjectId {}", subjectId);
        log.info(">>> mentorId {}", mentorId);
        log.info(">>> point {}", point);

        // room 정보 갱신하기
        // subjectId 넣기
        // mentorId 넣기 null이면 공개
        // point 넣기 + 학생 계좌에서 point 빼기
        roomService.update(roomId, subjectId, mentorId, point);
        return "redirect:/room/list";
    }
}
