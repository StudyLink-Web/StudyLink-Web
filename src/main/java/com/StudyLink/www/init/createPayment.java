package com.StudyLink.www.init;

import com.StudyLink.www.dto.PaymentPendingResponse;
import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.repository.PaymentRepository;
import com.StudyLink.www.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class createPayment {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    private final List<String> method = List.of("카드", "계좌이체", "간편결제");

    // 반복문에서 호출할 때, 한 건씩 안전하게 저장
    public void createRandomPayments(int count) {
        for (int i = 0; i < count; i++) {
            try {
                createRandomPayment();
            } catch (Exception e) {
                log.error(">>> 결제 더미 생성 실패, i={}", i, e);
            }
        }
    }

    // 건별 트랜잭션
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRandomPayment() {
        int productId = random.nextInt(2) + 1;
        long userId = random.nextLong(5, 95); // 5~94
        PaymentPendingResponse response = paymentService.createPendingPayment(productId, userId);
        Payment payment = paymentRepository.findByOrderId(response.getOrderId());

        // 상태 변경
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setPaymentKey(UUID.randomUUID().toString());
        payment.setMethod(randomFromList(method));
        payment.setCurrency("KRW");

        LocalDateTime createdAt = randomCreatedAt();
        payment.setRequestedAt(createdAt);
        payment.setApprovedAt(createdAt);

        paymentRepository.save(payment);

        log.info(">>> 결제 더미 생성 완료: orderId={}", payment.getOrderId());
    }

    private <T> T randomFromList(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

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
