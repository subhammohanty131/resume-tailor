package com.resumetailor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class AiSummaryPreview {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            CandidateProfile profile = new ProfileLoader().load(
                    Path.of("data", "candidate.properties")
            );

            List<String> summaries = profile.getSections()
                    .get("Professional Summary");

            if (summaries == null || summaries.isEmpty()) {
                System.err.println("Your profile needs a summary.");
                return;
            }

            String original = summaries.get(0);

            System.out.println("Paste the job description.");
            System.out.println("Enter END on a separate line:");

            StringBuilder input = new StringBuilder();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.trim().equalsIgnoreCase("END")) {
                    break;
                }

                input.append(line).append('\n');
            }

            String jobDescription = input.toString().trim();

            if (jobDescription.isEmpty()) {
                System.err.println("Job description is required.");
                return;
            }

            List<String> matches = new SkillMatcher()
                    .findMatchingSkills(profile, jobDescription);

            System.out.println();
            System.out.println("Requesting an AI summary suggestion...");

            String suggested = new GeminiResumeClient().rewriteSummary(
                    original,
                    matches,
                    jobDescription
            );

            List<String> issues = new AiContentValidator()
                    .validateSummary(original, suggested, profile);

            System.out.println();
            System.out.println("ORIGINAL SUMMARY:");
            System.out.println(original);

            System.out.println();
            System.out.println("AI SUGGESTION:");
            System.out.println(suggested);

            System.out.println();

            if (issues.isEmpty()) {
                System.out.println("Automated checks passed.");
                System.out.println(
                        "Review factual meaning before applying this text.");
            } else {
                System.out.println("VALIDATION ISSUES:");

                for (String issue : issues) {
                    System.out.println("- " + issue);
                }
            }

            System.out.println();
            System.out.println(
                    "Preview only: your profile and PDF were not changed.");

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("AI request interrupted.");

        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }
}