package com.StudyLink.www.service;

import com.StudyLink.www.dto.PaymentPendingRequest;
import com.StudyLink.www.dto.PaymentPendingResponse;
import net.minidev.json.JSONObject;

public interface PaymentService {

    PaymentPendingResponse createPendingPayment(Long productId, Long userId);

    JSONObject confirmPayment(String jsonBody);
}
