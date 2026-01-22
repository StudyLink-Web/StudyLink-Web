package com.StudyLink.www.sync;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomSyncManager {

    private final Map<Long, Boolean> syncingRooms = new ConcurrentHashMap<>();

    public void startSync(long roomId) {
        syncingRooms.put(roomId, true);
    }

    public void endSync(long roomId) {
        syncingRooms.put(roomId, false);
    }

    public boolean isSyncing(long roomId) {
        return syncingRooms.getOrDefault(roomId, false);
    }
}