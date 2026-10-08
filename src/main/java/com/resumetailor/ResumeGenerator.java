package com.resumetailor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResumeGenerator {

    public Path generate(
            CandidateProfile profile,
            List<String> matchingSkills) throws IOException {

        return generate(
                profile,
                matchingSkills,
                Path.of("data", "candidate.properties"),
                Path.of("templates", "resume-template.tex"),
                Path.of("output", "tailored-resume.tex")
        );
    }

    public Path generate(
            CandidateProfile profile,
            List<String> matchingSkills,
            Path profileFile,
            Path templateFile,
            Path outputFile) throws IOException {

        Properties data = new Properties();

        try (Reader reader = Files.newBufferedReader(
                profileFile,
                StandardCharsets.UTF_8)) {

            data.load(reader);
        }

        String template = Files.readString(
                templateFile,
                StandardCharsets.UTF_8);

        String[] labels = {
                "Manual Testing",
                "Automation and Java",
                "API Testing",
                "Database Testing",
                "Tools and Practices"
        };

        String[] keys = {
                "manual",
                "automation",
                "api",
                "database",
                "tools"
        };

        List<String> skillLines = new ArrayList<>();

        for (int i = 0; i < keys.length; i++) {

            String description = required(
                    data,
                    "skills.display." + keys[i]);

            skillLines.add(
                    "\\textbf{"
                    + escape(labels[i])
                    + ":} "
                    + escape(description)
            );
        }

        String projects =
                project(data, 1, 2, 5, matchingSkills)
                + project(data, 6, 7, 9, matchingSkills);

        String[] job = required(data, "experience.1")
                .split("\\s*\\|\\s*");

        if (job.length != 4) {
            throw new IOException(
                    "experience.1 needs "
                    + "Company | Role | Location | Dates");
        }

        String experience =
                "\\textbf{"
                + escape(job[0])
                + "}\\hfill\\textbf{"
                + escape(job[3]).replace("-", "--")
                + "}\\\\\n"
                + escape(job[1])
                + "\\hfill "
                + escape(job[2])
                + "\n"
                + bullets(
                        numbered(data, "experience", 2, 20),
                        true
                );

        StringBuilder education = new StringBuilder();

        for (int i = 1; i <= 20; i++) {

            String entry = data.getProperty(
                    "education." + i,
                    "").trim();

            if (entry.isEmpty()) {
                continue;
            }

            String[] fields = entry.split("\\s*\\|\\s*");

            if (fields.length != 4) {
                throw new IOException(
                        "education." + i + " needs "
                        + "Qualification | Institution | Year | Result");
            }

            if (education.length() > 0) {
                education.append("\\\\[3pt]\n");
            }

            education.append("\\textbf{")
                     .append(escape(fields[0]))
                     .append("}\\hfill ")
                     .append(escape(fields[2]))
                     .append("\\\\\n")
                     .append(escape(fields[1]))
                     .append("\\hfill ")
                     .append(escape(fields[3]));
        }

        String separator =
                "\\enspace\\textbar{}\\enspace ";

        String email = required(data, "email");
        String phone = required(data, "phone");
        String linkedin = required(data, "linkedin");
        String github = required(data, "github");

        String contact =
                "\\href{mailto:"
                + escape(email)
                + "}{"
                + escape(email)
                + "}"
                + separator
                + escape(phone)
                + separator
                + "\\href{"
                + url(linkedin)
                + "}{LinkedIn}"
                + separator
                + "\\href{"
                + url(github)
                + "}{"
                + escape(github.replaceFirst("^https?://", ""))
                + "}";

        Map<String, String> values = Map.of(
                "NAME",
                escape(profile.getName()),

                "HEADLINE",
                escape(required(data, "headline")),

                "CONTACT",
                contact,

                "SUMMARY",
                escape(required(data, "summary")),

                "SKILLS",
                bullets(skillLines, false),

                "PROJECTS",
                projects,

                "EXPERIENCE",
                experience,

                "EDUCATION",
                education.toString(),

                "COURSES",
                bullets(
                        numbered(data, "certification", 1, 20),
                        true
                )
        );

        Matcher matcher = Pattern.compile("@@([A-Z]+)@@")
                .matcher(template);

        StringBuilder rendered = new StringBuilder();

        while (matcher.find()) {

            String replacement = values.get(
                    matcher.group(1));

            if (replacement == null) {
                throw new IOException(
                        "Unknown template field: "
                        + matcher.group(1));
            }

            matcher.appendReplacement(
                    rendered,
                    Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(rendered);

        Path output = outputFile.toAbsolutePath().normalize();

        Files.createDirectories(output.getParent());

        Files.writeString(
                output,
                rendered.toString(),
                StandardCharsets.UTF_8);

        return output;
    }

    private String project(
            Properties data,
            int titleIndex,
            int first,
            int last,
            List<String> matchingSkills) throws IOException {

        String[] parts = required(
                data,
                "project." + titleIndex)
                .split("\\s*\\|\\s*", 2);

        String repository = parts.length == 2
                ? "\\href{"
                    + url(parts[1])
                    + "}{GitHub Repository}"
                : "GitHub Repository";

        List<String> originalBullets = numbered(
                data,
                "project",
                first,
                last);

        ResumeTailorer tailorer = new ResumeTailorer();

        List<String> rankedBullets = tailorer.prioritise(
                originalBullets,
                matchingSkills);

        return "\\textbf{"
                + escape(parts[0]).replace(" - ", " -- ")
                + "}\\hfill "
                + repository
                + "\n"
                + bullets(rankedBullets, true);
    }

    private List<String> numbered(
            Properties data,
            String prefix,
            int first,
            int last) {

        List<String> entries = new ArrayList<>();

        for (int i = first; i <= last; i++) {

            String value = data.getProperty(
                    prefix + "." + i,
                    "").trim();

            if (!value.isEmpty()) {
                entries.add(value);
            }
        }

        return entries;
    }

    private String bullets(
            List<String> entries,
            boolean escapeEntries) {

        StringBuilder result =
                new StringBuilder("\\begin{itemize}\n");

        for (String entry : entries) {

            result.append("\\item ")
                  .append(
                          escapeEntries
                                  ? escape(entry)
                                  : entry)
                  .append("\n");
        }

        return result.append("\\end{itemize}\n")
                     .toString();
    }

    private String required(
            Properties data,
            String key) throws IOException {

        String value = data.getProperty(key, "").trim();

        if (value.isEmpty()) {
            throw new IOException(
                    "Missing profile field: " + key);
        }

        return value;
    }

    private String url(String value) throws IOException {

        String address = value.matches("^https?://.*")
                ? value
                : "https://" + value;

        if (!address.matches(
                "https?://[A-Za-z0-9./:_?=&%+~#@-]+")) {

            throw new IOException(
                    "Unsupported URL: " + value);
        }

        return address.replace("%", "\\%")
                      .replace("#", "\\#")
                      .replace("&", "\\&");
    }

    private String escape(String text) {

        StringBuilder result = new StringBuilder();

        for (char character : text.toCharArray()) {

            switch (character) {

                case '\\' ->
                        result.append("\\textbackslash{}");

                case '&', '%', '$', '#', '_', '{', '}' ->
                        result.append('\\').append(character);

                case '~' ->
                        result.append("\\textasciitilde{}");

                case '^' ->
                        result.append("\\textasciicircum{}");

                case '|' ->
                        result.append("\\textbar{}");

                default ->
                        result.append(character);
            }
        }

        return result.toString();
    }
}