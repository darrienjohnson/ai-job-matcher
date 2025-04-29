package com.menoson.ai_job_matcher.dto;

import com.menoson.ai_job_matcher.entity.Job;

public class JobMatchDTO {
    private String title;
    private String company;
    private String location;
    private String description;
    private double matchScore; // value between 0.0 - 1.0

    public JobMatchDTO(String title, String company, String location, String description, double matchScore) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.matchScore = matchScore;
    }

    public JobMatchDTO() {
        // No-arg constructor for frameworks like Jackson or for manual setters
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public void setJob(Job job) {
        this.title = job.getTitle();
        this.company = job.getCompany();
        this.location = job.getLocation();
        this.description = job.getDescription();
    }

}
