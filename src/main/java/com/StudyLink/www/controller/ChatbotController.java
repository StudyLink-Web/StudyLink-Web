package com.StudyLink.www.controller;

import com.StudyLink.www.dto.ChatbotDTO;
import com.StudyLink.www.entity.StudentScore;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.StudentScoreRepository;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.ChatBotSessionService;
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
@lombok.extern.slf4j.Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatBotSessionService sessionService; // 추가
    private final UserRepository userRepository;
    private final StudentScoreRepository studentScoreRepository;

    @GetMapping("/chatbot")
    public String chatbot() {
        return "chatbot"; // templates/chatbot.html 반환
    }

    @PostMapping("/chatbot/send")
    @ResponseBody
    public ChatbotDTO.Response send(@RequestBody ChatbotDTO.Request request, Principal principal) {
        log.info("요청 수신 - Session ID: {}, Query: {}", request.getSessionId(), request.getQuery());
        
        // 1. 대화 내역 저장 (사용자 질문)
        if (request.getSessionId() != null) {
            sessionService.saveMessage(request.getSessionId(), "USER", request.getQuery());
        }

        // 2. 기존 성적 정보 조회 및 포함 (로그인한 경우)
        if (principal != null) {
            String email = "";
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
                email = token.getPrincipal().getAttribute("email");
            } else {
                email = principal.getName();
            }

            final String identifier = email;
            Users foundUser = userRepository.findByEmail(identifier)
                    .orElseGet(() -> userRepository.findByUsername(identifier).orElse(null));

            if (foundUser != null) {
                List<StudentScore> dbScores = studentScoreRepository.findByUser_UserIdAndScoreRecordIsNull(foundUser.getUserId());
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
            }
        }

        // 3. AI 서버에 질문 전달
        ChatbotDTO.Response response = chatbotService.getChatResponse(request);

        // 4. 대화 내역 저장 (AI 응답)
        if (request.getSessionId() != null && response != null && response.getAnswer() != null) {
            sessionService.saveMessage(request.getSessionId(), "BOT", response.getAnswer());
            
            // [추가] AI가 생성한 제목이 있다면 세션 제목 업데이트
            if (response.getTitle() != null && !response.getTitle().isEmpty()) {
                sessionService.updateSessionTitle(request.getSessionId(), response.getTitle());
            }
        }

        return response;
    }
}
