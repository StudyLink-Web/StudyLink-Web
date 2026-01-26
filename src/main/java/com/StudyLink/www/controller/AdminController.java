package com.StudyLink.www.controller;

import com.StudyLink.www.dto.AdminPaymentDTO;
import com.StudyLink.www.entity.Payment;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.entity.Product;
import com.StudyLink.www.repository.ProductRepository;
import com.StudyLink.www.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final PaymentService paymentService;
    private final ProductRepository productRepository;

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
        return "/admin/paymentDetail";
    }
}
