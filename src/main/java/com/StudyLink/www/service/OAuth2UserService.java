package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 사용자 서비스
 * 소셜 로그인 사용자 정보 처리
 * - 카카오, 네이버, 구글 지원
 */
@Service
// @RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    // ⭐ 변경: ObjectProvider로 변경 (순환 참조 완전 해결)
    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;


    /**
     * OAuth2 사용자 정보 로드
     *
     * @param userRequest - OAuth2 사용자 요청
     * @return OAuth2User - 처리된 사용자 정보
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("🔐 [START] loadUser() 메서드 시작");

            // 기본 사용자 정보 로드
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("✅ super.loadUser() 완료");

            // 소셜 로그인 제공자 확인
            String registrationId = userRequest.getClientRegistration().getRegistrationId();

            System.out.println("🔐 OAuth2 로그인: " + registrationId);
            log.info("🔐 OAuth2 로그인 시작: {}", registrationId);

            // 제공자별 사용자 정보 처리
            OAuth2User result = null;
            switch (registrationId) {
                case "kakao":
                    log.info("🔍 Kakao 로그인 처리 시작");
                    result = processKakaoUser(oAuth2User);
                    log.info("✅ Kakao 사용자 처리 완료");
                    break;
                case "naver":
                    log.info("🔍 Naver 로그인 처리 시작");
                    result = processNaverUser(oAuth2User);
                    log.info("✅ Naver 사용자 처리 완료");
                    break;
                case "google":
                    log.info("🔍 Google 로그인 처리 시작");
                    result = processGoogleUser(oAuth2User);
                    log.info("✅ Google 사용자 처리 완료");
                    break;
                default:
                    log.warn("⚠️ 지원하지 않는 제공자: {}", registrationId);
                    result = oAuth2User;
            }

            log.info("✅ [SUCCESS] loadUser() 메서드 완료 - 반환값: {}", result != null ? "OK" : "NULL");
            return result;
        } catch (Exception e) {
            log.error("❌ [ERROR] loadUser() 중 오류 발생: {}", e.getMessage());
            log.error("❌ 스택 트레이스: ", e);
            e.printStackTrace();
            throw new OAuth2AuthenticationException("OAuth2 로그인 처리 중 오류: " + e.getMessage());
        }
    }


    /**
     * 카카오 사용자 정보 처리
     * API 응답: {id, properties: {nickname, profile_image}, kakao_account: {email}}
     */
    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String id = attributes.get("id").toString();
        String nickname = "";
        String profileImage = "";
        String email = "";

        // properties에서 닉네임, 프로필 이미지 추출
        if (properties != null) {
            nickname = (String) properties.getOrDefault("nickname", "카카오사용자");
            profileImage = (String) properties.getOrDefault("profile_image", "");
        }

        // kakao_account에서 이메일 추출
        if (kakaoAccount != null) {
            email = (String) kakaoAccount.getOrDefault("email", "");
        }

        // ⭐ 수정: 이메일에서 사용자명 추출 (@ 앞부분)
        //String fixedUsername = email != null && !email.isEmpty() ? email.split("@")[0] : nickname;

        // ⭐ 수정: 카카오는 이메일을 안 주는 경우가 많으므로 닉네임 사용
        String fixedUsername = nickname;
        if (fixedUsername == null || fixedUsername.isEmpty()) {
            fixedUsername = "카카오사용자_" + id;
        }

        // ⭐ 이메일이 없으면 가상 이메일 생성
        String finalEmail = (email != null && !email.isEmpty()) ? email : "kakao_" + id + "@kakao.com";

        // 사용자 정보 통합
        attributes.put("username", "kakao_" + id);
        attributes.put("nickname", fixedUsername);
        attributes.put("name", fixedUsername);  // ⭐ nickname 대신 fixedUsername 사용
        attributes.put("email", finalEmail);    // ⭐ email 대신 finalEmail 사용
        attributes.put("picture", profileImage);
        attributes.put("provider", "kakao");

        System.out.println("✅ 카카오 사용자: " + nickname + " (" + finalEmail + ")");

        // DB에 사용자 저장
        saveOAuth2User("kakao_" + id, finalEmail, profileImage, nickname, "kakao");

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "nickname"
        );
    }


    /**
     * 네이버 사용자 정보 처리
     * API 응답: {resultcode, message, response: {id, name, email, profile_image}}
     * ⭐ 수정: 이메일 정보 제대로 가져오기 추가
     */
    private OAuth2User processNaverUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        // ⭐ 중요: Naver는 response 객체 안에 데이터가 있음
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String id = "";
        String name = "";
        String email = "";  // ⭐ 추가: 이메일 변수 초기화
        String profileImage = "";

        if (response != null) {
            id = (String) response.getOrDefault("id", "");
            name = (String) response.getOrDefault("name", "네이버사용자");
            email = (String) response.getOrDefault("email", "");  // ⭐ 추가: 이메일 가져오기
            profileImage = (String) response.getOrDefault("profile_image", "");

            // ⭐ null 체크 추가
            if (id == null || id.isEmpty()) {
                log.error("❌ Naver ID가 null이거나 비어있습니다!");
                throw new IllegalArgumentException("Naver 사용자 ID를 찾을 수 없습니다.");
            }

            // ⭐ 디버깅 로그
            log.info("🔍 [DEBUG] Naver response에서 추출한 email: {}", email);
            log.info("🔍 [DEBUG] Naver response 전체: {}", response);
        } else {
            log.error("❌ Naver response 객체가 null입니다!");
            throw new IllegalArgumentException("Naver API 응답이 올바르지 않습니다.");
        }

        // ⭐ 이메일에서 사용자명 추출 (@ 앞부분)
        String fixedUsername = email != null && !email.isEmpty() ? email.split("@")[0] : name;

        // ⭐ 로그 추가: 추출된 사용자명 확인
        log.info("✅ Naver 이메일: {}, 추출된 사용자명: {}", email, fixedUsername);

        // 사용자 정보 통합
        attributes.put("id", id);  // ⭐ 추가: "id"를 attributes에 명시적으로 추가
        attributes.put("username", "naver_" + id);
        attributes.put("name", fixedUsername);  // ⭐ attributes의 "name"을 업데이트
        attributes.put("email", email);  // ⭐ 이메일도 attributes에 추가
        attributes.put("picture", profileImage);
        attributes.put("provider", "naver");

        System.out.println("✅ 네이버 사용자: " + name + " (" + email + ")");

        // DB에 사용자 저장
        saveOAuth2User("naver_" + id, email, profileImage, name, "naver");

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "id"  // ✅ nameAttributeKey: attributes의 "id" 키를 사용
        );
    }



    /**
     * 구글 사용자 정보 처리
     * API 응답: {sub, name, email, picture, locale, ...}
     */
    private OAuth2User processGoogleUser(OAuth2User oAuth2User) {
        try {
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

            log.info("🔍 [DEBUG] Google attributes 키 목록: {}", attributes.keySet());

            // ⭐ 수정: 'sub' 대신 'id' 사용 (Google OAuth2에서 'id' 사용)
            String sub = (String) attributes.get("id");
            if (sub == null) {
                log.error("❌ Google OAuth 응답에서 'id' 값 없음!");
                throw new OAuth2AuthenticationException("Google OAuth 응답에서 'id' 값을 찾을 수 없습니다.");
            }

            String name = (String) attributes.getOrDefault("name", "구글사용자");
            String email = (String) attributes.getOrDefault("email", "");
            String picture = (String) attributes.getOrDefault("picture", "");

            log.info("📋 전체 attributes: {}", attributes);
            log.info("✅ 구글 사용자 정보: name={}, email={}, id={}", name, email, sub);

            System.out.println("📋 전체 attributes: " + oAuth2User.getAttributes());
            System.out.println("✅ 구글 사용자: " + name + " (" + email + ") [id: " + sub + "]");

            // ⭐ 이메일에서 사용자명 추출 (@ 앞부분)
            String fixedUsername = email.split("@")[0];

            // ⭐ attributes 업데이트
            attributes.put("username", "google_" + sub);
            attributes.put("name", fixedUsername);  // ⭐ 추가: attributes의 "name"을 업데이트

            attributes.put("provider", "google");
            attributes.put("picture", picture);

            log.info("🔍 [DEBUG] saveOAuth2User 호출 전");
            saveOAuth2User("google_" + sub, email, picture, name, "google");
            log.info("🔍 [DEBUG] saveOAuth2User 호출 후");


            return new DefaultOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes,
                    "id"  // ⭐ 수정: "sub" → "id"
            );
        } catch (Exception e) {
            log.error("❌ processGoogleUser() 중 오류: {}", e.getMessage());
            log.error("❌ 스택 트레이스: ", e);
            throw new OAuth2AuthenticationException("Google 사용자 처리 중 오류: " + e.getMessage());
        }
    }


    /**
     * 소셜 로그인 사용자 정보 저장 (데이터베이스)
     * ⭐ 수정: 이메일 기반 저장 + 닉네임 자동 설정
     *
     * @param username     - 소셜 로그인 ID (provider_id 형식)
     * @param email        - 이메일 주소
     * @param profileImage - 프로필 이미지 URL
     * @param name         - 사용자 이름 (실명)
     * @param provider     - 제공자 (kakao, naver, google)
     */
    private void saveOAuth2User(String username, String email, String profileImage, String name, String provider) {
        try {
            log.info("🔍 [DEBUG] saveOAuth2User 시작");
            log.info("🔍 [DEBUG] username: {}", username);
            log.info("🔍 [DEBUG] email: {}", email);
            log.info("🔍 [DEBUG] name: {}", name);
            log.info("🔍 [DEBUG] provider: {}", provider);

            // ⭐ 이메일 기반으로 기존 사용자 조회
            Optional<Users> existingUser = userRepository.findByEmail(email);
            log.info("🔍 [DEBUG] 기존 사용자 조회 결과: {}", existingUser.isPresent());

            // ⭐ 이메일에서 사용자명 추출 (@ 앞부분) - 닉네임으로도 사용
            String fixedUsername = email != null && !email.isEmpty() ? email.split("@")[0] : "user_" + System.currentTimeMillis();
            log.info("🔍 [DEBUG] fixedUsername 생성됨: {}", fixedUsername);

            Users user;
            if (existingUser.isPresent()) {
                // ⭐ 기존 사용자 업데이트
                user = existingUser.get();
                user.setName(name);
                user.setNickname(fixedUsername);  // ⭐ 닉네임을 이메일 @ 앞부분으로 설정
                user.setProfileImageUrl(profileImage);
                user.setOauthProvider(provider);
                user.setOauthId(username);
                user.setEmail(email);  // ⭐ 이메일 설정
                log.info("🔄 기존 사용자 정보 업데이트: {} ({})", email, provider);
            } else {
                // ⭐ 새 사용자 생성
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
                log.info("🔍 [DEBUG] PasswordEncoder 조회: {}", encoder != null ? "OK" : "NULL");

                // ⭐ 비밀번호 암호화
                String encodedPassword = (encoder != null)
                        ? encoder.encode("oauth_" + provider + "_" + System.currentTimeMillis())
                        : "oauth_" + provider + "_" + System.currentTimeMillis();

                user = Users.builder()
                        .email(email)  // ⭐ 이메일 설정
                        .name(name)
                        .username(fixedUsername)  // ⭐ 사용자명: 이메일 @ 앞부분
                        .nickname(fixedUsername)  // ⭐ 닉네임: 이메일 @ 앞부분
                        .profileImageUrl(profileImage)
                        .oauthProvider(provider)
                        .oauthId(username)
                        .password(encodedPassword)
                        .role("ROLE_USER")
                        .isActive(true)
                        .build();

                log.info("🔍 [DEBUG] Users 엔티티 빌드 완료");
                log.info("✅ 신규 OAuth2 사용자 생성: email={}, username={}, nickname={}", email, fixedUsername, fixedUsername);
            }

            // ⭐ 데이터베이스에 저장
            log.info("🔍 [DEBUG] userRepository.save() 호출 전");
            Users savedUser = userRepository.save(user);
            log.info("🔍 [DEBUG] userRepository.save() 완료, saved user_id: {}", savedUser.getUserId());
            log.info("💾 사용자 정보 저장 완료: email={}, user_id={}", email, savedUser.getUserId());

            // ⭐ 추가: 저장 성공 확인
            log.info("✅ [SUCCESS] saveOAuth2User 완료 - 사용자 저장됨");

        } catch (Exception e) {
            log.error("❌ [ERROR] 사용자 정보 저장 실패: {} - {}", email, e.getMessage());
            log.error("❌ 스택 트레이스: ", e);
            e.printStackTrace();
            throw new OAuth2AuthenticationException("사용자 저장 중 오류 발생: " + e.getMessage());
        }
    }

}
