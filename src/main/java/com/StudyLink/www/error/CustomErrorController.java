// CustomErrorController

package com.StudyLink.www.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    private static final String VIEW_PATH = "error/";

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;

        log.error("❌ Error 발생: statusCode={}", statusCode);

        if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return VIEW_PATH + "403";
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return VIEW_PATH + "404";
        }
        if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return VIEW_PATH + "500";
        }

        // ✅ 나머지 에러도 statusCode별 페이지가 없으면 500으로 통일
        return VIEW_PATH + "500";
    }
}
