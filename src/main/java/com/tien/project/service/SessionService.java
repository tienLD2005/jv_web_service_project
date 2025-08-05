package com.tien.project.service;

import com.tien.project.entity.Session;

import java.util.List;

public interface SessionService {
    List<Session> getActiveSessions();
//    void deleteSession(String sessionId);
    int cleanupExpiredSessions();
}
