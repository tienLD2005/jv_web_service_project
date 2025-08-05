package com.tien.project.repository;

import com.tien.project.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    @Query("SELECT s FROM Session s WHERE s.expiryTime > :currentTime")
    List<Session> findActiveSessions(LocalDateTime currentTime);


    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiryTime <= :currentTime")
    int deleteExpiredSessions(LocalDateTime currentTime);
}