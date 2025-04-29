package com.menoson.ai_job_matcher.service;

import com.menoson.ai_job_matcher.entity.Resume;
import com.menoson.ai_job_matcher.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final MatchingService matchingService;


    public ResumeService(ResumeRepository resumeRepository, JobRepository jobRepository, @Qualifier("matchingServiceImpl") MatchingService matchingService) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.matchingService = matchingService;
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
        String experience = extractSection(fullText, "Professional Experience");
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
        return matchingService.matchJobs(resumeOpt.get(), jobRepository.findAll());
    }


    private String extractTextFromResume(Path filePath) {
        try {
            PDDocument document = PDDocument.load(filePath.toFile());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String fullText = pdfStripper.getText(document);
            document.close();

            // Normalize common PDF formatting issues
            fullText = fullText
                    .replaceAll("-\\s*\\n\\s*", "")     // fix hyphenated line breaks
                    .replaceAll("\\n+", "\n")           // collapse multiple line breaks
                    .replaceAll("\\s{2,}", " ")          // normalize extra spaces
                    .replaceAll("(?m)^\\s+", "")         // trim line starts
                    .replaceAll("(?m)\\s+$", "")         // trim line ends
                    .trim();

            // Optional: log output for verification
            LOGGER.info("Extracted and Normalized Resume Text:\n" + fullText);

            return fullText;

        } catch (IOException e) {
            e.printStackTrace();
            return "Error extracting text";
        }
    }



    private String extractSkills(String text) {
        String lower = text.toLowerCase();

        int start = lower.indexOf("skills");
        if (start == -1) return "";

        // Find the next known section after skills
        int end = Integer.MAX_VALUE;
        String[] stopSections = {
                "professional experience", "experience", "education", "certifications", "projects", "summary",
                "professional experience"
        };

        for (String stop : stopSections) {
            int stopIndex = lower.indexOf(stop, start + 6); // avoid matching the 'skills' word itself
            if (stopIndex != -1 && stopIndex < end) {
                end = stopIndex;
            }
        }

        // Extract substring between skills and next section
        String rawSkills = text.substring(start, Math.min(end, text.length()));

        // Cleanup formatting
        return rawSkills
                .replaceAll("(?i)^skills\\s*[:\\-]*", "")    // Remove "Skills" heading
                .replaceAll("(?m)^\\s*•\\s*", "")            // remove bullets
                .replaceAll("\\s{2,}", " ")                  // collapse multiple spaces
                .replaceAll("\\n+", ", ")                    // convert line breaks to commas
                .replaceAll(",\\s*,", ",")                   // clean double commas
                .replaceAll("\\(.*?\\)", "")                 // remove parentheses
                .replaceAll(":+", ":")                       // normalize colons
                .trim();
    }




    private String extractEducation(String text) {
        return extractFlexibleSection(text, new String[] {
                "Education", "Education and Certifications", "Academic Background"
        });
    }

    private String extractExperience(String text) {
        return extractFlexibleSection(text, new String[] {
                "Experience", "Work Experience", "Professional Experiences", "Employment History"
        });
    }

    private String extractSection(String text, String sectionName) {
        String lower = text.toLowerCase();
        String sectionStart = sectionName.toLowerCase();

        int startIndex = lower.indexOf(sectionStart);
        if (startIndex == -1) return "";

        // Known headers that signal the end of a section
        String[] stopSections = {
                "professional experience", "experience", "education", "certifications", "projects", "summary"
        };

        int endIndex = text.length();
        for (String stop : stopSections) {
            int stopIdx = lower.indexOf(stop, startIndex + sectionStart.length());
            if (stopIdx != -1 && stopIdx < endIndex) {
                endIndex = stopIdx;
            }
        }

        return text.substring(startIndex, endIndex)
                .replaceAll("(?i)^skills\\s*[:\\-]*", "")    // Remove heading if it's "Skills"
                .replaceAll("(?m)^\\s*•\\s*", "")            // remove bullets
                .replaceAll("\\s{2,}", " ")                  // collapse spaces
                .replaceAll("\\n+", ", ")                    // convert line breaks to commas
                .replaceAll(",\\s*,", ",")                   // clean double commas
                .replaceAll("\\(.*?\\)", "")                 // remove parentheses
                .replaceAll(":+", ":")                       // normalize colons
                .trim();
    }


    private String extractFlexibleSection(String text, String[] sectionNames) {
        for (String section : sectionNames) {
            Pattern pattern = Pattern.compile(
                    "(?i)" + Pattern.quote(section) +      // Match section title (case-insensitive)
                            "\\s*\\n+" +                           // Followed by one or more line breaks
                            "(.*?)" +                              // Capture everything lazily...
                            "(?=\\n{2,}|\\n[A-Z][^\\n]*\\n|\\z)",  // ...until two newlines or next heading or end
                    Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String content = matcher.group(1).trim()
                        .replaceAll("•", "-")                // Replace bullet points with dashes
                        .replaceAll("-\\s*\\n\\s*", "")      // Join hyphenated line-breaks
                        .replaceAll("\\n+", ", ")            // Replace newlines with commas
                        .replaceAll("\\s{2,}", " ")          // Collapse multiple spaces
                        .replaceAll(",\\s*,", ",")           // Clean double commas
                        .replaceAll("\\(.*?\\)", "")         // Remove parentheses
                        .trim();
                return content;
            }
        }
        return "";
    }

}
