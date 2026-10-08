package com.resumetailor;

import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
    	CandidateProfile profile;

    	try {
    	    ProfileLoader loader = new ProfileLoader();
    	    profile = loader.load(Path.of("data", "candidate.properties"));

    	} catch (IOException exception) {
    	    System.err.println(
    	            "Could not load candidate profile: "
    	            + exception.getMessage());
    	    return;
    	}

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("RESUME TAILOR");
            System.out.println("Candidate: " + profile.getName());
            System.out.println();
            System.out.println("Paste the job description.");
            System.out.println("Enter END on a separate line to finish:");

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
                System.out.println("Please enter a job description.");
                return;
            }

            SkillMatcher matcher = new SkillMatcher();
            List<String> matches =
                    matcher.findMatchingSkills(profile, jobDescription);

            System.out.println();
            System.out.println("Matching skills:");

            if (matches.isEmpty()) {
                System.out.println("No listed skills matched.");
            } else {
                for (String skill : matches) {
                    System.out.println("- " + skill);
                }
            }

            System.out.println();
            System.out.println(
                    "Matched " + matches.size()
                    + " of " + profile.getSkills().size()
                    + " candidate skills."
            );
            try {
                ResumeGenerator generator = new ResumeGenerator();
                Path generatedFile = generator.generate(profile, matches);

                System.out.println();
                System.out.println("LaTeX file generated:");
                System.out.println(generatedFile);

                System.out.println();
                System.out.println("Compiling PDF...");

                PdfCompiler compiler = new PdfCompiler();
                Path pdfFile = compiler.compile(generatedFile);

                System.out.println("PDF generated:");
                System.out.println(pdfFile);

            } catch (IOException exception) {
                System.err.println(
                        "Resume generation failed: " + exception.getMessage());

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                System.err.println("PDF compilation was interrupted.");
            }
        }
    }
}