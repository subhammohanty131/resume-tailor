package com.resumetailor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkillMatcher {

    private static final Map<String, List<String>> ALIASES =
            createAliases();

    public List<String> findMatchingSkills(
            CandidateProfile profile,
            String jobDescription) {

        List<String> matches = new ArrayList<>();

        if (jobDescription == null || jobDescription.isBlank()) {
            return matches;
        }

        for (String skill : profile.getSkills()) {

            String key = skill.toLowerCase(Locale.ROOT);

            List<String> phrases = ALIASES.getOrDefault(
                    key, List.of(skill));

            boolean matched = phrases.stream()
                    .anyMatch(phrase ->
                            containsPhrase(jobDescription, phrase));

            if (matched) {
                matches.add(skill);
            }
        }

        return matches;
    }

    private boolean containsPhrase(String text, String phrase) {

        // Allow spaces, tabs, or line breaks between phrase words.
        String phraseExpression =
                Pattern.compile("\\s+")
                        .splitAsStream(phrase.trim())
                        .map(Pattern::quote)
                        .collect(Collectors.joining("\\s+"));

        // Prevent Java from matching JavaScript,
        // or SQL from matching MySQL.
        String expression =
                "(?<![\\p{L}\\p{N}_])"
                + phraseExpression
                + "(?![\\p{L}\\p{N}_])";

        return Pattern.compile(
                expression,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        ).matcher(text).find();
    }

    private static Map<String, List<String>> createAliases() {

        Map<String, List<String>> aliases = new LinkedHashMap<>();

        aliases.put("core java", List.of(
                "Core Java",
                "Java"
        ));

        aliases.put("selenium webdriver", List.of(
                "Selenium WebDriver",
                "Selenium"
        ));

        aliases.put("page object model", List.of(
                "Page Object Model",
                "POM"
        ));

        aliases.put("oop", List.of(
                "OOP",
                "OOPs",
                "Object Oriented Programming",
                "Object-Oriented Programming"
        ));

        aliases.put("rtm", List.of(
                "RTM",
                "Requirement Traceability Matrix",
                "Requirements Traceability Matrix"
        ));

        aliases.put("rest assured", List.of(
                "REST Assured",
                "REST-Assured",
                "RestAssured"
        ));

        aliases.put("postgresql", List.of(
                "PostgreSQL",
                "Postgres"
        ));

        aliases.put("rest apis", List.of(
                "REST APIs",
                "REST API",
                "RESTful APIs",
                "RESTful API"
        ));

        aliases.put("http methods", List.of(
                "HTTP Methods",
                "HTTP Method"
        ));

        aliases.put("http status codes", List.of(
                "HTTP Status Codes",
                "HTTP Status Code"
        ));

        return aliases;
    }
}