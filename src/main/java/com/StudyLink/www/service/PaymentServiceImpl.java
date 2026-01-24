package com.StudyLink.www.service;

import com.StudyLink.www.dto.PaymentPendingResponse;
import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.entity.Product;
import com.StudyLink.www.repository.ExchangeRequestRepository;
import com.StudyLink.www.repository.PaymentRepository;
import com.StudyLink.www.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${toss.secret-key}")
    private String secretKey;

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;

    @Override
    public PaymentPendingResponse createPendingPayment(int productId, Long userId) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getIsActive()) {
            throw new IllegalStateException("비활성화된 상품입니다.");
        }

        // orderId 생성
        String orderId = UUID.randomUUID().toString();

        // 결제(PENDING) 생성
        Payment payment = Payment.builder()
                .orderId(orderId)
                .productId(product.getProductId())
                .userId(userId)
                .amount(product.getProductPrice())
                .status(PaymentStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 반환할 DTO 생성
        return PaymentPendingResponse.builder()
                .orderId(orderId)
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .currency("KRW")
                .build();
    }

    @Override
    public JSONObject confirmPayment(String jsonBody, Long userId) {
        JSONParser parser = new JSONParser();
        try {
            String orderId;
            String amount;
            String paymentKey;

            // 클라이언트에서 받은 JSON 요청 바디 파싱
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");

            // 데이터 검사
            Payment payment = paymentRepository.findByOrderId(orderId);

            // 유효한 결제인지 검사
            if (payment == null) {
                throw new SecurityException("존재하지 않는 주문");
            }

            PaymentStatus status = payment.getStatus();

            if (status == PaymentStatus.APPROVED) {
                // 이미 승인 완료 → 멱등 응답
                // 네트워크 장애, 서버다운 등의 이유로 같은 결제에 대해 여러번 요청이 갈 수 있음
                // 최초 요청은 승인 -> 그 이후는 차단
                return alreadyApprovedServiceResponse(payment);
            }

            if (status == PaymentStatus.FAILED) {
                throw new IllegalStateException("결제 승인 실패");
            }

            if (status == PaymentStatus.CANCELED) {
                throw new IllegalStateException("결제 취소");
            }

            if (status == PaymentStatus.REQUESTED) {
                throw new IllegalStateException("결제 처리중");
            }

            // 본인 결제인지 검사
            if (!(userId).equals(payment.getUserId())) {
                throw new SecurityException("주문 소유자 불일치");
            }

            int reqAmount;
            try {
                reqAmount = Integer.parseInt(amount);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("금액 형식 오류");
            }

            if (payment.getAmount() != reqAmount) {
                throw new IllegalStateException("결제 금액 위조 감지");
            }

            JSONObject obj = new JSONObject();
            obj.put("orderId", orderId);
            obj.put("amount", amount);
            obj.put("paymentKey", paymentKey);

            // 인증 헤더 생성
            String widgetSecretKey = secretKey;
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
            String authorizations = "Basic " + new String(encodedBytes);

            // 결제 승인 API 호출
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

            // 응답 파싱
            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            responseStream.close();

            // 추후 환불 기능이 생기면 더 상세히 분리
            if (isSuccess) {
                // 결제 승인 성공 로직

                // DB payment REQUESTED -> SUCCESS로 변경
                payment.setStatus(PaymentStatus.APPROVED);

                // paymentKey 저장
                payment.setPaymentKey((String) jsonObject.get("paymentKey"));

                // method(결제 수단) 저장
                payment.setMethod((String) jsonObject.get("method"));

                // currency(통화) 저장
                payment.setCurrency((String) jsonObject.get("currency"));

                // 결제 요청 시간 저장
                String requestedAtStr = (String) jsonObject.get("requestedAt");
                OffsetDateTime odt = OffsetDateTime.parse(requestedAtStr);
                LocalDateTime requestedAt = odt.toLocalDateTime();
                payment.setRequestedAt(requestedAt);

                // 결제 승인 시각 저장
                String approvedAtStr = (String) jsonObject.get("approvedAt");
                odt = OffsetDateTime.parse(approvedAtStr);
                LocalDateTime approvedAt = odt.toLocalDateTime();
                payment.setApprovedAt(approvedAt);

                // 저장
                paymentRepository.save(payment);
            } else {
                // 결제 승인 실패 로직
                // DB payment REQUESTED -> FAILED로 변경
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(LocalDateTime.now());

                // 저장
                paymentRepository.save(payment);
            }


            // 결과 반환용 JSONObject 생성
            JSONObject result = new JSONObject();
            result.put("jsonObject", jsonObject);
            result.put("code", code);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject errorResult = new JSONObject();
            errorResult.put("isSuccess", false);
            errorResult.put("message", e.getMessage());
            errorResult.put("code", 500);
            return errorResult;
        }
    }

    // 이미 승인된 결제에 대한 응답 -> 정상적인 요청과 같은 응답으로 반환
    private JSONObject alreadyApprovedServiceResponse(Payment payment) {
        JSONObject body = new JSONObject();
        String productName = productRepository.findById(payment.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다.")).getProductName();
        body.put("orderName", productName);
        body.put("orderId", payment.getOrderId());
        body.put("totalAmount", payment.getAmount());
        body.put("approvedAt", payment.getApprovedAt().toString());
        body.put("method", payment.getMethod());
        body.put("status", payment.getStatus());

        JSONObject result = new JSONObject();
        result.put("jsonObject", body);
        result.put("code", 200);
        return result;
    }
}
