package com.osci.backend.service;

import com.osci.backend.dto.ResumeDTO;
import com.osci.backend.entity.Resume;
import com.osci.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public List<ResumeDTO> getAllResumes() {
        return resumeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ResumeDTO getResumeById(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return convertToDTO(resume);
    }

    @Transactional
    public ResumeDTO createResume(ResumeDTO resumeDTO) {
        Resume resume = Resume.builder()
                .name(resumeDTO.getName())
                .email(resumeDTO.getEmail())
                .phone(resumeDTO.getPhone())
                .position(resumeDTO.getPosition())
                .summary(resumeDTO.getSummary())
                .yearsOfExperience(resumeDTO.getYearsOfExperience())
                .skills(resumeDTO.getSkills())
                .build();

        Resume saved = resumeRepository.save(resume);
        return convertToDTO(saved);
    }

    @Transactional
    public ResumeDTO updateResume(Long id, ResumeDTO resumeDTO) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        resume.setName(resumeDTO.getName());
        resume.setEmail(resumeDTO.getEmail());
        resume.setPhone(resumeDTO.getPhone());
        resume.setPosition(resumeDTO.getPosition());
        resume.setSummary(resumeDTO.getSummary());
        resume.setYearsOfExperience(resumeDTO.getYearsOfExperience());
        resume.setSkills(resumeDTO.getSkills());

        return convertToDTO(resume);
    }

    @Transactional
    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }

    private ResumeDTO convertToDTO(Resume resume) {
        return ResumeDTO.builder()
                .id(resume.getId())
                .name(resume.getName())
                .email(resume.getEmail())
                .phone(resume.getPhone())
                .position(resume.getPosition())
                .summary(resume.getSummary())
                .yearsOfExperience(resume.getYearsOfExperience())
                .skills(resume.getSkills())
                .build();
    }
}