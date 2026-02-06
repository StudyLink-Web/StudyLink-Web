package com.StudyLink.www.service;

import com.StudyLink.www.dto.AdminUserDTO;
import com.StudyLink.www.dto.UserChartDTO;
import com.StudyLink.www.dto.UsersDTO;
import com.StudyLink.www.entity.Role;
import com.StudyLink.www.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface UserService {

    Long findUserIdByUsername(String username);

    Users findByEmail(String email);

    Users findByUsername(String username);

    int getTodayNewUserCount();

    UserChartDTO getUserChart();

    Page<AdminUserDTO> search(String email,
                              Role role,
                              Boolean isActive,
                              LocalDate startDate,
                              LocalDate endDate,
                              Pageable sortedPageable);

    UsersDTO getUserDetail(Long id);

    Users saveUser(Users user);
}
