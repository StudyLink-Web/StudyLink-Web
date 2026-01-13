package com.StudyLink.www.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(jakarta.servlet.http.HttpServletRequest request, Exception ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "";
        
        // 리소스 미발견(NoResourceFoundException) 처리 (favicon.ico 등)
        if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            if (errorMessage.contains("favicon.ico")) {
                return null; // 파비콘 누락은 로그 없이 무시
            }
            log.warn("Static resource not found: {}", errorMessage);
            return null;
        }

        // 사용자가 요청을 취소하여 발생하는 예외(Abort/NotUsable)는 무시하거나 가볍게 로그만 남김
        if (ex.getClass().getName().contains("ClientAbortException") || 
            ex instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException ||
            errorMessage.contains("중단되었습니다")) {
            log.debug("Client connection aborted: {}", errorMessage);
            return null; // 응답을 보내지 않음
        }

        log.error("Exception occurred: ", ex);

        // API 요청(/api/)인 경우 JSON 형태로 에러 응답
        if (request.getRequestURI().contains("/api/")) {
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Internal Server Error", "message", errorMessage));
        }

        // 일반 페이지 요청인 경우 에러 페이지 뷰 반환
        ModelAndView mav = new ModelAndView();
        mav.setViewName("error/500");
        mav.addObject("exception", ex);
        return mav;
    }
}
