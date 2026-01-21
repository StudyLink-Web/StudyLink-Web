package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@Primary // ✅ 이 한 줄이 핵심
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String sub = oidcUser.getSubject(); // Google 고유 ID
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        String username = "google_" + sub;

        Users user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    Users newUser = Users.builder()
                            .username(username)
                            .email(email)
                            .name(name)
                            .oauthProvider("google")
                            .oauthId(sub)
                            .role(Role.STUDENT)
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        Map<String, Object> attributes = new HashMap<>(oidcUser.getClaims());
        attributes.put("username", user.getUsername());
        attributes.put("name", user.getName());
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
