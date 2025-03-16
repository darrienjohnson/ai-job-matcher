package com.menoson.ai_job_matcher.repository;

import com.menoson.ai_job_matcher.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
}