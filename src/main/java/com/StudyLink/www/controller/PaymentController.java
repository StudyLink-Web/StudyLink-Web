package com.StudyLink.www.controller;

import com.StudyLink.www.dto.ExchangeRequestDTO;
import com.StudyLink.www.dto.PaymentPendingRequest;
import com.StudyLink.www.dto.PaymentPendingResponse;
import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.entity.Product;
import com.StudyLink.www.repository.ProductRepository;
import com.StudyLink.www.service.PaymentService;
import com.StudyLink.www.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;
    private final ProductRepository productRepository;


    // ================================================= 결제 =================================================
    // 페이지 이동
    @GetMapping("/payment")
    public void payment(){};

    // 페이지 이동
    @GetMapping("/success")
    public void success(){};

    // 페이지 이동
    @GetMapping("/fail")
    public void fail(){};

    // pending
    // 결제 대기상태
    // 결제창 진입시 결제 대기 주문 생성
    // 1. productId로 상품 조회
    // 2. orderId 생성
    // 3. payment 상태 = PENDING
    // 4. 금액 확정
    // 5. DB 저장
    // 6. orderId, 금액, 상품명 반환
    @PostMapping("/pending")
    public ResponseEntity<?> createPendingPayment(@RequestBody PaymentPendingRequest request,
                                                  Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            userId = userService.findUserIdByUsername(username);
        }

        try {
            PaymentPendingResponse response = paymentService.createPendingPayment(request.getProductId(), userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    // 결제 승인
    // 성공시 PENDING -> SUCCESS
    // 실패시 PENDING -> FAIL
    // 결제 요청 금액이 DB 데이터와 같은지 확인하기 (금액 변조여부 탐지)
    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirm(@RequestBody String jsonBody, Authentication authentication) throws Exception {
        try {
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                String username = authentication.getName();
                userId = userService.findUserIdByUsername(username);
            }

            JSONObject response = paymentService.confirmPayment(jsonBody, userId);
            JSONObject jsonObject = (JSONObject) response.get("jsonObject");
            int code = ((Number) response.get("code")).intValue();
            return ResponseEntity
                    .status(HttpStatus.valueOf(code))
                    .body(jsonObject);
        } catch (Exception e) {
            // 로깅 및 에러 응답 처리
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "결제 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorObj);
        }
    }



    // =============================================== 포인트 환전 =============================================
    // 페이지 이동
    @GetMapping("/exchange")
    public void exchange(){};

    @PostMapping("/exchange")
    public ResponseEntity<String> exchange(@RequestBody ExchangeRequestDTO request, Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            userId = userService.findUserIdByUsername(username);
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }

        int isOk = paymentService.insertExchangeRequest(request, userId);

        return isOk == 1 ? ResponseEntity.ok("OK") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
