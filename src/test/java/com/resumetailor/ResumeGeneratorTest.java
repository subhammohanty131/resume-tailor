package com.resumetailor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

 public class ResumeGeneratorTest {

    private Path directory;
    private Path profileFile;
    private Path templateFile;
    private Path outputFile;

    private static final String PROFILE = """
            name=Test Candidate
            skills=SQL,Java
            summary=Validated A&B, 100%, user_name and #1.
            headline=Software Testing | QA Automation
            email=test@example.com
            phone=1234567890
            linkedin=https://www.linkedin.com/in/example
            github=https://github.com/example

            skills.display.manual=Functional testing.
            skills.display.automation=Java.
            skills.display.api=Postman.
            skills.display.database=SQL.
            skills.display.tools=Git.

            experience.1=Example Company | Tester | Example City | 2023-2024
            experience.2=Investigated defects.

            project.1=Banking Project | https://github.com/example/banking
            project.2=Created HTML reports.
            project.3=Validated balances using SQL.
            project.6=Manual Testing Project
            project.7=Documented test cases.

            education.1=Example Degree | Example University | 2021 | 88%
            certification.1=Example workshop
            """;

    @BeforeMethod
    public void prepareFiles() throws IOException {

        directory = Files.createTempDirectory("resume-generator-test-");

        profileFile = directory.resolve("candidate.properties");
        templateFile = directory.resolve("resume-template.tex");
        outputFile = directory.resolve("output").resolve("resume.tex");

        Files.writeString(
                profileFile, PROFILE, StandardCharsets.UTF_8);

        // Test the actual template used by the application.
        Files.copy(
                Path.of("templates", "resume-template.tex"),
                templateFile
        );
    }

    @AfterMethod(alwaysRun = true)
    public void removeTemporaryFiles() throws IOException {

        if (directory == null || !Files.exists(directory)) {
            return;
        }

        List<Path> paths;

        try (Stream<Path> stream = Files.walk(directory)) {
            paths = stream.sorted(Comparator.reverseOrder()).toList();
        }

        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }

    private String generate(List<String> matchingSkills)
            throws IOException {

        CandidateProfile profile =
                new ProfileLoader().load(profileFile);

        Path result = new ResumeGenerator().generate(
                profile,
                matchingSkills,
                profileFile,
                templateFile,
                outputFile
        );

        Assert.assertEquals(
                result,
                outputFile.toAbsolutePath().normalize()
        );

        return Files.readString(result, StandardCharsets.UTF_8);
    }

    @Test
    public void shouldGenerateCompleteLatexAndEscapeCharacters()
            throws IOException {

        String latex = generate(List.of());

        Assert.assertTrue(
                latex.startsWith("\\documentclass[10pt,a4paper]{article}")
        );

        Assert.assertTrue(latex.contains("\\end{document}"));

        Assert.assertFalse(
                latex.contains("@@"),
                "All template placeholders should be replaced."
        );

        Assert.assertTrue(latex.contains(
                "Validated A\\&B, 100\\%, user\\_name and \\#1."
        ));

        Assert.assertTrue(latex.contains(
                "\\section*{Courses and Workshops}"
        ));

        Assert.assertTrue(latex.contains(
                "\\href{https://github.com/example/banking}"
                + "{GitHub Repository}"
        ));

        Assert.assertTrue(latex.contains(
                "Example University\\hfill 88\\%"
        ));
    }

    @Test
    public void shouldPrioritiseRelevantProjectBullet()
            throws IOException {

        String latex = generate(List.of("SQL"));

        String relevantBullet = "Validated balances using SQL.";
        String otherBullet = "Created HTML reports.";

        Assert.assertTrue(latex.contains(relevantBullet));
        Assert.assertTrue(latex.contains(otherBullet));

        Assert.assertTrue(
                latex.indexOf(relevantBullet)
                        < latex.indexOf(otherBullet),
                "The SQL bullet should appear before the report bullet."
        );

        Assert.assertTrue(
                latex.indexOf("\\section*{Projects}")
                        < latex.indexOf("\\section*{Work Experience}"),
                "The requested section order should be preserved."
        );
    }

    @Test
    public void shouldRejectMissingRequiredDisplayField()
            throws IOException {

        String incompleteProfile = PROFILE.replace(
                "skills.display.api=Postman.",
                "skills.display.api="
        );

        Files.writeString(
                profileFile,
                incompleteProfile,
                StandardCharsets.UTF_8
        );

        IOException error = Assert.expectThrows(
                IOException.class,
                () -> generate(List.of())
        );

        Assert.assertEquals(
                error.getMessage(),
                "Missing profile field: skills.display.api"
        );

        Assert.assertFalse(
                Files.exists(outputFile),
                "Invalid profile data should not produce a document."
        );
    }
}