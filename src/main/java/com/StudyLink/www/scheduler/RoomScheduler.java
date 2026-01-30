package com.StudyLink.www.scheduler;

import com.StudyLink.www.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RoomScheduler {
    private final RoomService roomService;

    @Scheduled(fixedDelay = 60000) // 60초마다 실행
    public void deleteExpiredRooms() {
        // 현재 시간 기준으로 inprogress 상태 중 20분 지난 방 삭제
        roomService.deleteExpiredRooms();
        log.info(">>> 만료된 방 정리 실행: " + System.currentTimeMillis());
    }

    @Scheduled(fixedDelay = 60000 * 60 * 24) // 24시간 마다 실행
    public void deleteOldTempAndPendingRooms() {
        // 현재 시간 기준으로 temp, pending 상태중 24시간 지난 방 삭제
        roomService.deleteOldTempAndPendingRooms();
        log.info(">>> 만료된 방 정리 실행: " + System.currentTimeMillis());
    }
}
