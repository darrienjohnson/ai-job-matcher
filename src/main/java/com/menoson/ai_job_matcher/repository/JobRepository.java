package com.menoson.ai_job_matcher.repository;

import com.menoson.ai_job_matcher.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
