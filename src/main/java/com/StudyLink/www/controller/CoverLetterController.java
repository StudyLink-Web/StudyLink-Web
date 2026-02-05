package com.StudyLink.www.controller;

import com.StudyLink.www.dto.CoverLetterDTO;
import com.StudyLink.www.entity.MembershipType;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.AuthService;
import com.StudyLink.www.service.CoverLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cover-letter")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterController {

    private final CoverLetterService coverLetterService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateAI(@RequestBody CoverLetterDTO.Request request,
            Authentication authentication) {
        Optional<Users> userOpt = getCurrentUser(authentication);
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).build();

        Users user = userOpt.get();

        // 멤버십 체크 (STANDARD 이상만 가능)
        if (user.getMembership() == MembershipType.FREE) {
            log.warn("🚫 [CoverLetter] 멤버십 부족: {} (Free)", user.getEmail());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "PREMIUM_REQUIRED");
            errorResponse.put("message", "AI 자소서 생성 기능은 Standard 또는 Premium PASS 요금제 이용 시 가능합니다.");
            return ResponseEntity.status(403).body(errorResponse);
        }

        String aiContent = coverLetterService.generateAIContent(user, request);

        Map<String, String> response = new HashMap<>();
        response.put("content", aiContent != null ? aiContent : "AI 생성에 실패했습니다 (결과 없음).");
        return ResponseEntity.ok(response);
    }

    /**
     * 생기부 텍스트에서 키워드 추출
     */
    @PostMapping("/extract")
    public ResponseEntity<CoverLetterDTO.ExtractResponse> extractKeywords(
            @RequestBody CoverLetterDTO.ExtractRequest request, Authentication authentication) {
        Optional<Users> userOpt = getCurrentUser(authentication);
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).build();

        Users user = userOpt.get();

        // 멤버십 체크 (STANDARD 이상만 가능)
        if (user.getMembership() == MembershipType.FREE) {
            log.warn("🚫 [CoverLetter] 멤버십 부족(추출): {} (Free)", user.getEmail());
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(coverLetterService.extractFromRecord(request));
    }

    /**
     * 자소서 저장
     */
    @PostMapping("/save")
    @SuppressWarnings("unchecked")
    public ResponseEntity<CoverLetterDTO.Response> saveCoverLetter(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        Optional<Users> userOpt = getCurrentUser(authentication);
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).build();

        Users user = userOpt.get();

        // DTO 수동 매핑 (Jackson을 통한 Map -> DTO 변환 시도 가능하나 명시적 매핑 선호)
        Map<String, Object> reqMap = (Map<String, Object>) payload.get("request");
        String content = (String) payload.get("content");

        CoverLetterDTO.Request request = CoverLetterDTO.Request.builder()
                .title((String) reqMap.get("title"))
                .questionNum((Integer) reqMap.get("questionNum"))
                .questionText((String) reqMap.get("questionText"))
                .targetUniversity((String) reqMap.get("targetUniversity"))
                .targetMajor((String) reqMap.get("targetMajor"))
                .build();

        return ResponseEntity.ok(coverLetterService.saveCoverLetter(user, request, content));
    }

    /**
     * 나의 자소서 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<CoverLetterDTO.Response>> getList(Authentication authentication) {
        Optional<Users> userOpt = getCurrentUser(authentication);
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).build();

        Users user = userOpt.get();
        return ResponseEntity.ok(coverLetterService.getMyCoverLetters(user));
    }

    /**
     * 자소서 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coverLetterService.deleteCoverLetter(id);
        return ResponseEntity.ok().build();
    }

    private Optional<Users> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            log.warn("인증 정보가 없거나 유효하지 않습니다.");
            return Optional.empty();
        }
        String identifier = authentication.getName();

        Optional<Users> userOpt = authService.getUserByEmail(identifier);
        if (userOpt.isPresent()) {
            log.info("✅ [CoverLetter] 이메일로 사용자 조회 성공: {}", identifier);
            return userOpt;
        }

        log.warn("⚠️ [CoverLetter] 이메일 조회 실패, Username으로 재시도: {}", identifier);
        return userRepository.findByUsername(identifier);
    }
}
