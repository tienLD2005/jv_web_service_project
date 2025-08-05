package com.tien.project.controller;

import com.tien.project.dto.response.APIResponse;
import com.tien.project.entity.Session;
import com.tien.project.repository.SessionRepository;
import com.tien.project.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionRepository sessionRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Session>> getActiveSessions() {
        List<Session> sessions = sessionService.getActiveSessions();
        APIResponse.DataWrapper<Session> data = new APIResponse.DataWrapper<>(sessions, null);
        return new ResponseEntity<>(new APIResponse<>(true, "Danh sách session đang hoạt động", data, null, null), HttpStatus.OK);
    }

    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteSession(@PathVariable String sessionId) {
        sessionRepository.deleteById(sessionId);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Session deleted"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Xoá session thành công", data, null, null), HttpStatus.OK);
    }

//    @PostMapping("/cleanup")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<APIResponse<String>> cleanupExpiredSessions() {
//        sessionService.cleanupExpiredSessions();
//        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Expired sessions cleaned up"), null);
//        return new ResponseEntity<>(new APIResponse<>(true, "Dọn dẹp session hết hạn thành công", data, null, null), HttpStatus.OK);
//    }
}
