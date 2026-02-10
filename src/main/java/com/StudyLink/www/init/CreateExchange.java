package com.StudyLink.www.init;

import com.StudyLink.www.entity.ExchangeRequest;
import com.StudyLink.www.entity.ExchangeStatus;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.ExchangeRequestRepository;
import com.StudyLink.www.repository.PaymentRepository;
import com.StudyLink.www.repository.UserRepository;
import com.StudyLink.www.service.PaymentService;
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
public class CreateExchange {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final ExchangeRequestRepository exchangeRequestRepository;

    private final List<String> account = List.of("01235670562", "57561027468", "212313857503", "4834123708953", "45378701222045"
            , "1230789075037", "7512453123090", "4560787506770", "4560420794", "78094530126", "99753102374", "04708078123", "456012966450");
    private final List<String> bankName = List.of("국민은행", "기업은행", "신한은행", "우리은행", "하나은행", "카카오뱅크", "토스뱅크");
    private final List<String> names = List.of("홍길동", "김철수", "이영희", "박민수", "최지우");

    public void createRandomExchange() {
        try {
            Users user = userRepository.findById((long)(Math.random() * 90) + 5).orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다."));
            LocalDateTime createdAt = randomCreatedAt();
            ExchangeRequest exchangeRequest = ExchangeRequest.builder()
                    .user(user)
                    .point((int)(Math.random() * 50) * 1000 + 10000)
                    .status(randomFromEnum(ExchangeStatus.class))
                    .createdAt(createdAt)
                    .account(randomFromList(account))
                    .bankName(randomFromList(bankName))
                    .accountHolder(randomFromList(names))
                    .build();
            if (exchangeRequest.getStatus() != ExchangeStatus.PENDING) {
                exchangeRequest.setProcessedAt(createdAt);
            }
            exchangeRequestRepository.save(exchangeRequest);
        } catch (Exception e) {
            log.error("환전 더미 생성 실패", e);
        }
    }

    private <T> T randomFromList(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <T extends Enum<T>> T randomFromEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[random.nextInt(values.length)];
    }

    // 랜덤 생성일 생성 (오늘 기준 최근 30일 내)
    private LocalDateTime randomCreatedAt() {
        int daysBack = random.nextInt(30); // 0~29일 전
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
