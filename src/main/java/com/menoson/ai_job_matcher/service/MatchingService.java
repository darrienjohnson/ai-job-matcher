package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.dto.JobMatchDTO;
import com.menoson.ai_job_matcher.entity.Job;
import com.menoson.ai_job_matcher.entity.Resume;

import java.util.List;

public interface MatchingService {
    List<JobMatchDTO> matchJobs(Resume resume, List<Job> allJobs);
}
