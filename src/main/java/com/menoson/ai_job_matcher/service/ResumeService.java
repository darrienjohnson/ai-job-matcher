package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.entity.Resume;
import com.menoson.ai_job_matcher.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private static final String UPLOAD_DIR = "uploads/";
    private final Tika tika;
    private static final Logger LOGGER = Logger.getLogger(ResumeService.class.getName());

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
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
