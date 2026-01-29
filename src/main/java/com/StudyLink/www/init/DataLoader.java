package com.StudyLink.www.init;

import com.StudyLink.www.entity.Product;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Subject;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.ProductRepository;
import com.StudyLink.www.repository.SubjectRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {
    // subject 초기 데이터 삽입 용
    // 서버 실행시 자동 실행
    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.nickname}")
    private String adminNickname;

    @Value("${admin.username}")
    private String adminUsername;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubjectRepository subjectRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // 초기 데이터 세팅 필요시 여기에 추가

        // 1. 관리자 계정 자동 생성
        if (!userRepository.existsByEmail(adminEmail)) {
            Users admin = Users.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    .role(Role.ADMIN)
                    .nickname(adminNickname)
                    .username(adminUsername)
                    .build();
            userRepository.save(admin);
        }

        // 과목 초기 데이터
        if(subjectRepository.count() == 0) {
            subjectRepository.save(Subject.builder().name("고등국어").build());
            subjectRepository.save(Subject.builder().name("독서").build());
            subjectRepository.save(Subject.builder().name("문학").build());
            subjectRepository.save(Subject.builder().name("화법과작문").build());
            subjectRepository.save(Subject.builder().name("언어와매체").build());
            subjectRepository.save(Subject.builder().name("영어").build());
            subjectRepository.save(Subject.builder().name("한국사").build());
            subjectRepository.save(Subject.builder().name("고등수학").build());
            subjectRepository.save(Subject.builder().name("수학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("수학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("미적분").build());
            subjectRepository.save(Subject.builder().name("확률과통계").build());
            subjectRepository.save(Subject.builder().name("기하").build());
            subjectRepository.save(Subject.builder().name("물리학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("물리학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("화학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("화학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("생명과학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("생명과학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("지구과학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("지구과학Ⅱ").build());
        }

        // 상품 초기 데이터
        if (productRepository.count() == 0) {
            productRepository.save(
                    Product.builder()
                            .productName("Standard")
                            .productPrice(19900)
                            .isActive(true)
                            .build()
            );

            productRepository.save(
                    Product.builder()
                            .productName("Premium")
                            .productPrice(49900)
                            .isActive(true)
                            .build()
            );
        }
    }
}