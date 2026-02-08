package com.StudyLink.www.controller;

import com.StudyLink.www.dto.FavoriteDTO;
import com.StudyLink.www.dto.RoomDTO;
import com.StudyLink.www.dto.SubjectDTO;
import com.StudyLink.www.entity.Favorite;
import com.StudyLink.www.entity.Room;
import com.StudyLink.www.entity.StudentProfile;
import com.StudyLink.www.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/room/*")
@RequiredArgsConstructor
@Slf4j
@Controller
public class RoomController {
    private final RoomService roomService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final MentorProfileService mentorProfileService;
    private final DrawDataService drawDataService;
    private final NotificationService notificationService;

    @GetMapping("/list")
    public void list(@RequestParam(defaultValue = "0") int publicPage,
                     @RequestParam(defaultValue = "0") int privatePage,
                     Authentication authentication, Model model) {

        int pageGroupSize = 5; // 한 그룹에 보여줄 페이지 수

        // ================================
        // 1. 멘토 개인 방 (Private Rooms)
        // ================================
        Pageable privatePageable = PageRequest.of(privatePage, 3, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RoomDTO> privateRoomPage;

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            long mentorId = userService.findUserIdByUsername(username);
            privateRoomPage = roomService.getPrivateRoomList(mentorId, privatePageable);
        } else {
            // 익명 유저는 빈 페이지
            privateRoomPage = Page.empty(privatePageable);
        }

        List<RoomDTO> privateRoomList = privateRoomPage.getContent();
        log.info(">>> privateRoomList {}", privateRoomList);

        // 그룹 시작/끝 페이지 계산
        int privateStartPage = (privateRoomPage.getNumber() / pageGroupSize) * pageGroupSize;
        int privateEndPage = Math.min(privateStartPage + pageGroupSize, privateRoomPage.getTotalPages());

        int privatePrevGroup = Math.max(privateStartPage - pageGroupSize, 0);
        int privateNextGroup = Math.min(privateStartPage + pageGroupSize, privateRoomPage.getTotalPages());

        model.addAttribute("privateRoomPage", privateRoomPage);
        model.addAttribute("privateRoomList", privateRoomList);
        model.addAttribute("privateStartPage", privateStartPage);
        model.addAttribute("privateEndPage", privateEndPage);
        model.addAttribute("privatePrevGroup", privatePrevGroup);
        model.addAttribute("privateNextGroup", privateNextGroup);

        // ================================
        // 2. 공개 방 (Public Rooms)
        // ================================
        Pageable publicPageable = PageRequest.of(publicPage, 9, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RoomDTO> publicRoomPage = roomService.getRoomList(publicPageable);
        List<RoomDTO> roomList = publicRoomPage.getContent();
        log.info(">>> roomList {}", roomList);

        // 그룹 시작/끝 페이지 계산
        int publicStartPage = (publicRoomPage.getNumber() / pageGroupSize) * pageGroupSize;
        int publicEndPage = Math.min(publicStartPage + pageGroupSize, publicRoomPage.getTotalPages());

        int publicPrevGroup = Math.max(publicStartPage - pageGroupSize, 0);
        int publicNextGroup = Math.min(publicStartPage + pageGroupSize, publicRoomPage.getTotalPages());

        model.addAttribute("roomPage", publicRoomPage);
        model.addAttribute("roomList", roomList);
        model.addAttribute("publicStartPage", publicStartPage);
        model.addAttribute("publicEndPage", publicEndPage);
        model.addAttribute("publicPrevGroup", publicPrevGroup);
        model.addAttribute("publicNextGroup", publicNextGroup);
    }

    // room 생성
    @GetMapping("/makeRoom")
    public String makeRoom(Authentication authentication, Model model){
        String username = authentication.getName();
        log.info(">>> username {}", username);

        // username -> studentId
        long studentId = userService.findUserIdByUsername(username);
        log.info(">>> studentId {}", studentId);
        model.addAttribute("senderId", studentId);

        // 방 생성
        RoomDTO roomDTO = roomService.createRoom(studentId);
        log.info(">>> roomDTO {}", roomDTO);
        model.addAttribute("roomDTO", roomDTO);


        // 필요한 데이터 보내기
        // 과목
        List<SubjectDTO> subjectList = roomService.getSubjectDTOList();
        model.addAttribute("subjectList", subjectList);


        // 찜 멘토
        List<FavoriteDTO> favoriteList = favoriteService.getFavoritesByStudent(studentId).stream().map(FavoriteDTO::new).toList();
        model.addAttribute("favoriteList", favoriteList);
        log.info(">>> favoriteList {}", favoriteList);

        // 학생 보유 point
        StudentProfile studentProfile = studentProfileService.getStudentProfile(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 프로필을 찾을 수 없습니다."));
        int point = studentProfile.getBonusPoint() + studentProfile.getChargedPoint();
        model.addAttribute("userPoint", point);

        return "/room/room";
    }

    // room에서 등록후 list로 돌아가는 경우
    @PostMapping("/register")
    public String register(long roomId, int subjectId, int point, @RequestParam(required = false) Long mentorId,
                           RedirectAttributes redirectAttributes){
        log.info(">>> subjectId {}", subjectId);
        log.info(">>> mentorId {}", mentorId);
        log.info(">>> point {}", point);

        // room 정보 갱신하기
        // subjectId 넣기
        // mentorId 넣기 null이면 공개
        // point 넣기 + 학생 계좌에서 point 빼기
        RoomDTO roomDTO = roomService.getRoomDTO(roomId);
        roomDTO.setSubjectDTO(SubjectDTO.builder().subjectId(subjectId).build());
        roomDTO.setMentorId(mentorId);
        roomDTO.setStatus(RoomDTO.Status.PENDING);
        roomDTO.setPoint(point);
        if (mentorId != null) {
            roomDTO.setIsPublic(false);
        } else {
            roomDTO.setIsPublic(true);
        }
        log.info(">>> roomDTO {}", roomDTO);
        roomService.save(roomDTO);

        // 학생 포인트 차감
        studentProfileService.updatePoint(roomDTO.getStudentId(), -point);

        redirectAttributes.addFlashAttribute("message", "문제가 등록되었습니다.");
        return "redirect:/room/list";
    }

    // room 입장
    @GetMapping("/enterRoom")
    public String enterRoom(Authentication authentication, Long roomId, Model model){
        // roomId -> roomDTO
        if (roomId == null && model.containsAttribute("roomId")) {
            roomId = (Long) model.getAttribute("roomId");
        }
        log.info(">>> roomId {}", roomId);
        RoomDTO roomDTO = roomService.getRoomDTO(roomId);
        log.info(">>> roomDTO {}", roomDTO);
        model.addAttribute("roomDTO", roomDTO);

        // senderId
        Long senderId = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            senderId = userService.findUserIdByUsername(username);
        }
        log.info(">>> senderId {}", senderId);
        model.addAttribute("senderId", senderId);

        // 필요한 데이터 보내기
        // 과목
        List<SubjectDTO> subjectList = roomService.getSubjectDTOList();
        model.addAttribute("subjectList", subjectList);
        log.info(">>> subjectList {}", subjectList);



        // 찜 멘토
        if (senderId != null) {
            List<FavoriteDTO> favoriteList = favoriteService.getFavoritesByStudent(senderId).stream().map(FavoriteDTO::new).toList();
            model.addAttribute("favoriteList", favoriteList);
            log.info(">>> favoriteList {}", favoriteList);
        }

        if (roomDTO.getStatus() == RoomDTO.Status.IN_PROGRESS) {
            LocalDateTime startedAt = roomDTO.getInProgressedAt();
            LocalDateTime now = LocalDateTime.now();

            long elapsedSeconds = Duration.between(startedAt, now).getSeconds(); // 경과 시간(초)
            int totalSeconds = 20 * 60; // 제한시간 20분

            int timeLeft = (int)(totalSeconds - elapsedSeconds);
            if (timeLeft < 0) {
                timeLeft = 0; // 음수 방지
            }
            model.addAttribute("timeLeft", timeLeft);
        }
        return "/room/room";
    }

    @GetMapping("updateState")
    public String updateState(Authentication authentication, long roomId, RedirectAttributes redirectAttributes){
        // roomId -> roomDTO
        log.info(">>> roomId {}", roomId);
        RoomDTO roomDTO = roomService.getRoomDTO(roomId);
        log.info(">>> roomDTO {}", roomDTO);

        List<String> userRoles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        log.info(">>> userRoles {}", userRoles);

        String username = authentication.getName();
        long userId = userService.findUserIdByUsername(username);

        switch (roomDTO.getStatus()){
            case PENDING -> {
                if (userRoles.contains("ROLE_MENTOR")) {
                    // 멘토가 문제 풀이 시작버튼을 누른 상태
                    // 조건부 업데이트: PENDING 상태인 경우만 IN_PROGRESS로 변경
                    // 동시에 두 멘토가 접근시 생기는 문제 방지
                    // mentorId 입력
                    int updated = roomService.updateStatusIfPending(roomId, userId, Room.Status.IN_PROGRESS);
                    if (updated > 0) {
                        // 업데이트 성공 → 상태 변경 완료
                        redirectAttributes.addFlashAttribute("roomId", roomId);
                        redirectAttributes.addFlashAttribute("message", "문제 풀이를 시작했습니다. 제한시간은 20분 입니다.");


                        // 알림
                        notificationService.createNotification(roomDTO.getStudentId(), "ANSWER_RECEIVED", "'" + username + "' 멘토가 문제풀이를 시작했습니다.", null);

                        return "redirect:/room/enterRoom?roomId=" + roomId;
                    } else {
                        // 업데이트 실패 → 이미 다른 멘토가 시작했거나 PENDING 아님
                        redirectAttributes.addFlashAttribute("message", "다른 멘토가 이미 풀이를 시작했거나 삭제된 문제입니다.");
                        return "redirect:/room/list";
                    }
                } else if (userRoles.contains("ROLE_STUDENT")) {
                    // 학생이 등록 취소 버튼을 누른 상태
                    // 조건부 삭제 : PENDING 상태인 경우만 삭제
                    // 문제 삭제와 멘토의 풀이 시작 충돌 방지
                    int deleted = roomService.deleteIfPending(roomId);

                    if (deleted > 0) {
                        // 삭제 성공
                        // mongodb 캔버스 데이터 삭제
                        drawDataService.removeRoom(roomId);
                        // 포인트 반환하기
                        studentProfileService.updatePoint(roomDTO.getStudentId(), roomDTO.getPoint());

                        redirectAttributes.addFlashAttribute("message", "등록이 취소되었습니다.");
                        return "redirect:/room/list";
                    } else {
                        // 삭제 실패 → 이미 멘토가 풀이를 시작했거나 방이 없음
                        redirectAttributes.addFlashAttribute("message", "이미 멘토가 풀이를 시작했습니다.");
                        return "redirect:/room/list";
                    }
                }
            }
            case IN_PROGRESS -> {
                if (userRoles.contains("ROLE_MENTOR")){
                    // 멘토가 문제풀이 완료 버튼을 누른 상태
                    // 상태 업데이트 IN_PROGRESS -> ANSWERED
                    // 학생 알림 기능 (선택)
                    roomDTO.setStatus(RoomDTO.Status.ANSWERED);
                    roomService.save(roomDTO);
                    redirectAttributes.addFlashAttribute("message", "문제풀이가 완료되었습니다.");

                    // 알림
                    notificationService.createNotification(roomDTO.getStudentId(), "ANSWER_RECEIVED", "'" + username + "' 멘토가 문제풀이를 완료했습니다.", null);
                    return "redirect:/room/list";
                }
            }
        }
        return "redirect:/room/list";
    }

    @GetMapping("/exitRoom")
    public String exitRoom(Authentication authentication, long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
        // roomId -> roomDTO
        log.info(">>> roomId {}", roomId);
        RoomDTO roomDTO = roomService.getRoomDTO(roomId);
        log.info(">>> roomDTO {}", roomDTO);

        String referer = request.getHeader("Referer");
        log.info(">>> referer {}", referer);

        String redirectUrl = (referer != null) ? "redirect:" + referer : "redirect:/room/list";

        List<String> userRoles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        log.info(">>> userRoles {}", userRoles);

        String username = authentication.getName();
        switch (roomDTO.getStatus()){
            case TEMP -> {
                // 학생이 문제 등록 중 방 나가는 경우
                // 임시방 삭제
                // 메시지 삭제
                // 파일 삭제
                // 캔버스 데이터 삭제
                roomService.deleteRoom(roomId);
                drawDataService.removeRoom(roomId);
                redirectAttributes.addFlashAttribute("message", "문제 등록이 취소되었습니다.");
                return "redirect:/room/list";
            }
            case IN_PROGRESS -> {
                // 멘토가 문제풀이를 포기하는 경우
                if (roomDTO.getIsPublic()) {
                    // 방 삭제, 학생에게 알림(선택)
                    roomService.deleteRoom(roomId);
                    drawDataService.removeRoom(roomId);

                    // 알림
                    notificationService.createNotification(roomDTO.getStudentId(), "ANSWER_RECEIVED", "'" + username + "' 멘토가 문제풀이를 포기했습니다.", null);

                    // 학생 포인트 반환
                    studentProfileService.updatePoint(roomDTO.getStudentId(), roomDTO.getPoint());

                    redirectAttributes.addFlashAttribute("message", "문제풀이가 취소되었습니다.");
                    return "redirect:/room/list";
                } else {
                    // 1대1 문제 일경우
                    roomService.deleteRoom(roomId);

                    // 알림
                    notificationService.createNotification(roomDTO.getStudentId(), "ANSWER_RECEIVED", "'" + username + "' 멘토가 문제풀이를 포기했습니다.", null);

                    // 학생 포인트 반환
                    studentProfileService.updatePoint(roomDTO.getStudentId(), roomDTO.getPoint());

                    redirectAttributes.addFlashAttribute("message", "1대1 문제풀이가 취소되었습니다.");
                    return "redirect:/room/list";
                }
            }
        }
        return redirectUrl;
    }


    @GetMapping("/myQuiz")
    public String myQuiz(Authentication authentication, Model model,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String subject,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                         @RequestParam(defaultValue = "desc") String sort,
                         @PageableDefault(size = 15) Pageable pageable) {

        // 빈 문자열을 null로 처리 (optional)
        Room.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Room.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("잘못된 status 값: " + status);
            }
        }

        // 정렬 기준 만들기
        Sort sortOption = sort.equalsIgnoreCase("asc")
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        // 정렬 반영된 Pageable 생성 (pageable에서 페이지 번호, 사이즈 사용)
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        String username = authentication.getName();
        long userId = userService.findUserIdByUsername(username);

        // 조건 반영한 페이지 조회 (서비스 메서드에 인자 추가 필요)
        Page<RoomDTO> myQuizPage = roomService.getMyQuizList(userId, statusEnum, subject, startDate, endDate, sortedPageable);

        List<RoomDTO> myQuizList = myQuizPage.getContent();
        log.info(">>> userId {}", userId);
        log.info(">>> myQuizList {}", myQuizList);
        int pageGroupSize = 5;
        int startPage = (myQuizPage.getNumber() / pageGroupSize) * pageGroupSize;
        int endPage = Math.min(startPage + pageGroupSize, myQuizPage.getTotalPages());
        int prevGroup = Math.max(startPage - pageGroupSize, 0);
        int nextGroup = Math.min(startPage + pageGroupSize, myQuizPage.getTotalPages());

        model.addAttribute("myQuizPage", myQuizPage);
        model.addAttribute("myQuizList", myQuizList);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("prevGroup", prevGroup);
        model.addAttribute("nextGroup", nextGroup);

        // 검색 조건 유지
        List<SubjectDTO> subjectList = roomService.getSubjectDTOList();
        model.addAttribute("subjectList", subjectList);
        model.addAttribute("status", statusEnum);
        model.addAttribute("statuses", Room.Status.values());
        model.addAttribute("subject", subject);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);

        return "/room/myQuiz";
    }

    @PostMapping("/endRoom")
    public String endRoom(long roomId, int rating, @RequestParam(defaultValue = "false") boolean addFavoriteMentorCheckbox,
                          RedirectAttributes redirectAttributes){
        // 방 정보 갱신
        RoomDTO roomDTO = roomService.getRoomDTO(roomId);
        roomDTO.setStatus(RoomDTO.Status.COMPLETED);
        roomDTO.setRating(rating);
        roomService.save(roomDTO);

        // 멘토에게 포인트 지급
        mentorProfileService.addPoint(roomDTO.getMentorId(), roomDTO.getPoint().longValue());

        // 멘토 푼 문제수 1증가
        mentorProfileService.plusQuizCount(roomDTO.getMentorId());

        // 멘토 평점 갱신
        mentorProfileService.updateAverageRating(roomDTO.getMentorId());

        // addFavoriteMentorCheckbox true면 찜 추가
        try {
            if (addFavoriteMentorCheckbox) {
                long studentId = roomDTO.getStudentId();
                long mentorId = roomDTO.getMentorId();
                Favorite favorite = favoriteService.addFavorite(studentId, mentorId);
            }
            redirectAttributes.addFlashAttribute("message", "문제풀이가 종료되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "멘토 찜하기에 실패했습니다.");
        }
        return "redirect:/room/list";
    }
}
