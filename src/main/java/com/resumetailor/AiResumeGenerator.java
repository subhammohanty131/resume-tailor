package com.resumetailor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class AiResumeGenerator {

    public static void main(String[] args) {
        Path profileFile = Path.of("data", "candidate.properties");
        Path templateFile = Path.of("templates", "resume-template.tex");
        Path outputFile = Path.of(
                "output", "reviewed-resume.tex"
        ).toAbsolutePath().normalize();

        try (Scanner scanner = new Scanner(System.in)) {
            CandidateProfile profile =
                    new ProfileLoader().load(profileFile);

            List<String> summaries = profile.getSections()
                    .get("Professional Summary");

            if (summaries == null
                    || summaries.isEmpty()
                    || summaries.get(0).isBlank()) {
                throw new IOException(
                        "Professional Summary is missing from the profile."
                );
            }

            String originalSummary = summaries.get(0);

            System.out.println("RESUME TAILOR - AI REVIEW");
            System.out.println("Candidate: " + profile.getName());
            System.out.println();
            System.out.println("Paste the job description.");
            System.out.println("Enter END on a separate line:");

            StringBuilder jobDescription = new StringBuilder();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if ("END".equalsIgnoreCase(line.trim())) {
                    break;
                }

                jobDescription.append(line).append('\n');
            }

            String jd = jobDescription.toString().trim();

            if (jd.isBlank()) {
                System.out.println(
                        "No job description supplied. Nothing generated."
                );
                return;
            }

            List<String> matches = new SkillMatcher()
                    .findMatchingSkills(profile, jd);

            System.out.println();
            System.out.println("Matching skills:");

            if (matches.isEmpty()) {
                System.out.println("- No matching skills found.");
            } else {
                matches.forEach(skill ->
                        System.out.println("- " + skill)
                );
            }

            String suggestion = null;
            List<String> issues = List.of();
            boolean canApply = false;

            System.out.println();
            System.out.println("Requesting an AI summary suggestion...");

            try {
                suggestion = new GeminiResumeClient().rewriteSummary(
                        originalSummary, matches, jd
                );

                issues = new AiContentValidator().validateSummary(
                        originalSummary, suggestion, profile
                );

                canApply = issues.isEmpty();

            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.out.println(
                        "You can still generate using the original summary."
                );
            }

            System.out.println();
            System.out.println("ORIGINAL SUMMARY:");
            System.out.println(originalSummary);

            if (suggestion != null) {
                System.out.println();
                System.out.println("AI SUGGESTION:");
                System.out.println(suggestion);

                if (canApply) {
                    System.out.println();
                    System.out.println("Automated checks passed.");
                    System.out.println(
                            "Review factual accuracy before choosing APPLY."
                    );
                } else {
                    System.out.println();
                    System.out.println("VALIDATION ISSUES:");

                    issues.forEach(issue ->
                            System.out.println("- " + issue)
                    );

                    System.out.println(
                            "This suggestion cannot be applied in this flow."
                    );
                }
            }

            System.out.println();

            if (canApply) {
                System.out.println(
                        "Enter APPLY to use the reviewed AI summary,"
                );
                System.out.println(
                        "ORIGINAL to keep your existing summary,"
                );
                System.out.println("or CANCEL to stop.");
            } else {
                System.out.println(
                        "Enter ORIGINAL to generate with your existing summary,"
                );
                System.out.println("or CANCEL to stop.");
            }

            String selectedSummary = null;

            while (scanner.hasNextLine()) {
                String choice = scanner.nextLine().trim();

                if ("CANCEL".equalsIgnoreCase(choice)) {
                    System.out.println(
                            "Cancelled. No resume files were generated."
                    );
                    return;
                }

                if ("ORIGINAL".equalsIgnoreCase(choice)) {
                    selectedSummary = originalSummary;
                    break;
                }

                if ("APPLY".equalsIgnoreCase(choice) && canApply) {
                    selectedSummary = suggestion;
                    break;
                }

                System.out.println(
                        canApply
                                ? "Enter APPLY, ORIGINAL, or CANCEL."
                                : "Enter ORIGINAL or CANCEL."
                );
            }

            if (selectedSummary == null) {
                System.out.println(
                        "No selection received. Nothing generated."
                );
                return;
            }

            generateReviewedResume(
                    profile,
                    matches,
                    selectedSummary,
                    profileFile,
                    templateFile,
                    outputFile
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Resume generation interrupted.");
        } catch (IOException e) {
            System.out.println(
                    "Resume generation failed: " + e.getMessage()
            );
        }
    }

    private static void generateReviewedResume(
            CandidateProfile originalProfile,
            List<String> matches,
            String selectedSummary,
            Path profileFile,
            Path templateFile,
            Path outputFile
    ) throws IOException, InterruptedException {

        Properties properties = new Properties();

        try (Reader reader = Files.newBufferedReader(
                profileFile, StandardCharsets.UTF_8
        )) {
            properties.load(reader);
        }

        properties.setProperty("summary", selectedSummary);

        Map<String, List<String>> sections = new LinkedHashMap<>(
                originalProfile.getSections()
        );

        sections.put(
                "Professional Summary",
                List.of(selectedSummary)
        );

        CandidateProfile reviewedProfile = new CandidateProfile(
                originalProfile.getName(),
                originalProfile.getSkills(),
                originalProfile.getContact(),
                sections
        );

        Path temporaryProfile = Files.createTempFile(
                "resume-review-", ".properties"
        );

        try {
            try (Writer writer = Files.newBufferedWriter(
                    temporaryProfile, StandardCharsets.UTF_8
            )) {
                properties.store(
                        writer,
                        "Temporary profile for reviewed resume generation"
                );
            }

            new ResumeGenerator().generate(
                    reviewedProfile,
                    matches,
                    temporaryProfile,
                    templateFile,
                    outputFile
            );

            System.out.println();
            System.out.println("LaTeX file generated:");
            System.out.println(outputFile);

            System.out.println();
            System.out.println("Compiling PDF...");

            Path pdfFile = new PdfCompiler().compile(outputFile);

            System.out.println("PDF generated:");
            System.out.println(pdfFile);
            System.out.println();
            System.out.println(
                    "Your candidate.properties file was not changed."
            );

        } finally {
            Files.deleteIfExists(temporaryProfile);
        }
    }
}