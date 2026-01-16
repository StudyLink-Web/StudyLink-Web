package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
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

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("🔐 [START] CustomOAuth2UserService.loadUser() 시작");

            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("✅ super.loadUser() 완료");

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("🔐 OAuth2 로그인 제공자: {}", registrationId);

            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            String nameAttributeKey = "sub";

            // ⭐ Google (OIDC)
            if ("google".equals(registrationId)) {
                log.info("🔍 Google 로그인 처리 시작");
                String sub = (String) attributes.get("sub");
                String name = (String) attributes.getOrDefault("name", "구글사용자");
                String email = (String) attributes.getOrDefault("email", "");
                String picture = (String) attributes.getOrDefault("picture", "");

                if (email == null || email.isEmpty()) {
                    email = "google_" + sub + "@google.com";
                }

                String fixedUsername = "google_" + sub;
                String fixedNickname = "Google_" + sub;

                log.info("✅ Google 사용자: name={}, email={}", name, email);
                saveOAuth2User(fixedUsername, email, picture, name, "google", fixedNickname);

                attributes.put("username", fixedUsername);
                attributes.put("nickname", fixedNickname);
                attributes.put("provider", "google");
                nameAttributeKey = "sub";

                log.info("✅ Google 사용자 처리 완료");
            }
            // ⭐ Kakao
            else if ("kakao".equals(registrationId)) {
                log.info("🔍 Kakao 로그인 처리 시작");
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

                String id = attributes.get("id").toString();
                String nickname = (properties != null) ? (String) properties.get("nickname") : "카카오사용자";

                // ⭐ 개발 환경: 카카오 이메일 대신 항상 임시 이메일 생성
                String email = "kakao_" + id + "@kakao.com";
                log.warn("⚠️ Kakao 개발환경: 임시 email 생성: {}", email);

                String picture = (properties != null) ? (String) properties.get("profile_image") : "";

                // ⭐ FIX: name은 nickname으로 사용 (Kakao는 name 필드가 없음)
                String fixedName = nickname;
                String fixedUsername = "kakao_" + id;
                String fixedNickname = "Kakao_" + id;

                log.info("✅ Kakao 사용자: nickname={}, email={}", nickname, email);
                saveOAuth2User(fixedUsername, email, picture, fixedName, "kakao", fixedNickname);

                attributes.put("username", fixedUsername);
                attributes.put("nickname", fixedNickname);
                attributes.put("name", fixedName);
                attributes.put("provider", "kakao");
                attributes.put("id", id);
                attributes.put("email", email);

                nameAttributeKey = "id";

                log.info("✅ Kakao 사용자 처리 완료");
            }

// ⭐ Naver
            else if ("naver".equals(registrationId)) {
                log.info("🔍 Naver 로그인 처리 시작");
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");

                String id = (String) response.get("id");
                String name = (String) response.getOrDefault("name", "네이버사용자");
                String email = (String) response.get("email");
                String picture = (String) response.get("profile_image");

                // ⭐ email이 null이면 생성
                if (email == null || email.isEmpty()) {
                    email = "naver_" + id + "@naver.com";
                    log.warn("⚠️ Naver email이 null - 임시 email 생성: {}", email);
                }

                String fixedUsername = "naver_" + id;
                String fixedNickname = "Naver_" + id;

                log.info("✅ Naver 사용자: name={}, email={}", name, email);
                saveOAuth2User(fixedUsername, email, picture, name, "naver", fixedNickname);

                attributes.put("username", fixedUsername);
                attributes.put("nickname", fixedNickname);
                attributes.put("name", name);  // ⭐ 추가!
                attributes.put("provider", "naver");
                attributes.put("email", email);
                nameAttributeKey = "username";

                log.info("✅ Naver 사용자 처리 완료");
            }


            log.info("✅ [SUCCESS] CustomOAuth2UserService.loadUser() 완료");

            return new DefaultOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes,
                    nameAttributeKey
            );
        } catch (Exception e) {
            log.error("❌ [ERROR] CustomOAuth2UserService 중 오류: {}", e.getMessage());
            throw new OAuth2AuthenticationException("OAuth2 처리 중 오류: " + e.getMessage());
        }
    }

    private void saveOAuth2User(String username, String email, String profileImage, String name, String provider, String nickname) {
        try {
            log.info("🔍 [DEBUG] saveOAuth2User 시작 - username: {}", username);

            Optional<Users> existingUser = userRepository.findByUsername(username);

            Users user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setName(name);
                user.setNickname(nickname);
                user.setProfileImageUrl(profileImage);
                user.setOauthProvider(provider);
                user.setOauthId(username);
                user.setEmail(email);
                // schoolEmail이 null이면 빈 문자열 설정 (unique 제약 회피)
                if (user.getSchoolEmail() == null) {
                    user.setSchoolEmail(null);  // NULL로 유지 (unique 제약 자동 무시)
                }
            } else {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
                String encodedPassword = (encoder != null)
                        ? encoder.encode("oauth_" + provider + "_" + System.currentTimeMillis())
                        : "oauth_" + provider + "_" + System.currentTimeMillis();

                user = Users.builder()
                        .username(username)
                        .nickname(nickname)
                        .email(email)
                        .name(name)
                        .profileImageUrl(profileImage)
                        .oauthProvider(provider)
                        .oauthId(username)
                        .password(encodedPassword)
                        .role("ROLE_USER")
                        .isActive(true)
                        // OAuth2 사용자는 schoolEmail을 NULL로 설정
                        .schoolEmail(null)
                        .isVerifiedStudent(false)
                        .build();
            }

            Users savedUser = userRepository.save(user);
            log.info("💾 사용자 정보 저장 완료: username={}, user_id={}, email={}", username, savedUser.getUserId(), email);

        } catch (Exception e) {
            log.error("❌ [ERROR] 사용자 정보 저장 실패: {}", e.getMessage());
            throw new OAuth2AuthenticationException("사용자 저장 중 오류: " + e.getMessage());
        }
    }
}
