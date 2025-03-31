package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.dto.JobMatchDTO;
import com.menoson.ai_job_matcher.entity.Job;
import com.menoson.ai_job_matcher.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KeywordMatchingService implements MatchingService {

    @Override
    public List<JobMatchDTO> matchJobs(Resume resume, List<Job> allJobs) {
        // Extract individual keywords from the skills blob
        Set<String> resumeKeywords = new HashSet<>(Arrays.asList(
                resume.getSkills()
                        .replaceAll("(?i)(Programming:|Frameworks.*?:|Cloud.*?:|Databases:|Automation.*?:|Monitoring.*?:|Security.*?:|Development Methodologies:)", "")
                        .replaceAll("\\(.*?\\)", "")
                        .replaceAll("[^a-zA-Z0-9 ]", " ")  // Remove symbols like colons, bullets, etc.
                        .toLowerCase()
                        .split("\\s+")
        ));

        // Remove any short/noise words
        resumeKeywords.removeIf(word -> word.length() <= 2);

        List<JobMatchDTO> matches = new ArrayList<>();

        for (Job job : allJobs) {
            int matchCount = 0;
            String jobText = job.getDescription().toLowerCase().replaceAll("[^a-zA-Z0-9 ]", " ");

            for (String keyword : resumeKeywords) {
                if (jobText.contains(keyword)) {
                    matchCount++;
                }
            }

            double matchScore = resumeKeywords.size() > 0
                    ? (double) matchCount / resumeKeywords.size()
                    : 0.0;

            System.out.println("Matched " + matchCount + " keywords: " + resumeKeywords);
            System.out.println("Job Description: " + job.getDescription());
            
            if (matchScore > 0) {
                matches.add(new JobMatchDTO(
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getDescription(),
                        matchScore
                ));
            }
        }

        matches.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return matches;
    }
}
