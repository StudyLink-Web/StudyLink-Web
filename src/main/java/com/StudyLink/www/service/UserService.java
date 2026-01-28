package com.StudyLink.www.service;

import com.StudyLink.www.dto.UserChartDTO;

public interface UserService {
    Long findUserIdByUsername(String username);

    int getTodayNewUserCount();

    UserChartDTO getUserChart();
}