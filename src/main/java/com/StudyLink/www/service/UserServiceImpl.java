package com.StudyLink.www.service;

import com.StudyLink.www.dto.UserChartDTO;
import com.StudyLink.www.dto.UserChartPeriodDTO;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Long findUserIdByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음 username=" + username));
        return user.getUserId();
    }

    @Override
    public int getTodayNewUserCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        return userRepository.countByCreatedAtBetween(startOfDay, startOfNextDay);
    }

    @Override
    public UserChartDTO getUserChart() {
        return new UserChartDTO(
                getUserChartByDays(7),
                getUserChartByDays(30)
        );
    }

    private UserChartPeriodDTO getUserChartByDays(int days) {
        List<String> labels = new ArrayList<>();
        List<Integer> daily = new ArrayList<>();
        List<Integer> cumulative = new ArrayList<>();

        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        int sum = userRepository.countBefore(
                startDate.atStartOfDay()
        );

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);

            int count = userRepository.countByCreatedDate(
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay()
            );

            sum += count;

            labels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            daily.add(count);
            cumulative.add(sum);
        }

        return new UserChartPeriodDTO(labels, daily, cumulative);
    }
}
