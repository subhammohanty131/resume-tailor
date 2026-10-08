package com.resumetailor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiContentValidator {

    private static final Pattern NUMBERS =
            Pattern.compile("\\d+(?:\\.\\d+)?%?");

    private static final Pattern CONTACT =
            Pattern.compile(
                    "(?i)https?://|www\\.|"
                            + "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}"
            );

    private static final List<String> STRONG_CLAIMS = List.of(
            "expert",
            "expertise",
            "proficient",
            "proficiency",
            "advanced",
            "extensive",
            "seasoned",
            "certified",
            "senior",
            "specialist",
            "specialized",
            "specialised",
            "professional experience",
            "industry experience",
            "production experience",
            "years of experience",
            "led a team",
            "managed a team",
            "proven track record"
    );

    private static final List<String> ACTIVITIES = List.of(
            "Defect Reporting",
            "Defect Tracking",
            "Test Planning",
            "Test Cases",
            "Test Scenarios",
            "RTM",
            "Performance Testing",
            "Security Testing",
            "Load Testing",
            "Accessibility Testing",
            "Mobile Testing"
    );

    private static final List<String> EXTRA_TECHNOLOGIES = List.of(
            "Java",
            "Selenium",
            "TestNG",
            "REST Assured",
            "JDBC",
            "SQL",
            "GitHub Actions",
            "Postman",
            "PostgreSQL",
            "HSQLDB",
            "Maven",
            "AWS",
            "Azure",
            "Docker",
            "Kubernetes",
            "Jenkins",
            "Cypress",
            "Playwright",
            "Python",
            "JavaScript",
            "TypeScript",
            "MongoDB",
            "MySQL",
            "Oracle",
            "Appium",
            "JMeter",
            "Cucumber"
    );

    public List<String> validateSummary(
            String original,
            String rewritten,
            CandidateProfile profile
    ) {
        List<String> issues = new ArrayList<>();

        if (original == null || original.isBlank()) {
            issues.add("The original summary is empty.");
            return issues;
        }

        if (rewritten == null || rewritten.isBlank()) {
            issues.add("The AI summary is empty.");
            return issues;
        }

        String source = original.trim();
        String suggestion = rewritten.trim();

        if (suggestion.length() < 30) {
            issues.add("The AI summary is too short.");
        }

        if (suggestion.length() > source.length() + 100) {
            issues.add(
                    "The AI summary is substantially longer than the original."
            );
        }

        if (suggestion.contains("\n")
                || suggestion.contains("\r")
                || suggestion.contains("\\")
                || suggestion.contains("`")
                || suggestion.contains("**")
                || suggestion.contains("##")) {
            issues.add(
                    "The AI summary must be one plain-text paragraph."
            );
        }

        if (CONTACT.matcher(suggestion).find()) {
            issues.add(
                    "The AI summary contains a URL or email address."
            );
        }

        if (!numberCounts(source).equals(numberCounts(suggestion))) {
            issues.add(
                    "Numeric facts were added, removed, or changed."
            );
        }

        preserveQualifier(source, suggestion, "portfolio", issues);
        preserveQualifier(source, suggestion, "fundamentals", issues);
        preserveQualifier(source, suggestion, "basics", issues);
        preserveQualifier(source, suggestion, "beginner", issues);
        preserveQualifier(source, suggestion, "introductory", issues);

        if (containsPhrase(source, "project experience")
                && !containsPhrase(suggestion, "project experience")
                && !containsPhrase(suggestion, "portfolio")) {
            issues.add(
                    "The project-experience qualifier was removed."
            );
        }

        for (String claim : STRONG_CLAIMS) {
            if (containsPhrase(suggestion, claim)
                    && !containsPhrase(source, claim)) {
                issues.add(
                        "New proficiency or experience claim: " + claim
                );
            }
        }

        Set<String> catalog = new LinkedHashSet<>();

        if (profile != null) {
            catalog.addAll(profile.getSkills());
        }

        catalog.addAll(EXTRA_TECHNOLOGIES);
        catalog.addAll(ACTIVITIES);

        CandidateProfile vocabulary = new CandidateProfile(
                "Validation Vocabulary",
                new ArrayList<>(catalog)
        );

        SkillMatcher matcher = new SkillMatcher();

        Set<String> sourceSkills = new LinkedHashSet<>(
                matcher.findMatchingSkills(vocabulary, source)
        );

        List<String> suggestedSkills =
                matcher.findMatchingSkills(vocabulary, suggestion);

        for (String skill : suggestedSkills) {
            if (!sourceSkills.contains(skill)) {
                issues.add(
                        "Skill or activity absent from the original summary: "
                                + skill
                );
            }
        }

        return List.copyOf(issues);
    }

    private void preserveQualifier(
            String original,
            String rewritten,
            String qualifier,
            List<String> issues
    ) {
        if (containsPhrase(original, qualifier)
                && !containsPhrase(rewritten, qualifier)) {
            issues.add(
                    "The experience qualifier was removed: " + qualifier
            );
        }
    }

    private boolean containsPhrase(String text, String phrase) {
        String[] words = phrase.toLowerCase(Locale.ROOT)
                .trim()
                .split("\\s+");

        StringBuilder expression = new StringBuilder(
                "(?<![\\p{L}\\p{N}_])"
        );

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                expression.append("\\s+");
            }

            expression.append(Pattern.quote(words[i]));
        }

        expression.append("(?![\\p{L}\\p{N}_])");

        return Pattern.compile(
                expression.toString(),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        ).matcher(text).find();
    }

    private Map<String, Integer> numberCounts(String text) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Matcher matcher = NUMBERS.matcher(text);

        while (matcher.find()) {
            counts.merge(matcher.group(), 1, Integer::sum);
        }

        return counts;
    }
}