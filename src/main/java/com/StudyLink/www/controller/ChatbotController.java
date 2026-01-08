package com.StudyLink.www.controller;

import com.StudyLink.www.dto.ChatbotDTO;
import com.StudyLink.www.entity.StudentScore;
import com.StudyLink.www.repository.StudentScoreRepository;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;
    private final StudentScoreRepository studentScoreRepository;

    @GetMapping("/room/chatbot")
    public String chatbot() {
        return "chatbot"; // templates/chatbot.html 반환
    }

    @PostMapping("/room/chatbot/send")
    @ResponseBody
    public ChatbotDTO.Response send(@RequestBody ChatbotDTO.Request request, Principal principal) {
        // 로그인한 사용자가 있는 경우 DB에서 성적을 조회하여 요청에 포함
        if (principal != null) {
            String email = principal.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                List<StudentScore> dbScores = studentScoreRepository.findByUser_UserId(user.getUserId());
                if (!dbScores.isEmpty()) {
                    List<ChatbotDTO.UserScore> dtoScores = dbScores.stream()
                            .map(score -> ChatbotDTO.UserScore.builder()
                                    .subjectName(score.getSubjectName())
                                    .score(score.getScore())
                                    .scoreType(score.getScoreType())
                                    .category(score.getCategory())
                                    .build())
                            .collect(Collectors.toList());
                    request.setUserScores(dtoScores);
                }
            });
        }
        return chatbotService.getChatResponse(request);
    }
}
