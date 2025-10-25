package com.osci.backend.controller;

import com.osci.backend.dto.ResumeDTO;
import com.osci.backend.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public ResponseEntity<List<ResumeDTO>> getAllResumes(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        List<ResumeDTO> resumes = resumeService.getAllResumes();
        // 모든 이력서에 요청자 IP 추가
        resumes.forEach(resume -> resume.setRequestIp(clientIp));
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDTO> getResumeById(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        ResumeDTO resume = resumeService.getResumeById(id);
        resume.setRequestIp(clientIp);
        return ResponseEntity.ok(resume);
    }

    @PostMapping
    public ResponseEntity<ResumeDTO> createResume(@RequestBody ResumeDTO resumeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.createResume(resumeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDTO> updateResume(
            @PathVariable Long id,
            @RequestBody ResumeDTO resumeDTO) {
        return ResponseEntity.ok(resumeService.updateResume(id, resumeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    // Client IP 추출 메서드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For에 여러 IP가 있을 경우 첫 번째가 클라이언트 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}