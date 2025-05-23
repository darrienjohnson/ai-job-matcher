package com.menoson.ai_job_matcher.entity;

import jakarta.persistence.*; // includes JPA annotations for database mapping

@Entity //Marks this class as a JPA entity meaning it should be mapped to a DB table
@Table(name = "jobs") // sets database table name to jobs
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // automatically generates a unique ID for each row using database's identity column strategy
    private Long id; // primary key
    // each becomes a column in the jobs table
    private String title;
    private String company;
    private String location;
    private String description;

    // Constructors
    public Job() {}

    public Job(String title, String company, String location, String description) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
