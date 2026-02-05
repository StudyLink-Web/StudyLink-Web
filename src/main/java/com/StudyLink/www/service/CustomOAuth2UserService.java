// CustomOAuth2UserService

package com.StudyLink.www.service;

import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
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

    @Autowired
    private StudentProfileService studentProfileService;

    // 클래스 로딩시 실행
    public CustomOAuth2UserService() {
        log.info("✅ CustomOAuth2UserService 생성됨!!!");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("🔐 [START] CustomOAuth2UserService.loadUser() 호출됨!!!");

            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("✅ super.loadUser() 완료");

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("🔐 OAuth2 로그인 제공자: {}", registrationId);

            // ⭐ 추가: registrationId 값이 뭔지 확인
            log.info("⭐⭐⭐ registrationId.equals(\"kakao\"): {}", "kakao".equals(registrationId));
            log.info("⭐⭐⭐ registrationId.equals(\"naver\"): {}", "naver".equals(registrationId));

            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            String nameAttributeKey = "username";

            // ⭐ Kakao
            if ("kakao".equals(registrationId)) {
                log.info("🔍 Kakao 로그인 처리 시작");
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

                String id = attributes.get("id").toString();
                String nickname = (properties != null)
                        ? (String) properties.get("nickname")
                        : "카카오사용자";

                // ⭐ 개발 환경: 카카오 이메일 대신 항상 임시 이메일 생성
                String email = "kakao_" + id + "@kakao.com";
                log.warn("⚠️ Kakao 개발환경: 임시 email 생성: {}", email);

                String picture = (properties != null)
                        ? (String) properties.get("profile_image")
                        : "";

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

                nameAttributeKey = "username";
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
                attributes.put("name", name); // ⭐ 추가!
                attributes.put("provider", "naver");
                attributes.put("email", email);

                nameAttributeKey = "username";
                log.info("✅ Naver 사용자 처리 완료");
            }

            // ⭐ Google
            else if ("google".equals(registrationId)) {
                log.info("🔍 Google 로그인 처리 시작");

                String email = (String) attributes.get("email");

                // ✅ null-safe: name이 null/blank면 기본값 부여
                String name = (String) attributes.get("name");
                if (name == null || name.isBlank()) {
                    name = "구글사용자";
                }

                String picture = (String) attributes.get("picture");

                String fixedUsername = email;   // Google은 email을 username으로 사용
                String fixedNickname = name;    // nickname은 절대 null이면 안 됨

                log.info("✅ Google 사용자: name={}, email={}", name, email);
                saveOAuth2User(fixedUsername, email, picture, name, "google", fixedNickname);

                attributes.put("username", fixedUsername);
                attributes.put("nickname", fixedNickname);
                attributes.put("name", name);
                attributes.put("provider", "google");
                attributes.put("email", email);

                nameAttributeKey = "username";
                log.info("✅ Google 사용자 처리 완료");
            }


            // ⭐ DB에서 사용자 조회하여 authorities 생성
            Users user = userRepository.findByUsername((String) attributes.get("username"))
                    .orElse(null);

            // ⭐ 권한(authorities) 생성
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            if (user != null && user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()));
                log.info("✅ 권한 설정: ROLE_{}", user.getRole().toString());
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
                log.warn("⚠️ 사용자 역할 없음 - 기본값 ROLE_STUDENT 설정");
            }

            log.info("✅ [SUCCESS] CustomOAuth2UserService.loadUser() 완료");

            return new DefaultOAuth2User(
                    authorities,
                    attributes,
                    nameAttributeKey);

        } catch (Exception e) {
            log.error("❌ [ERROR] CustomOAuth2UserService 중 오류: {}", e.getMessage());
            throw new OAuth2AuthenticationException("OAuth2 처리 중 오류: " + e.getMessage());
        }
    }

    private void saveOAuth2User(String username, String email, String profileImage,
                                String name, String provider, String nickname) {
        try {
            log.info("🔍 [DEBUG] saveOAuth2User 시작 - username: {}", username);

            // ✅ nickname/name null 방어 (DB not-null 대비)  ← 여기!!!
            if (nickname == null || nickname.isBlank()) {
                nickname = (name != null && !name.isBlank()) ? name : username;
            }
            if (name == null || name.isBlank()) {
                name = nickname;
            }

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

                if (user.getSchoolEmail() == null) {
                    user.setSchoolEmail(null);
                }

            } else {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
                String encodedPassword = (encoder != null)
                        ? encoder.encode("oauth_" + provider + "_" + System.currentTimeMillis())
                        : "oauth_" + provider + "_" + System.currentTimeMillis();

                user = Users.builder()
                        .username(username)
                        .nickname(nickname)   // ✅ 여기서 절대 null 아님
                        .email(email)
                        .name(name)           // ✅ 여기서도 절대 null 아님
                        .profileImageUrl(profileImage)
                        .oauthProvider(provider)
                        .oauthId(username)
                        .password(encodedPassword)
                        .role(Role.STUDENT)
                        .isActive(true)
                        .schoolEmail(null)
                        .isVerifiedStudent(false)
                        .build();
            }

            Users savedUser = userRepository.save(user);
            log.info("💾 사용자 정보 저장 완료: username={}, user_id={}, email={}, role={}",
                    username, savedUser.getUserId(), email, savedUser.getRole());

            // Student_profile 생성
            studentProfileService.createStudentProfile(savedUser.getUserId(), "", "", "");
        } catch (Exception e) {
            log.error("❌ [ERROR] 사용자 정보 저장 실패: {}", e.getMessage());
            throw new OAuth2AuthenticationException("사용자 저장 중 오류: " + e.getMessage());
        }
    }
}
