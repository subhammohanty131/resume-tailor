package com.resumetailor;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class AiContentValidatorTest {

    private static final String ORIGINAL =
            "QA-focused IT professional with manual testing and banking "
                    + "automation portfolio experience using Java, Selenium, "
                    + "TestNG, REST Assured, and JDBC/SQL. Built reusable "
                    + "page objects, UI/API/database checks, screenshot "
                    + "evidence, HTML reports, and a GitHub Actions "
                    + "regression workflow. Seeking QA Tester or junior "
                    + "QA Automation opportunities.";

    private final AiContentValidator validator =
            new AiContentValidator();

    private final CandidateProfile profile = new CandidateProfile(
            "Test Candidate",
            List.of(
                    "Core Java",
                    "Selenium WebDriver",
                    "TestNG",
                    "REST Assured",
                    "JDBC",
                    "SQL",
                    "Postman",
                    "Defect Reporting",
                    "GitHub Actions"
            )
    );

    @Test
    public void acceptsFaithfulRewrite() {
        String rewritten =
                "QA-focused IT professional with manual testing and "
                        + "banking automation portfolio experience using "
                        + "Java, Selenium, TestNG, REST Assured, and JDBC/SQL. "
                        + "Built reusable page objects, UI/API/database "
                        + "validation checks, screenshot evidence, HTML "
                        + "reports, and a GitHub Actions regression workflow. "
                        + "Seeking Junior QA Engineer opportunities.";

        Assert.assertTrue(
                validator.validateSummary(
                        ORIGINAL, rewritten, profile
                ).isEmpty()
        );
    }

    @Test
    public void rejectsNewProficiencyClaim() {
        assertIssue(
                ORIGINAL + " Proficient in automation.",
                "proficient"
        );
    }

    @Test
    public void flagsNewActivityEvenWhenInCandidateSkills() {
        assertIssue(
                ORIGINAL + " Experienced in defect reporting.",
                "Defect Reporting"
        );
    }

    @Test
    public void flagsNewToolEvenWhenInCandidateSkills() {
        assertIssue(
                ORIGINAL + " Used Postman.",
                "Postman"
        );
    }

    @Test
    public void rejectsRemovedPortfolioQualifier() {
        assertIssue(
                ORIGINAL.replace("portfolio ", ""),
                "portfolio"
        );
    }

    @Test
    public void rejectsInventedNumber() {
        assertIssue(
                ORIGINAL + " Executed 100 tests.",
                "Numeric facts"
        );
    }

    @Test
    public void rejectsChangedNumber() {
        String source = ORIGINAL + " Executed 70 tests.";
        String rewritten = ORIGINAL + " Executed 90 tests.";

        List<String> issues = validator.validateSummary(
                source, rewritten, profile
        );

        Assert.assertTrue(
                issues.stream().anyMatch(
                        issue -> issue.contains("Numeric facts")
                )
        );
    }

    @Test
    public void rejectsContactInformation() {
        assertIssue(
                ORIGINAL + " Contact qa@example.com.",
                "URL or email"
        );
    }

    @Test
    public void rejectsMultipleParagraphs() {
        assertIssue(
                ORIGINAL + "\nAvailable for QA roles.",
                "plain-text paragraph"
        );
    }

    @Test
    public void rejectsEmptySuggestion() {
        Assert.assertFalse(
                validator.validateSummary(
                        ORIGINAL, " ", profile
                ).isEmpty()
        );
    }

    @Test
    public void preservesFundamentalsQualifier() {
        String source =
                "QA candidate with portfolio experience in Java and "
                        + "REST Assured fundamentals for banking checks.";

        String rewritten =
                "QA candidate with portfolio experience in Java and "
                        + "REST Assured for banking checks.";

        List<String> issues = validator.validateSummary(
                source, rewritten, profile
        );

        Assert.assertTrue(
                issues.stream().anyMatch(
                        issue -> issue.contains("fundamentals")
                )
        );
    }

    @Test
    public void acceptsJavaAndSeleniumAliases() {
        String source =
                "QA candidate with portfolio experience using Core Java "
                        + "and Selenium WebDriver for banking workflow checks.";

        String rewritten =
                "QA candidate with portfolio experience using Java "
                        + "and Selenium for banking workflow checks.";

        Assert.assertTrue(
                validator.validateSummary(
                        source, rewritten, profile
                ).isEmpty()
        );
    }

    private void assertIssue(String rewritten, String expectedText) {
        List<String> issues = validator.validateSummary(
                ORIGINAL, rewritten, profile
        );

        Assert.assertTrue(
                issues.stream().anyMatch(
                        issue -> issue.contains(expectedText)
                ),
                "Expected issue containing: " + expectedText
                        + "; actual issues: " + issues
        );
    }
}