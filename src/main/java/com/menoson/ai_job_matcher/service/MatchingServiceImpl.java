// MatchingServiceImpl.java
package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.dto.JobMatchDTO;
import com.menoson.ai_job_matcher.entity.Job;
import com.menoson.ai_job_matcher.entity.Resume;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class MatchingServiceImpl implements MatchingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String EMBED_MATCH_URL = "http://localhost:5001/similarity";

    @Override
    public List<JobMatchDTO> matchJobs(Resume resume, List<Job> allJobs) {
        List<JobMatchDTO> results = new ArrayList<>();

        for (Job job : allJobs) {
            float score = getSimilarityScore(resume.getText(), job.getDescription());

            JobMatchDTO dto = new JobMatchDTO();
            dto.setJob(job);
            dto.setMatchScore(score);
            results.add(dto);
        }

        // Sort by highest match score
        results.sort(Comparator.comparingDouble(JobMatchDTO::getMatchScore).reversed());

        return results;
    }

    private float getSimilarityScore(String resume, String job) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("resume", resume);
        requestMap.put("job", job);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(EMBED_MATCH_URL, request, Map.class);

            Object scoreObj = response.getBody().get("similarity");
            return scoreObj != null ? Float.parseFloat(scoreObj.toString()) : 0f;

        } catch (Exception e) {
            e.printStackTrace();
            return 0f; // fallback score on error
        }
    }
}
