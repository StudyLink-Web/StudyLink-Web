package com.StudyLink.www.service;

import com.StudyLink.www.dto.ExchangeRequestDTO;
import com.StudyLink.www.dto.PaymentPendingRequest;
import com.StudyLink.www.dto.PaymentPendingResponse;
import net.minidev.json.JSONObject;

public interface PaymentService {

    PaymentPendingResponse createPendingPayment(int productId, Long userId);

    JSONObject confirmPayment(String jsonBody, Long userId);

    int insertExchangeRequest(ExchangeRequestDTO request, Long userId);
}
