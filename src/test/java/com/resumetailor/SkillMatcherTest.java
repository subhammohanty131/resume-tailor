package com.resumetailor;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SkillMatcherTest {

    private final SkillMatcher matcher = new SkillMatcher();

    @Test
    public void shouldMatchAliasesIgnoringCase() {

        CandidateProfile profile = new CandidateProfile(
                "Test Candidate",
                List.of(
                        "Core Java",
                        "Selenium WebDriver",
                        "Page Object Model",
                        "PostgreSQL"
                )
        );

        List<String> actual = matcher.findMatchingSkills(
                profile,
                "We require JAVA, selenium, POM and Postgres."
        );

        Assert.assertEquals(
                actual,
                profile.getSkills(),
                "All four aliases should match."
        );
    }

    @Test
    public void shouldRejectPartialWordMatches() {

        CandidateProfile profile = new CandidateProfile(
                "Test Candidate",
                List.of("Core Java", "SQL")
        );

        List<String> actual = matcher.findMatchingSkills(
                profile,
                "We require JavaScript and MySQL."
        );

        Assert.assertTrue(
                actual.isEmpty(),
                "JavaScript and MySQL must not match Java and SQL."
        );
    }

    @Test
    public void shouldMatchPhrasesAcrossLineBreaks() {

        CandidateProfile profile = new CandidateProfile(
                "Test Candidate",
                List.of("Manual Testing")
        );

        List<String> actual = matcher.findMatchingSkills(
                profile,
                "Experience in manual\n testing is required."
        );

        Assert.assertEquals(actual, List.of("Manual Testing"));
    }

    @Test
    public void shouldHandleEmptyAndNullDescriptions() {

        CandidateProfile profile = new CandidateProfile(
                "Test Candidate",
                List.of("SQL")
        );

        Assert.assertTrue(
                matcher.findMatchingSkills(profile, "   ").isEmpty()
        );

        Assert.assertTrue(
                matcher.findMatchingSkills(profile, null).isEmpty()
        );
    }
}