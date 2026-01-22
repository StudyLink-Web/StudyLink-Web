package com.StudyLink.www.service;

import com.StudyLink.www.dto.PaymentPendingResponse;
import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.entity.Product;
import com.StudyLink.www.repository.ExchangeRequestRepository;
import com.StudyLink.www.repository.PaymentRepository;
import com.StudyLink.www.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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
    public PaymentPendingResponse createPendingPayment(Long productId, Long userId) {
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
    public JSONObject confirmPayment(String jsonBody) {
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

            // 결과 반환용 JSONObject 생성
            JSONObject result = new JSONObject();
            result.put("jsonObject", jsonObject);
            result.put("isSuccess", isSuccess);
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
}
