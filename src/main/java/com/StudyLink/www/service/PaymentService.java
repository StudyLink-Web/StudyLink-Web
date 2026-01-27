package com.StudyLink.www.service;

import com.StudyLink.www.dto.*;
import com.StudyLink.www.entity.ExchangeStatus;
import com.StudyLink.www.entity.PaymentStatus;
import net.minidev.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PaymentService {

    PaymentPendingResponse createPendingPayment(int productId, Long userId);

    JSONObject confirmPayment(String jsonBody, Long userId);

    int insertExchangeRequest(ExchangeRequestDTO request, Long userId);

    Page<AdminPaymentDTO> search(PaymentStatus status, String method, String email, LocalDate startDate, LocalDate endDate, Pageable sortedPageable);

    AdminPaymentDetailDTO getPaymentDetail(Long id);

    Page<AdminExchangeRequestDTO> searchExchangeRequests(ExchangeStatus status, String email, LocalDate startDate, LocalDate endDate, String basis, Pageable pageable);

    AdminExchangeRequestDetailDTO getExchangeRequestDetail(Long id);
    void approve(long id);

    void reject(AdminExchangeRequestRejectDTO adminExchangeRequestRejectDTO);
}
