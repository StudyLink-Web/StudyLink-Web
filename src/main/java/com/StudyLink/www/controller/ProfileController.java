package com.StudyLink.www.controller;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ProfileController (프로필 관리 API 컨트롤러)
 * 프로필 정보 수정 및 관리 API 처리
 *
 * 담당 기능:
 * - 프로필 정보 조회
 * - 기본 정보 수정 (이름, 닉네임)
 * - 프로필 사진 업로드/삭제
 * - 관심사 수정
 * - 휴대폰 번호 수정
 * - 학년/학과 수정
 * - 프로필 정보 종합 수정
 * - 이메일 인증 여부 조회
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    /**
     * 프로필 정보 조회
     * GET /api/profile
     *
     * 현재 로그인된 사용자의 프로필 정보를 조회
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 프로필 정보 JSON
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> profile = profileService.getProfile(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profile);

            log.info("✅ 프로필 조회: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 프로필 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 기본 정보 수정 (이름, 닉네임)
     * PUT /api/profile/basic-info
     *
     * @param request 요청 데이터 (name, nickname)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @PutMapping("/basic-info")
    public ResponseEntity<Map<String, Object>> updateBasicInfo(
            @RequestBody UpdateBasicInfoRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 입력값 검증
            if (request.getName() == null && request.getNickname() == null) {
                throw new IllegalArgumentException("수정할 정보를 입력하세요");
            }

            Map<String, Object> updatedProfile = profileService.updateBasicInfo(
                    user.getUserId(),
                    request.getName(),
                    request.getNickname()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "기본 정보가 수정되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 기본 정보 수정: userId={}, name={}, nickname={}",
                    user.getUserId(), request.getName(), request.getNickname());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 기본 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 기본 정보 수정 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 프로필 사진 업로드
     * PUT /api/profile/image
     *
     * @param request 요청 데이터 (profileImageUrl)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @PutMapping("/image")
    public ResponseEntity<Map<String, Object>> updateProfileImage(
            @RequestBody UpdateProfileImageRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            if (request.getProfileImageUrl() == null || request.getProfileImageUrl().isEmpty()) {
                throw new IllegalArgumentException("프로필 이미지 URL을 입력하세요");
            }

            Map<String, Object> updatedProfile = profileService.updateProfileImage(
                    user.getUserId(),
                    request.getProfileImageUrl()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 사진이 업로드되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 프로필 사진 업로드: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 프로필 사진 업로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 프로필 사진 업로드 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 프로필 사진 삭제
     * DELETE /api/profile/image
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, Object>> deleteProfileImage(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> updatedProfile = profileService.deleteProfileImage(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 사진이 삭제되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 프로필 사진 삭제: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 프로필 사진 삭제 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 관심사 수정
     * PUT /api/profile/interests
     *
     * @param request 요청 데이터 (interests)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @PutMapping("/interests")
    public ResponseEntity<Map<String, Object>> updateInterests(
            @RequestBody UpdateInterestsRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            if (request.getInterests() == null) {
                throw new IllegalArgumentException("관심사를 입력하세요");
            }

            Map<String, Object> updatedProfile = profileService.updateInterests(
                    user.getUserId(),
                    request.getInterests()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "관심사가 수정되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 관심사 수정: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 관심사 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 관심사 수정 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 휴대폰 번호 수정
     * PUT /api/profile/phone
     *
     * @param request 요청 데이터 (phone)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @PutMapping("/phone")
    public ResponseEntity<Map<String, Object>> updatePhone(
            @RequestBody UpdatePhoneRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            if (request.getPhone() == null || request.getPhone().isEmpty()) {
                throw new IllegalArgumentException("휴대폰 번호를 입력하세요");
            }

            Map<String, Object> updatedProfile = profileService.updatePhone(
                    user.getUserId(),
                    request.getPhone()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "휴대폰 번호가 수정되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 휴대폰 번호 수정: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 휴대폰 번호 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 휴대폰 번호 수정 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 학년/학과 수정
     * PUT /api/profile/grade-year
     *
     * @param request 요청 데이터 (gradeYear)
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @PutMapping("/grade-year")
    public ResponseEntity<Map<String, Object>> updateGradeYear(
            @RequestBody UpdateGradeYearRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            if (request.getGradeYear() == null || request.getGradeYear().isEmpty()) {
                throw new IllegalArgumentException("학년/학과를 입력하세요");
            }

            Map<String, Object> updatedProfile = profileService.updateGradeYear(
                    user.getUserId(),
                    request.getGradeYear()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "학년/학과가 수정되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 학년/학과 수정: userId={}, gradeYear={}", user.getUserId(), request.getGradeYear());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 학년/학과 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 학년/학과 수정 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 프로필 정보 종합 수정
     * PUT /api/profile/comprehensive
     *
     * 여러 필드를 한 번에 수정
     *
     * @param request 요청 데이터
     * @param authentication 현재 로그인된 사용자 정보
     * @return 수정된 프로필 정보 JSON
     */
    @PutMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> updateProfileComprehensive(
            @RequestBody UpdateProfileComprehensiveRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Map<String, Object> updatedProfile = profileService.updateProfileComprehensive(
                    user.getUserId(),
                    request.getName(),
                    request.getNickname(),
                    request.getPhone(),
                    request.getGradeYear(),
                    request.getInterests(),
                    request.getProfileImageUrl()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필이 수정되었습니다");
            response.put("data", updatedProfile);

            log.info("✅ 프로필 종합 수정: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.warn("⚠️ 프로필 종합 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 프로필 종합 수정 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 이메일 인증 여부 조회
     * GET /api/profile/email-verified
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 이메일 인증 여부 JSON
     */
    @GetMapping("/email-verified")
    public ResponseEntity<Map<String, Object>> isEmailVerified(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Boolean isVerified = profileService.isEmailVerified(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("emailVerified", isVerified);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 이메일 인증 여부 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 학생 인증 여부 조회
     * GET /api/profile/student-verified
     *
     * @param authentication 현재 로그인된 사용자 정보
     * @return 학생 인증 여부 JSON
     */
    @GetMapping("/student-verified")
    public ResponseEntity<Map<String, Object>> isStudentVerified(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            Boolean isVerified = profileService.isStudentVerified(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentVerified", isVerified);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 학생 인증 여부 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 닉네임 중복 확인
     * GET /api/profile/check-nickname
     *
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부 JSON
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
        try {
            if (nickname == null || nickname.isEmpty()) {
                throw new IllegalArgumentException("닉네임을 입력하세요");
            }

            boolean isAvailable = profileService.isNicknameAvailable(nickname);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", isAvailable);
            response.put("message", isAvailable ? "사용 가능한 닉네임입니다" : "이미 사용 중인 닉네임입니다");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 닉네임 중복 확인 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 닉네임으로 사용자 프로필 조회
     * GET /api/profile/by-nickname/{nickname}
     *
     * @param nickname 사용자 닉네임
     * @return 사용자 프로필 JSON
     */
    @GetMapping("/by-nickname/{nickname}")
    public ResponseEntity<Map<String, Object>> getUserByNickname(@PathVariable String nickname) {
        try {
            Optional<Map<String, Object>> userProfile = profileService.getUserByNickname(nickname);

            if (userProfile.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "사용자를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userProfile.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            log.error("❌ 사용자 프로필 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========== Request 클래스 ==========

    /**
     * 기본 정보 수정 요청
     */
    public static class UpdateBasicInfoRequest {
        private String name;
        private String nickname;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }

    /**
     * 프로필 사진 업로드 요청
     */
    public static class UpdateProfileImageRequest {
        private String profileImageUrl;

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    }

    /**
     * 관심사 수정 요청
     */
    public static class UpdateInterestsRequest {
        private String interests;

        public String getInterests() { return interests; }
        public void setInterests(String interests) { this.interests = interests; }
    }

    /**
     * 휴대폰 번호 수정 요청
     */
    public static class UpdatePhoneRequest {
        private String phone;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * 학년/학과 수정 요청
     */
    public static class UpdateGradeYearRequest {
        private String gradeYear;

        public String getGradeYear() { return gradeYear; }
        public void setGradeYear(String gradeYear) { this.gradeYear = gradeYear; }
    }

    /**
     * 프로필 종합 수정 요청
     */
    public static class UpdateProfileComprehensiveRequest {
        private String name;
        private String nickname;
        private String phone;
        private String gradeYear;
        private String interests;
        private String profileImageUrl;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getGradeYear() { return gradeYear; }
        public void setGradeYear(String gradeYear) { this.gradeYear = gradeYear; }

        public String getInterests() { return interests; }
        public void setInterests(String interests) { this.interests = interests; }

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    }
}
