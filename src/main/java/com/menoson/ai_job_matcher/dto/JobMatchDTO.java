package com.menoson.ai_job_matcher.dto;

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
}
