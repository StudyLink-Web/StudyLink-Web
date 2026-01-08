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
    //매핑에서 에러매핑 받는 컨트롤러
    private final String VIEW_PATH = "/error/";

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if(status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                //404 에러일경우...
                return VIEW_PATH+"404";
            }
            if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return VIEW_PATH+"500";
            }
        }
        return "/";
    }
}
