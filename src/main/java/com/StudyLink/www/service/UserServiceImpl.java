package com.StudyLink.www.service;

import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Users findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("식별자가 비어있습니다.");
        }

        return userRepository.findByNickname(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseGet(() -> userRepository.findByUsername(identifier)
                                .orElseThrow(() ->
                                        new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier))));
    }

    @Override
    @Transactional(readOnly = true)
    public Long findUserIdByIdentifier(String identifier) {
        return findByIdentifier(identifier).getUserId();
    }
}
