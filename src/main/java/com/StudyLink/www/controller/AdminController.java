package com.StudyLink.www.controller;

import com.StudyLink.www.dto.*;
import com.StudyLink.www.entity.ExchangeStatus;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final PaymentService paymentService;

    @GetMapping("/admin")
    public void admin(Model model) {
        model.addAttribute("currentMenu", "admin");
    };

    @GetMapping("/payment")
    public void payment(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(defaultValue = "desc") String sort,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        method = (method != null && method.isBlank()) ? null : method;
        email = (email != null && email.isBlank()) ? null : email;

        Sort sortOption = sort.equals("asc")
                ? Sort.by("approvedAt").ascending()
                : Sort.by("approvedAt").descending();

        Pageable sortedPageable =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<AdminPaymentDTO> page =
                paymentService.search(status, method, email, startDate, endDate, sortedPageable);

        model.addAttribute("adminPaymentDTOList", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("status", status);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("method", method);
        model.addAttribute("email", email);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);
        model.addAttribute("currentMenu", "payment");
    }

    @GetMapping("/payment/{id}")
    public String paymentDetail(@PathVariable Long id, Model model) {
        AdminPaymentDetailDTO adminPaymentDetail = paymentService.getPaymentDetail(id);

        model.addAttribute("adminPaymentDetail", adminPaymentDetail);
        model.addAttribute("currentMenu", "payment");
        return "/admin/paymentDetail";
    }

    @GetMapping("/exchange")
    public void exchange(
            @RequestParam(required = false) ExchangeStatus status,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(defaultValue = "createdAt") String basis,
            @RequestParam(defaultValue = "desc") String sort,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        email = (email != null && email.isBlank()) ? null : email;

        Sort sortOption = null;
        if (basis.equals("createdAt")) {
            sortOption = sort.equals("asc")
                    ? Sort.by("createdAt").ascending()
                    : Sort.by("createdAt").descending();
        } else {
            sortOption = sort.equals("asc")
                    ? Sort.by("processedAt").ascending()
                    : Sort.by("processedAt").descending();
        }

        Pageable sortedPageable =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<AdminExchangeRequestDTO> page =
                paymentService.searchExchangeRequests(status, email, startDate, endDate, basis, sortedPageable);

        model.addAttribute("adminExchangeRequestDTOList", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ExchangeStatus.values());
        model.addAttribute("email", email);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("basis", basis);
        model.addAttribute("sort", sort);
        model.addAttribute("currentMenu", "exchange");
    };

    @GetMapping("/exchange/{id}")
    public String exchangeDetail(@PathVariable Long id, Model model) {
        AdminExchangeRequestDetailDTO adminExchangeRequestDetail = paymentService.getExchangeRequestDetail(id);

        model.addAttribute("adminExchangeRequestDetail", adminExchangeRequestDetail);
        model.addAttribute("currentMenu", "exchange");
        return "/admin/exchangeDetail";
    }

    @GetMapping("/exchangeApprove/{id}")
    public ResponseEntity<Void> approveExchange(@PathVariable long id) {
        paymentService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/exchangeReject")
    public ResponseEntity<Void> exchangeReject(@RequestBody AdminExchangeRequestRejectDTO adminExchangeRequestRejectDTO) {
        paymentService.reject(adminExchangeRequestRejectDTO);
        return ResponseEntity.ok().build();
    }
}
