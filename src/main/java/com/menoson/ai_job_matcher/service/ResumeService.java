package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.entity.Resume;
import com.menoson.ai_job_matcher.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.menoson.ai_job_matcher.dto.JobMatchDTO;
import com.menoson.ai_job_matcher.entity.Job;
import com.menoson.ai_job_matcher.repository.JobRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private static final String UPLOAD_DIR = "uploads/";
    private final Tika tika;
    private static final Logger LOGGER = Logger.getLogger(ResumeService.class.getName());
    private final JobRepository jobRepository;

    public ResumeService(ResumeRepository resumeRepository, JobRepository jobRepository) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.tika = new Tika();
    }

    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public Resume uploadResume(MultipartFile file, String candidateName, String email) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);

        // Ensure upload directory exists
        Files.createDirectories(filePath.getParent());

        // Save file
        Files.write(filePath, file.getBytes());

        // Extract full resume text
        String fullText = extractTextFromResume(filePath);

        // Extract structured data
        String skills = extractSection(fullText, "Skills");
        String experience = extractSection(fullText, "Professional Experiences");
        String education = extractSection(fullText, "Education and Certifications");

        // Create Resume entry in DB
        Resume resume = new Resume();
        resume.setCandidateName(candidateName);
        resume.setEmail(email);
        resume.setFilePath(filePath.toString());
        resume.setSkills(skills.isEmpty() ? "No skills found" : skills);
        resume.setExperience(experience.isEmpty() ? "Experience not found" : experience);
        resume.setEducation(education.isEmpty() ? "Education not found" : education);
        resume.setUploadedAt(java.time.LocalDateTime.now());

        return resumeRepository.save(resume);
    }

    public List<JobMatchDTO> getMatchingJobs(Long resumeId) {
        Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
        if (resumeOpt.isEmpty()) return Collections.emptyList();

        Resume resume = resumeOpt.get();

        // Clean the 'skills' string from the resume
        String cleanedSkills = resume.getSkills()
                .replaceAll("(?i)(Programming:|Frameworks.*?:|Cloud.*?:)", "") // remove headings like "Programming:", case-insensitive
                .replaceAll("\\n", ",") // convert line breaks to commas (so each line becomes a separate skill)
                .replaceAll("\\s+", " ") // normalize extra spaces to a single space
                .replaceAll("\\(.*?\\)", "") // remove anything inside parentheses (e.g., AWS (S3, RDS))
                .trim(); // remove leading/trailing whitespace

        // Split the cleaned skills string into an array, separated by commas
        String[] resumeSkills = cleanedSkills.split(",\\s*"); // split on comma + optional space

        // Debugging - print extracted skills to console for verification
        System.out.println("Cleaned & Split Skills: " + Arrays.toString(resumeSkills));

        // Create a list to hold matching job results
        List<JobMatchDTO> matches = new ArrayList<>();

        // Loop through all jobs in the database
        for (Job job : jobRepository.findAll()) {
            int matchCount = 0;

            // Get the job description, convert to lowercase, and normalize whitespace
            String jobText = job.getDescription().toLowerCase().replaceAll("\\s+", " ");

            // Compare each resume skill to the job description
            for (String skill : resumeSkills) {
                if (jobText.contains(skill.toLowerCase())) {
                    matchCount++; // Increment counter for each skill match found
                }
            }

            double matchScore = resumeSkills.length > 0 ? (double) matchCount / resumeSkills.length : 0.0;

            // If there's at least one match, create a DTO and add it to the results
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

        // Sort the list of matches in descending order by match score
        matches.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        // Return the sorted list of job matches
        return matches;
    }


    private String extractTextFromResume(Path filePath) {
        try {
            PDDocument document = PDDocument.load(filePath.toFile());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String fullText = pdfStripper.getText(document);
            document.close();

            // Debugging - print extracted text
            LOGGER.info("Extracted Resume Text: \n" + fullText);

            return fullText;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error extracting text";
        }
    }


    private String extractSkills(String text) {
        String[] skillKeywords = {
                "Java", "Python", "SQL", "Spring Boot", "React", "Angular", "AWS", "Docker",
                "Machine Learning", "Data Analysis", "PostgreSQL", "REST API", "Git", "Kubernetes", "CI/CD",
                "TensorFlow", "Flask", "Node.js", "GraphQL", "TypeScript", "Cloud Computing"
        };

        StringBuilder foundSkills = new StringBuilder();
        for (String skill : skillKeywords) {
            if (Pattern.compile("\\b" + skill + "\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                foundSkills.append(skill).append(", ");
            }
        }

        return foundSkills.length() > 0 ? foundSkills.substring(0, foundSkills.length() - 2) : "No skills found";
    }

    private String extractExperience(String text) {
        Pattern pattern = Pattern.compile("(\\d+\\s*(years|yrs|year) of experience)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "Experience not found";
    }

    private String extractEducation(String text) {
        String[] educationKeywords = {
                "Bachelor", "Master", "PhD", "B.Sc", "M.Sc", "Associate", "Doctorate", "MBA", "BS", "MS", "BA", "MA"
        };

        StringBuilder foundEducation = new StringBuilder();
        for (String degree : educationKeywords) {
            if (Pattern.compile("\\b" + degree + "\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                foundEducation.append(degree).append(", ");
            }
        }

        return foundEducation.length() > 0 ? foundEducation.substring(0, foundEducation.length() - 2) : "Education not found";
    }

    private String extractSection(String text, String sectionName) {
        Pattern pattern = Pattern.compile(sectionName + "\\s*(.*?)\\s*(?=\\n\\S+:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
