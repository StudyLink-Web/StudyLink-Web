package com.StudyLink.www.controller;

import com.StudyLink.www.dto.*;
import com.StudyLink.www.entity.ExchangeStatus;
import com.StudyLink.www.entity.PaymentStatus;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.service.InquiryService;
import com.StudyLink.www.service.PaymentService;
import com.StudyLink.www.service.UserService;
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
    private final UserService userService;
    private final InquiryService inquiryService;

    @GetMapping("/admin")
    public void admin(Model model) {
        // 일일 통계 데이터 조회
        // 오늘 결제 건수, 오늘 결제 금액, 오늘 환전 요청 건수, 오늘 환전 금액, 오늘 신규 가입자 수
        int todayPaymentCount = paymentService.getTodayPaymentCount();
        long todayPaymentAmount = paymentService.getTodayPaymentAmount();
        int todayExchangeRequestCount = paymentService.getTodayExchangeRequestCount();
        long todayExchangeAmount = paymentService.getTodayExchangeAmount();
        int todayNewUsers = userService.getTodayNewUserCount();

        // 모델에 데이터 담기
        model.addAttribute("todayPaymentCount", todayPaymentCount); // 오늘 결제 건수 (건)
        model.addAttribute("todayPaymentAmount", todayPaymentAmount); // 오늘 결제 금액 합계 (원)
        model.addAttribute("todayExchangeRequestCount", todayExchangeRequestCount); // 오늘 환전 요청 건수 (건)
        model.addAttribute("todayExchangeAmount", todayExchangeAmount); // 오늘 환전 요청 금액 합계 (원)
        model.addAttribute("todayNewUsers", todayNewUsers); // 오늘 신규 가입자 수 (명)

        // 그래프 데이터 받아오기
        UserChartDTO userChartDTO = userService.getUserChart();
        PaymentChartDTO paymentChartDTO = paymentService.getPaymentChart();
        ExchangeChartDTO exchangeChartDTO = paymentService.getExchangeChart();

        // 모델에 데이터 담기
        model.addAttribute("userChartDTO", userChartDTO); // 회원 차트 데이터
        model.addAttribute("paymentChartDTO", paymentChartDTO); // 결제 차트 데이터
        model.addAttribute("exchangeChartDTO", exchangeChartDTO); // 환전 차트 데이터

        model.addAttribute("currentMenu", "admin");
    };

    @GetMapping("/user")
    public void user(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "desc") String sort,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        email = (email != null && email.isBlank()) ? null : email;

        Role roleEnum = null;
        if (role != null && !role.isBlank()) {
            roleEnum = Role.valueOf(role.toUpperCase());
        }

        Sort sortOption = sort.equals("asc")
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<AdminUserDTO> page = userService.search(email, roleEnum, isActive, startDate, endDate, sortedPageable);

        model.addAttribute("usersList", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("isActive", isActive);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);
        model.addAttribute("currentMenu", "user");
    }

    @GetMapping("/user/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        UsersDTO usersDTO = userService.getUserDetail(id);

        model.addAttribute("adminUserDetail", usersDTO);
        model.addAttribute("currentMenu", "user");
        return "/admin/userDetail";
    }

    @GetMapping("/payment")
    public void payment(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "desc") String sort,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        method = (method != null && method.isBlank()) ? null : method;
        email = (email != null && email.isBlank()) ? null : email;

        Sort sortOption = sort.equals("asc")
                ? Sort.by("approvedAt").ascending()
                : Sort.by("approvedAt").descending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<AdminPaymentDTO> page = paymentService.search(status, method, email, startDate, endDate, sortedPageable);

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "createdAt") String basis,
            @RequestParam(defaultValue = "desc") String sort,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
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

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<AdminExchangeRequestDTO> page = paymentService.searchExchangeRequests(status, email, startDate, endDate,
                basis, sortedPageable);

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
    public ResponseEntity<Void> exchangeReject(
            @RequestBody AdminExchangeRequestRejectDTO adminExchangeRequestRejectDTO) {
        paymentService.reject(adminExchangeRequestRejectDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notice")
    public String notice(Model model) {
        log.info("📂 [AdminController] 공지 관리 페이지(/admin/notice) 요청 수신");
        model.addAttribute("currentMenu", "notice");
        return "admin/notice";
    }

    @GetMapping("inquiry")
    public void exchange(
            @RequestParam(required = false) String choose,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "desc") String sort,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        choose = (choose != null && choose.isBlank()) ? null : choose;
        status = (status != null && status.isBlank()) ? null : status;
        username = (username != null && username.isBlank()) ? null : username;

        Sort sortOption = null;
        sortOption = sort.equals("asc")
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<AdminInquiryDTO> page = inquiryService.searchInquiryList(choose, status, username, startDate, endDate, sortedPageable);
        log.info(">>> adminInquiryDTOList {}", page.getContent());
        model.addAttribute("adminInquiryDTOList", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("choose", choose);
        model.addAttribute("status", status);
        model.addAttribute("username", username);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);
        model.addAttribute("currentMenu", "inquiry");
    };

    @GetMapping("/inquiry/{id}")
    public String inquiryDetail(@PathVariable Long id, Model model) {
        InquiryDTO inquiryDTO = inquiryService.findById(id);

        model.addAttribute("inquiry", inquiryDTO);
        model.addAttribute("currentMenu", "inquiry");
        return "/admin/inquiryDetail";
    }

    @PostMapping("/inquiryAnswer")
    public ResponseEntity<Void> inquiryAnswer(
            @RequestParam Long qno,
            @RequestParam String adminContent
    ) {
        log.info(">>> qno {}", qno);
        log.info(">>> adminContent {}", adminContent);
        inquiryService.answer(qno, adminContent);
        return ResponseEntity.ok().build();
    }
}
