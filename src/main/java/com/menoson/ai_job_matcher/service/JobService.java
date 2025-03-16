package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.entity.Job;
import com.menoson.ai_job_matcher.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    // Constructor that injects a JobRepository instance into the JobService
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Return a list of all jobs in the database
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Creates a new job in the database and returns the saved job
    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    // Updates an existing job in the database and returns the updated job
    public Job updateJob(Long id, Job updatedJob) {
        return jobRepository.findById(id)
                .map(job -> {
                    job.setTitle(updatedJob.getTitle());
                    job.setCompany(updatedJob.getCompany());
                    job.setLocation(updatedJob.getLocation());
                    job.setDescription(updatedJob.getDescription());
                    return jobRepository.save(job);
                })
                .orElse(null); // Return null if job not found
    }

    // Deletes a job from the database by its ID
    public boolean deleteJob(Long id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
