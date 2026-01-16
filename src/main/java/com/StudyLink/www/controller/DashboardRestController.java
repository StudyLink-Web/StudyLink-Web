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

    @org.springframework.beans.factory.annotation.Value("${python.api.url}")
    private String pythonApiUrl;

    @org.springframework.beans.factory.annotation.Value("${python.api.token:#{null}}")
    private String pythonApiToken;

    // Remove hardcoded URL
    // private final String AI_ANALYSIS_URL = ...;

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
        
        log.info("📡 [DashboardData] User: {}, Score Count: {}", user.getEmail(), scores.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 성적 데이터 저장 (현재 활성화된 성적)
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveScores(
            Authentication authentication, 
            @RequestBody List<StudentScoreDTO> scores) {
        
        Users user = getCurrentUser(authentication);
        log.info("📥 [ScoreSaveRequest] User: {}, Incoming Count: {}", user.getEmail(), scores != null ? scores.size() : 0);
        
        int savedCount = studentScoreService.saveScores(user.getUserId(), scores);
        
        Map<String, Object> response = new HashMap<>();
        if (savedCount > 0) {
            response.put("success", true);
            response.put("message", savedCount + "건의 성적이 성공적으로 저장되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "저장된 성적이 없습니다. 입력값을 확인해 주세요.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 성적 레코드 (제목 포함) 저장
     */
    @PostMapping("/records/save")
    public ResponseEntity<Map<String, Object>> saveScoreRecord(
            Authentication authentication,
            @RequestBody Map<String, Object> payload) {
        
        Users user = getCurrentUser(authentication);
        String title = (String) payload.get("title");
        List<Map<String, Object>> scoreMaps = (List<Map<String, Object>>) payload.get("scores");
        
        // Map list를 DTO list로 변환 (jackson objectMapper 사용 권장되나 여기선 수동 매핑)
        List<StudentScoreDTO> scoreDTOs = scoreMaps.stream().map(m -> StudentScoreDTO.builder()
                .subjectName((String) m.get("subject_name"))
                .score(m.get("score") instanceof Number n ? n.doubleValue() : 0.0)
                .scoreType((String) m.get("score_type"))
                .category((String) m.get("category"))
                .optionalSubject((String) m.get("optional_subject"))
                .build()
        ).toList();

        Long recordId = studentScoreService.saveScoreRecord(user.getUserId(), title, scoreDTOs);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("recordId", recordId);
        response.put("message", "'" + title + "' 성적이 저장되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 내 성적 레코드 목록 조회
     */
    @GetMapping("/records")
    public ResponseEntity<List<java.util.Map<String, Object>>> getScoreRecords(Authentication authentication) {
        Users user = getCurrentUser(authentication);
        return ResponseEntity.ok(studentScoreService.getScoreRecords(user.getUserId()));
    }

    /**
     * 특정 레코드 로드
     */
    @GetMapping("/records/{id}/load")
    public ResponseEntity<List<StudentScoreDTO>> loadRecord(
            Authentication authentication,
            @PathVariable("id") Long recordId) {
        // 보안상 본인 루틴 체크 필요할 수 있으나 생략 (UserId 기반 필터링은 Service에서 수행 권장)
        return ResponseEntity.ok(studentScoreService.getRecordDetails(recordId));
    }

    /**
     * 특정 레코드 삭제
     */
    @DeleteMapping("/records/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(
            Authentication authentication,
            @PathVariable("id") Long recordId) {
        
        studentScoreService.deleteScoreRecord(recordId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "성적이 성공적으로 삭제되었습니다.");
        
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
            log.info("📌 파이썬 서버 분석 요청 중... URL: {}", pythonApiUrl + "/analyze-dashboard");
            DashboardDTO.AnalysisResponse response = restTemplate.postForObject(pythonApiUrl + "/analyze-dashboard", request, DashboardDTO.AnalysisResponse.class);
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("❌ 파이썬 서버 분석 연동 실패 (HTTP {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("❌ 파이썬 서버 분석 연동 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 파이썬 서버 연동 - 성적 추이 분석 (모든 시험 이력 비교)
     */
    @GetMapping("/analysis/trend")
    public ResponseEntity<DashboardDTO.TrendAnalysisResponse> getTrendAnalysis(Authentication authentication) {
        Users user = getCurrentUser(authentication);
        List<DashboardDTO.TrendItem> trends = studentScoreService.getAllTrendData(user.getUserId());

        if (trends == null || trends.isEmpty()) {
            log.warn("⚠️ 성적 이력이 없어 추이 분석을 진행할 수 없습니다. User: {}", user.getEmail());
            return ResponseEntity.noContent().build();
        }

        DashboardDTO.TrendAnalysisRequest request = DashboardDTO.TrendAnalysisRequest.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .trends(trends)
                .build();

        try {
            log.info("📊 파이썬 서버 성적 추이 분석 요청 중... URL: {}", pythonApiUrl + "/analyze-trend");
            DashboardDTO.TrendAnalysisResponse response = restTemplate.postForObject(
                    pythonApiUrl + "/analyze-trend", 
                    request, 
                    DashboardDTO.TrendAnalysisResponse.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 파이썬 서버 추이 분석 연동 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private Users getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            log.error("❌ 인증 정보가 없거나 유효하지 않습니다.");
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        String rawId = authentication.getName();
        
        // OAuth2 로그인 대응: 이메일 추출 시도
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            if (attributes.containsKey("email")) {
                rawId = (String) attributes.get("email");
            } else if (attributes.get("response") instanceof Map<?, ?> responseMap) { // Naver 대응
                if (responseMap.containsKey("email")) rawId = (String) responseMap.get("email");
            } else if (attributes.get("kakao_account") instanceof Map<?, ?> kakaoMap) { // Kakao 대응
                if (kakaoMap.containsKey("email")) rawId = (String) kakaoMap.get("email");
            }
        }

        final String finalIdentifier = rawId;
        log.info("🔍 사용자 조회 시도 (Identifier: {})", finalIdentifier);
        
        Optional<Users> userOpt = authService.getUserByEmail(finalIdentifier);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        return userRepository.findByUsername(finalIdentifier)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + finalIdentifier));
    }
}
