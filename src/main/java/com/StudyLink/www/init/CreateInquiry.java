package com.StudyLink.www.init;

import com.StudyLink.www.entity.Inquiry;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.InquiryRepository;
import com.StudyLink.www.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateInquiry {
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final Random random = new Random();

    // 문의 유형
    private final List<String> types = List.of("계정", "결제", "기능문의", "오류/버그", "기타");

    // 공개 여부
    private final List<String> visibility = List.of("Y", "N");

    private final List<String> status = List.of("PENDING", "READY", "COMPLETE");

    // 제목과 내용 매칭
    private final List<TitleContentPair> titleContentPairs = List.of(
            new TitleContentPair("문의드립니다.", "서비스 이용 중 문제가 발생했습니다."),
            new TitleContentPair("문제 발생", "결제가 정상적으로 처리되지 않습니다."),
            new TitleContentPair("질문 있습니다.", "기능 사용 방법이 궁금합니다."),
            new TitleContentPair("버그 신고", "앱 실행 중 오류가 발생합니다."),
            new TitleContentPair("사용 관련 문의", "기타 문의 사항입니다.")
    );

    public void createRandomInquiry() {
        try {
            long userId = (long) (Math.random() * 90) + 5;
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다."));

            String choose = randomFromList(types);
            String isPublic = randomFromList(visibility);
            String password = isPublic.equals("N") ? "1234" : null; // 비밀방이면 비밀번호

            // 제목+내용 매칭
            TitleContentPair pair = randomFromList(titleContentPairs);
            String title = pair.title();
            String userContent = pair.content();

            LocalDateTime createdAt = randomCreatedAt();
            Inquiry inquiry = Inquiry.builder()
                    .title(title)
                    .writerEmail(user.getUsername())
                    .choose(choose)
                    .isPublic(isPublic)
                    .password(password)
                    .userContent(userContent)
                    .status(randomFromList(status))
                    .createdAt(createdAt)
                    .build();

            inquiryRepository.save(inquiry);
            log.info(">>> 랜덤 문의 생성 완료");

        } catch (Exception e) {
            log.error("랜덤 문의 생성 실패", e);
        }
    }

    private <T> T randomFromList(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    // 제목+내용을 묶는 record
    private record TitleContentPair(String title, String content) {}

    private LocalDateTime randomCreatedAt() {
        int daysBack = random.nextInt(30);
        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);
        int seconds = random.nextInt(60);
        return LocalDateTime.now()
                .minusDays(daysBack)
                .minusHours(hours)
                .minusMinutes(minutes)
                .minusSeconds(seconds);
    }
}
