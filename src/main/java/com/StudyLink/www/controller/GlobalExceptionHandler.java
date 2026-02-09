package com.StudyLink.www.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest request, Exception ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "";

        // 사용자가 요청을 취소하여 발생하는 예외(Abort/NotUsable)는 무시하거나 가볍게 로그만 남김
        if (ex.getClass().getName().contains("ClientAbortException") ||
                ex instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException ||
                errorMessage.contains("중단되었습니다")) {
            log.debug("Client connection aborted: {}", errorMessage);
            return null;
        }

        log.error("Exception occurred: ", ex);

        // API 요청(/api/)인 경우 JSON 형태로 에러 응답
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", errorMessage));
        }

        // 일반 페이지 요청인 경우 에러 페이지 뷰 반환
        ModelAndView mav = new ModelAndView();
        mav.setViewName("error/500");
        mav.addObject("exception", ex);
        return mav;
    }

    /**
     * ✅ 정적 리소스/페이지 404 처리
     * - /api/** 는 JSON
     * - 웹 페이지 요청은 templates/error/404.html 로
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(HttpServletRequest request, NoResourceFoundException e) {

        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");

        // favicon.ico 같은건 조용히 무시
        if (uri != null && uri.contains("favicon.ico")) {
            return null;
        }

        boolean isApi = uri != null && uri.startsWith("/api/");
        boolean wantsHtml = (accept != null && accept.contains("text/html"));

        // ✅ API 요청이면 JSON 404 유지
        if (isApi || !wantsHtml) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", "Not Found: " + e.getResourcePath());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }

        // ✅ 브라우저 페이지 요청이면 404 페이지로
        ModelAndView mav = new ModelAndView();
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.setViewName("error/404"); // resources/templates/error/404.html
        mav.addObject("path", uri);
        return mav;
    }
}
