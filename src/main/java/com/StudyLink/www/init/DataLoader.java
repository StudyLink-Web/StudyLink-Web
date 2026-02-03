package com.StudyLink.www.init;

import com.StudyLink.www.entity.Product;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Subject;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.BoardRepository;
import com.StudyLink.www.repository.ProductRepository;
import com.StudyLink.www.repository.SubjectRepository;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {

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
    private final BoardRepository boardRepository;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(
                    Users.builder()
                            .email(adminEmail)
                            .password(passwordEncoder.encode(adminPassword))
                            .name(adminName)
                            .role(Role.ADMIN)
                            .nickname(adminNickname)
                            .username(adminUsername)
                            .build()
            );
        }

        Users admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Admin user not found: " + adminEmail));

        if (subjectRepository.count() == 0) {
            subjectRepository.save(Subject.builder().name("고등국어").color("#FFD7D7").build());
            subjectRepository.save(Subject.builder().name("독서").color("#FFF3B0").build());
            subjectRepository.save(Subject.builder().name("문학").color("#D4F1F4").build());
            subjectRepository.save(Subject.builder().name("화법과작문").color("#D6EAD8").build());
            subjectRepository.save(Subject.builder().name("언어와매체").color("#FDE2E4").build());
            subjectRepository.save(Subject.builder().name("영어").color("#C6DEF1").build());
            subjectRepository.save(Subject.builder().name("한국사").color("#FFF9C4").build());
            subjectRepository.save(Subject.builder().name("고등수학").color("#C3FDB8").build());
            subjectRepository.save(Subject.builder().name("수학Ⅰ").color("#E0BBE4").build());
            subjectRepository.save(Subject.builder().name("수학Ⅱ").color("#FFDAC1").build());
            subjectRepository.save(Subject.builder().name("미적분").color("#B5EAEA").build());
            subjectRepository.save(Subject.builder().name("확률과통계").color("#FF9AA2").build());
            subjectRepository.save(Subject.builder().name("기하").color("#FFB7B2").build());
            subjectRepository.save(Subject.builder().name("물리학Ⅰ").color("#FCE1E4").build());
            subjectRepository.save(Subject.builder().name("물리학Ⅱ").color("#E6F4F1").build());
            subjectRepository.save(Subject.builder().name("화학Ⅰ").color("#FFF1E6").build());
            subjectRepository.save(Subject.builder().name("화학Ⅱ").color("#D6EFFF").build());
            subjectRepository.save(Subject.builder().name("생명과학Ⅰ").color("#DEFDE0").build());
            subjectRepository.save(Subject.builder().name("생명과학Ⅱ").color("#E3F6F5").build());
            subjectRepository.save(Subject.builder().name("지구과학Ⅰ").color("#FBE8A6").build());
            subjectRepository.save(Subject.builder().name("지구과학Ⅱ").color("#F9F9F9").build());
        }

        String[] boardTitles = {
                "새 학기 캠퍼스 생활 꿀팁",
                "시험 기간 루틴 공유해요",
                "과제 효율적으로 끝내는 방법",
                "동아리 선택할 때 체크할 점",
                "교수님께 메일 보낼 때 예절",
                "팀플 스트레스 줄이는 법",
                "중간고사 준비 전략",
                "기말고사 대비 공부법",
                "학교 근처 맛집 추천",
                "시간 관리 노하우"
        };

        String[] boardContents = {
                "새 학기 시작 전에 알아두면 좋은 캠퍼스 생활 팁을 정리해봤어요.",
                "시험 기간에는 무리하지 말고 꾸준히 페이스를 유지하는 게 제일 중요하더라구요.",
                "과제는 미루지 말고 작은 단위로 나눠서 진행하면 훨씬 수월해요.",
                "동아리는 관심 분야랑 활동 시간을 꼭 확인하고 들어가면 후회가 적어요.",
                "교수님께 메일은 제목/인사/요지/질문 순서로 정리하면 답장도 빨라요.",
                "팀플은 역할 분담을 확실히 하고, 중간중간 공유하는 게 핵심이에요.",
                "중간고사는 범위 정리 후 기출/요약 노트 중심으로 공부하면 효율적이에요.",
                "기말고사는 누적이라 복습이 중요해서, 주차별로 정리해두면 편해요.",
                "학교 주변에 가성비 좋은 곳이 은근 많아서 몇 군데 추천해요.",
                "하루 계획은 지킬 수 있는 수준으로 잡고 꾸준히 실천하는 게 좋아요."
        };

        if (boardRepository.count() == 0) {
            String sql = """
                INSERT INTO board
                (title, content, writer, user_id, view_count, department, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;

            for (int i = 0; i < 20; i++) {
                jdbcTemplate.update(
                        sql,
                        boardTitles[i % boardTitles.length] + " #" + (i + 1),
                        boardContents[i % boardContents.length],
                        admin.getUsername(),
                        admin.getUserId(),
                        0,
                        "관리자"
                );
            }
        }

        String[] communityTitles = {
                "취업 준비 같이 해요",
                "요즘 학교 분위기 어떤가요?",
                "스터디원 모집합니다",
                "팀플 구합니다",
                "교양 과목 추천해주세요",
                "자취생 생활 팁 공유",
                "시험기간 멘탈 관리법",
                "전공 수업 질문 있어요",
                "면접 후기 공유합니다",
                "방학 계획 어떻게 세우세요?"
        };

        String[] communityContents = {
                "요즘 취업 준비하면서 느낀 점이 많아서 같이 이야기해보고 싶어요.",
                "최근 학교 분위기 어떤가요? 다들 어떻게 지내는지 궁금해요.",
                "같이 공부할 스터디원 모집합니다! 관심 있으시면 댓글 주세요.",
                "팀플 같이 하실 분 구해요. 역할 분담 깔끔하게 하면서 진행해요.",
                "부담 없이 들을 수 있는 교양 과목 추천 부탁드려요!",
                "자취하면서 도움 됐던 꿀팁들 공유해봅니다.",
                "시험기간 멘탈 관리 어떻게 하시나요? 꿀팁 있으면 알려주세요.",
                "전공 수업 듣다가 이해 안 되는 부분이 있어서 질문 올립니다.",
                "최근 면접 보고 왔는데, 느낀 점 정리해서 공유해요.",
                "다들 방학 계획 어떻게 세우고 계신가요? 같이 아이디어 나눠요."
        };

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM community", Long.class) == 0) {
            String communitySql = """
                INSERT INTO community
                (title, content, writer, email, role, user_id, department, read_count, cmt_qty, file_qty, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;

            for (int i = 0; i < 20; i++) {
                jdbcTemplate.update(
                        communitySql,
                        communityTitles[i % communityTitles.length] + " #" + (i + 1),
                        communityContents[i % communityContents.length],
                        admin.getUsername(),
                        admin.getEmail(),
                        admin.getRole().name(),
                        admin.getUserId(),
                        "자유게시판",
                        0,
                        0,
                        0
                );
            }
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder().productName("Standard").productPrice(19900).isActive(true).build());
            productRepository.save(Product.builder().productName("Premium").productPrice(49900).isActive(true).build());
        }
    }
}
