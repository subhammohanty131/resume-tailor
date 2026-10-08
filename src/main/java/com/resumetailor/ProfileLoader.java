package com.resumetailor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ProfileLoader {

    public CandidateProfile load(Path file) throws IOException {

        Properties properties = new Properties();

        try (Reader reader =
                Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            properties.load(reader);
        }

        String name = properties.getProperty("name", "").trim();
        String contact = properties.getProperty("contact", "").trim();
        String summary = properties.getProperty("summary", "").trim();
        String skillsText = properties.getProperty("skills", "");

        List<String> skills = Arrays.stream(skillsText.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .distinct()
                .toList();

        if (name.isEmpty()) {
            throw new IOException("Candidate name is required.");
        }

        if (skills.isEmpty()) {
            throw new IOException(
                    "At least one candidate skill is required.");
        }

        Map<String, List<String>> sections = new LinkedHashMap<>();

        if (!summary.isEmpty()) {
            sections.put("Professional Summary", List.of(summary));
        }

        String[][] sectionDefinitions = {
                {"experience", "Professional Experience"},
                {"project", "Projects"},
                {"education", "Education"},
                {"certification", "Courses and Workshops"}
        };

        for (String[] definition : sectionDefinitions) {

            String prefix = definition[0];
            String heading = definition[1];

            List<String> entries = new ArrayList<>();

            for (int number = 1; number <= 20; number++) {

                String entry = properties.getProperty(
                        prefix + "." + number, "").trim();

                if (!entry.isEmpty()) {
                    entries.add(entry);
                }
            }

            if (!entries.isEmpty()) {
                sections.put(heading, entries);
            }
        }

        return new CandidateProfile(
                name,
                skills,
                contact,
                sections
        );
    }
}