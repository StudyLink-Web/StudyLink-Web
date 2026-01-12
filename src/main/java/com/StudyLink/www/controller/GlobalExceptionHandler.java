package com.StudyLink.www.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        log.error("Exception occurred: ", ex);

        ModelAndView mav = new ModelAndView();
        mav.setViewName("error/500");
        mav.addObject("exception", ex);

        return mav;
    }
}
