// CustomOidcUserService

package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        final String sub = oidcUser.getSubject(); // Google 고유 ID
        final String email = oidcUser.getEmail();
        final String rawName = oidcUser.getFullName();

        // ✅ null-safe: name fallback (재할당 대신 새 변수)
        final String finalName = (rawName == null || rawName.isBlank()) ? "구글사용자" : rawName;

        final String username = "google_" + sub;

        Users user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> {
                            // ✅ 기존 email 계정(estelle)에 Google OAuth 연결 (INSERT X)
                            existing.setOauthProvider("google");
                            existing.setOauthId(sub);

                            // nickname UNIQUE라서 충돌 방지용: 비어있을 때만 채우기
                            if (existing.getNickname() == null || existing.getNickname().isBlank()) {
                                String base = finalName;
                                if (base == null || base.isBlank()) base = "googleUser";
                                String suffix = sub.length() > 8 ? sub.substring(sub.length() - 8) : sub;
                                existing.setNickname(base + "_" + suffix);
                            }

                            // password NOT NULL 방어: 기존 계정 password가 null일 때만 채우기
                            if (existing.getPassword() == null || existing.getPassword().isBlank()) {
                                String rawPw = "oauth_google_" + sub + "_" + System.currentTimeMillis();
                                existing.setPassword(passwordEncoder.encode(rawPw));
                            }

                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> {
                            // ✅ username도 없고 email도 없을 때만 신규 생성
                            String base = finalName;
                            if (base == null || base.isBlank()) base = "googleUser";
                            String suffix = sub.length() > 8 ? sub.substring(sub.length() - 8) : sub;
                            String nickname = base + "_" + suffix;

                            String rawPw = "oauth_google_" + sub + "_" + System.currentTimeMillis();
                            String encodedPw = passwordEncoder.encode(rawPw);

                            Users newUser = Users.builder()
                                    .username(username)
                                    .email(email)
                                    .name(finalName)
                                    .nickname(nickname)
                                    .password(encodedPw)
                                    .oauthProvider("google")
                                    .oauthId(sub)
                                    .role(Role.STUDENT)
                                    .isActive(true)
                                    .build();

                            return userRepository.save(newUser);
                        })
                );


        Map<String, Object> attributes = new HashMap<>(oidcUser.getClaims());
        attributes.put("username", user.getUsername());
        attributes.put("name", user.getName());
        attributes.put("nickname", user.getNickname());
        attributes.put("role", user.getRole());

        return new DefaultOidcUser(
                Collections.singleton(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ),
                oidcUser.getIdToken(),
                new OidcUserInfo(attributes),
                "username"
        );
    }
}
