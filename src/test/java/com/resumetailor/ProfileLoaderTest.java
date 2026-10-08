package com.resumetailor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ProfileLoaderTest {

    private CandidateProfile loadText(String content)
            throws IOException {

        Path file = Files.createTempFile("candidate-test-", ".properties");

        try {
            Files.writeString(
                    file, content, StandardCharsets.UTF_8);

            return new ProfileLoader().load(file);

        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void shouldLoadProfileAndSections() throws IOException {

        CandidateProfile profile = loadText("""
                name=Test Candidate
                skills=Java,SQL
                contact=test@example.com
                summary=Test profile summary.
                project.1=Example project
                education.1=Example degree
                certification.1=Example workshop
                """);

        Assert.assertEquals(profile.getName(), "Test Candidate");

        Assert.assertEquals(
                profile.getSkills(),
                List.of("Java", "SQL")
        );

        Assert.assertEquals(
                profile.getContact(),
                "test@example.com"
        );

        Assert.assertEquals(
                profile.getSections().get("Professional Summary"),
                List.of("Test profile summary.")
        );

        Assert.assertEquals(
                profile.getSections().get("Projects"),
                List.of("Example project")
        );

        Assert.assertEquals(
                profile.getSections().get("Courses and Workshops"),
                List.of("Example workshop")
        );
    }

    @Test
    public void shouldTrimAndDeduplicateSkills() throws IOException {

        CandidateProfile profile = loadText("""
                name=  Test Candidate
                skills= Java, SQL, Java, , SQL,
                """);

        Assert.assertEquals(profile.getName(), "Test Candidate");

        Assert.assertEquals(
                profile.getSkills(),
                List.of("Java", "SQL")
        );

        Assert.assertTrue(profile.getSections().isEmpty());
    }

    @Test
    public void shouldRejectMissingName() {

        IOException error = Assert.expectThrows(
                IOException.class,
                () -> loadText("skills=SQL\n")
        );

        Assert.assertEquals(
                error.getMessage(),
                "Candidate name is required."
        );
    }

    @Test
    public void shouldRejectEmptySkills() {

        IOException error = Assert.expectThrows(
                IOException.class,
                () -> loadText("""
                        name=Test Candidate
                        skills= , ,
                        """)
        );

        Assert.assertEquals(
                error.getMessage(),
                "At least one candidate skill is required."
        );
    }

    @Test
    public void shouldRejectMissingFile() throws IOException {

        Path directory = Files.createTempDirectory("missing-profile-");

        try {
            Path missingFile = directory.resolve("missing.properties");

            Assert.expectThrows(
                    IOException.class,
                    () -> new ProfileLoader().load(missingFile)
            );

        } finally {
            Files.deleteIfExists(directory);
        }
    }
}