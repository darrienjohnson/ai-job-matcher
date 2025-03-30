package com.menoson.ai_job_matcher.controller;
import com.menoson.ai_job_matcher.dto.JobMatchDTO;


import com.menoson.ai_job_matcher.entity.Resume;
import com.menoson.ai_job_matcher.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import java.io.IOException;

@CrossOrigin(origins = "*") // Not for production, REMEMBER TO CHANGE LATER
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // Get all uploaded resumes
    @GetMapping
    public ResponseEntity<List<Resume>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    // Get a specific resume by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getResumeById(@PathVariable Long id) {
        Optional<Resume> resume = resumeService.getResumeById(id);
        return resume.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Returns a list of matching jobs with a match score for the given resume
    // Fetches the resume (i.e. ID = 1) Gets the resume skills, compares those skills to all jobs in database, returns only the jobs that match with non-zero score
    @GetMapping("/{id}/matches")
    public ResponseEntity<List<JobMatchDTO>> getJobMatches(@PathVariable Long id) {
        List<JobMatchDTO> matches = resumeService.getMatchingJobs(id);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("candidateName") String candidateName,
            @RequestParam("email") String email) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }

        try {
            // Save resume and file
            Resume savedResume = resumeService.uploadResume(file, candidateName, email);

            // Build public file URL
            String fileUrl = "http://localhost:8080/" + savedResume.getFilePath();

            // Prepare response body with resume + URL
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("resume", savedResume);
            responseBody.put("fileUrl", fileUrl);

            // Return 201 Created with full response
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

}
