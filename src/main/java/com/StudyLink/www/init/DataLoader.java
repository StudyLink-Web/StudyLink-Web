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
            subjectRepository.save(Subject.builder().name("고등국어").color("#FFD7D7").build());       // 연핑크
            subjectRepository.save(Subject.builder().name("독서").color("#FFF3B0").build());          // 연노랑
            subjectRepository.save(Subject.builder().name("문학").color("#D4F1F4").build());          // 연하늘
            subjectRepository.save(Subject.builder().name("화법과작문").color("#D6EAD8").build());    // 연초록
            subjectRepository.save(Subject.builder().name("언어와매체").color("#FDE2E4").build());    // 연분홍
            subjectRepository.save(Subject.builder().name("영어").color("#C6DEF1").build());          // 하늘색
            subjectRepository.save(Subject.builder().name("한국사").color("#FFF9C4").build());        // 연노랑
            subjectRepository.save(Subject.builder().name("고등수학").color("#C3FDB8").build());      // 연녹색
            subjectRepository.save(Subject.builder().name("수학Ⅰ").color("#E0BBE4").build());         // 연보라
            subjectRepository.save(Subject.builder().name("수학Ⅱ").color("#FFDAC1").build());         // 밝은 살구색
            subjectRepository.save(Subject.builder().name("미적분").color("#B5EAEA").build());         // 연청록
            subjectRepository.save(Subject.builder().name("확률과통계").color("#FF9AA2").build());     // 연분홍
            subjectRepository.save(Subject.builder().name("기하").color("#FFB7B2").build());           // 연핑크
            subjectRepository.save(Subject.builder().name("물리학Ⅰ").color("#FCE1E4").build());       // 아주 연한 분홍
            subjectRepository.save(Subject.builder().name("물리학Ⅱ").color("#E6F4F1").build());       // 연민트
            subjectRepository.save(Subject.builder().name("화학Ⅰ").color("#FFF1E6").build());          // 크림색
            subjectRepository.save(Subject.builder().name("화학Ⅱ").color("#D6EFFF").build());          // 연하늘
            subjectRepository.save(Subject.builder().name("생명과학Ⅰ").color("#DEFDE0").build());      // 연연두
            subjectRepository.save(Subject.builder().name("생명과학Ⅱ").color("#E3F6F5").build());      // 연민트
            subjectRepository.save(Subject.builder().name("지구과학Ⅰ").color("#FBE8A6").build());      // 연노랑
            subjectRepository.save(Subject.builder().name("지구과학Ⅱ").color("#F9F9F9").build());      // 거의 흰색
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