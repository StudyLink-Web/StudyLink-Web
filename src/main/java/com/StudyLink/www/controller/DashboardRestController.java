package com.StudyLink.www.controller;

import com.StudyLink.www.dto.DashboardDTO;
import com.StudyLink.www.dto.StudentScoreDTO;
import com.StudyLink.www.entity.StudentProfile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.AuthService;
import com.StudyLink.www.service.StudentProfileService;
import com.StudyLink.www.service.StudentScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardRestController {

    private final StudentScoreService studentScoreService;
    private final StudentProfileService studentProfileService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String AI_ANALYSIS_URL = "https://yaimbot23-chatbot-docker.hf.space/analyze-dashboard";

    /**
     * 현재 사용자의 점수 저장 여부 및 기본 데이터 조회
     */
    @GetMapping("/status")
    public ResponseEntity<DashboardDTO.StatusResponse> getStatus(Authentication authentication) {
        Users user = getCurrentUser(authentication);
        List<StudentScoreDTO> scores = studentScoreService.getScoresByUserId(user.getUserId());
        
        return ResponseEntity.ok(DashboardDTO.StatusResponse.builder()
                .hasScores(!scores.isEmpty())
                .build());
    }

    /**
     * 성적 데이터 조회
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getDashboardData(Authentication authentication) {
        Users user = getCurrentUser(authentication);
        List<StudentScoreDTO> scores = studentScoreService.getScoresByUserId(user.getUserId());
        Optional<StudentProfile> profile = studentProfileService.getStudentProfile(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("scores", scores);
        response.put("profile", profile.orElse(null));
        response.put("user", Map.of("nickname", user.getNickname(), "name", user.getName()));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 성적 데이터 저장
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveScores(
            Authentication authentication, 
            @RequestBody List<StudentScoreDTO> scores) {
        Users user = getCurrentUser(authentication);
        studentScoreService.saveScores(user.getUserId(), scores);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "성적이 성공적으로 저장되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 파이썬 서버 연동 - AI 심층 분석 결과 조회
     */
    @GetMapping("/analysis")
    public ResponseEntity<DashboardDTO.AnalysisResponse> getAnalysis(Authentication authentication) {
        Users user = getCurrentUser(authentication);
        List<StudentScoreDTO> scores = studentScoreService.getScoresByUserId(user.getUserId());
        Optional<StudentProfile> profile = studentProfileService.getStudentProfile(user.getUserId());

        if (scores.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        DashboardDTO.AnalysisRequest request = DashboardDTO.AnalysisRequest.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .userScores(scores)
                .targetUniversity(profile.map(StudentProfile::getTargetUniversity).orElse("미설정"))
                .targetMajor(profile.map(StudentProfile::getTargetMajor).orElse("미설정"))
                .build();

        try {
            log.info("📌 파이썬 서버 분석 요청 중...");
            DashboardDTO.AnalysisResponse response = restTemplate.postForObject(AI_ANALYSIS_URL, request, DashboardDTO.AnalysisResponse.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 파이썬 서버 분석 연동 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private Users getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            log.error("❌ 인증 정보가 없거나 유효하지 않습니다.");
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        String identifier = authentication.getName();
        
        // OAuth2 로그인 대응: 이메일 추출 시도
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            if (attributes.containsKey("email")) {
                identifier = (String) attributes.get("email");
            } else if (attributes.containsKey("response")) { // Naver 대응
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                if (response.containsKey("email")) identifier = (String) response.get("email");
            } else if (attributes.containsKey("kakao_account")) { // Kakao 대응
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount.containsKey("email")) identifier = (String) kakaoAccount.get("email");
            }
        }

        log.info("🔍 사용자 조회 시도 (Identifier: {})", identifier);
        
        final String finalId = identifier;
        return authService.getUserByEmail(finalId)
                .orElseGet(() -> {
                    // 이메일로 못 찾으면 username으로 재시도
                    return userRepository.findByUsername(finalId)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + finalId));
                });
    }
}
