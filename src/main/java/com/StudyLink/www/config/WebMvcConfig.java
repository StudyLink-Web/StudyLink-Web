package com.StudyLink.www.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.profile-image-dir:uploads/profiles}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DdayInterceptor());
    }  // addInterceptors 메서드 종료

    /**
     * ✅ 업로드된 파일을 정적 리소스로 제공
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/../");
    }

    public static class DdayInterceptor implements HandlerInterceptor {

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response,
                               Object handler, ModelAndView modelAndView) throws Exception {

            // modelAndView가 null이 아닐 때만 처리
            if (modelAndView != null) {
                // D-day 계산 (2026-11-11 수능)
                LocalDate csatDate = LocalDate.of(2026, 11, 19);
                LocalDate today = LocalDate.now();
                long dday = ChronoUnit.DAYS.between(today, csatDate);

                // 모든 요청에 dday 추가
                modelAndView.addObject("dday", dday);

                System.out.println("📅 D-day 계산 완료: D-" + dday);
            }

            HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
        }
    }  // DdayInterceptor 클래스 종료
}  // WebMvcConfig 클래스 종료
