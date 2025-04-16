package com.menoson.ai_job_matcher.controller;

import com.menoson.ai_job_matcher.entity.Job;
import com.menoson.ai_job_matcher.repository.JobRepository;
import com.menoson.ai_job_matcher.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// Handles HTTP requests for Job data
@RestController //tells Spring this class handles API requests and returns JSON responses
@RequestMapping("/api") //sets base URL
public class JobController {

    private final JobRepository jobRepository;
    private final JobService jobService;

    // Constructor-based injection (preferred)
    public JobController(JobRepository jobRepository, JobService jobService) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
    }

    //retrieves all job records from the DB using the repository
    @GetMapping("/jobs")
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        Job savedJob = jobRepository.save(job);
        return new ResponseEntity<>(savedJob, HttpStatus.CREATED);
    }

    @PostMapping("/jobs/bulk")
    public ResponseEntity<?> createJobs(@RequestBody List<Job> jobs) {
        jobRepository.saveAll(jobs);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        Job updatedJob = jobService.updateJob(id, job);
        if (updatedJob != null) {
            return ResponseEntity.ok(updatedJob);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        if (jobService.deleteJob(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}
