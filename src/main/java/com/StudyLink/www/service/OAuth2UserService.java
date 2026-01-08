package com.StudyLink.www.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 사용자 서비스
 * 소셜 로그인 사용자 정보 처리
 * - 카카오, 네이버, 구글 지원
 */
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    /**
     * OAuth2 사용자 정보 로드
     * @param userRequest - OAuth2 사용자 요청
     * @return OAuth2User - 처리된 사용자 정보
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 사용자 정보 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 로그인 제공자 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        System.out.println("🔐 OAuth2 로그인: " + registrationId);

        // 제공자별 사용자 정보 처리
        switch (registrationId) {
            case "kakao":
                return processKakaoUser(oAuth2User);
            case "naver":
                return processNaverUser(oAuth2User);
            case "google":
                return processGoogleUser(oAuth2User);
            default:
                return oAuth2User;
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

        // 사용자 정보 통합
        attributes.put("username", "kakao_" + id);
        attributes.put("name", nickname);
        attributes.put("email", email);
        attributes.put("picture", profileImage);
        attributes.put("provider", "kakao");

        System.out.println("✅ 카카오 사용자: " + nickname + " (" + email + ")");

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "id"
        );
    }

    /**
     * 네이버 사용자 정보 처리
     * API 응답: {resultcode, message, response: {id, name, email, profile_image}}
     */
    private OAuth2User processNaverUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String id = "";
        String name = "";
        String email = "";
        String profileImage = "";

        if (response != null) {
            id = (String) response.getOrDefault("id", "");
            name = (String) response.getOrDefault("name", "네이버사용자");
            email = (String) response.getOrDefault("email", "");
            profileImage = (String) response.getOrDefault("profile_image", "");
        }

        // 사용자 정보 통합
        attributes.put("username", "naver_" + id);
        attributes.put("name", name);
        attributes.put("email", email);
        attributes.put("picture", profileImage);
        attributes.put("provider", "naver");

        System.out.println("✅ 네이버 사용자: " + name + " (" + email + ")");

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "id"
        );
    }

    /**
     * 구글 사용자 정보 처리
     * API 응답: {sub, name, email, picture, locale, ...}
     */
    private OAuth2User processGoogleUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String sub = (String) attributes.getOrDefault("sub", "");
        String name = (String) attributes.getOrDefault("name", "구글사용자");
        String email = (String) attributes.getOrDefault("email", "");
        String picture = (String) attributes.getOrDefault("picture", "");

        // 사용자 정보 통합
        attributes.put("username", "google_" + sub);
        attributes.put("provider", "google");
        attributes.put("picture", picture);

        System.out.println("✅ 구글 사용자: " + name + " (" + email + ")");

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "sub"
        );
    }

    /**
     * 소셜 로그인 사용자 정보 저장 (데이터베이스)
     * @param username - 소셜 로그인 ID
     * @param email - 이메일
     * @param profileImage - 프로필 이미지 URL
     * @param provider - 제공자 (kakao, naver, google)
     *
     * TODO: 데이터베이스 저장 로직 구현 필요
     */
    private void saveOAuth2User(String username, String email, String profileImage, String provider) {
        // DB 저장 로직 (User 엔티티 및 Repository 필요)
        System.out.println("저장 중: " + username + " (" + provider + ")");
        // userRepository.save(new User(username, email, profileImage, provider));
    }
}