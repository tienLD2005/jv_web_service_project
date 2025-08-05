package com.tien.project.service.impl;

import com.tien.project.entity.Session;
import com.tien.project.repository.SessionRepository;
import com.tien.project.service.SessionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Override
    public List<Session> getActiveSessions() {
        return sessionRepository.findActiveSessions(LocalDateTime.now());
    }

//    @Override
//    public void deleteSession(String sessionId) {
//        sessionRepository.deleteBySessionId(sessionId);
//    }

    @Transactional
    @Override
    public int cleanupExpiredSessions() {
        return sessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}
