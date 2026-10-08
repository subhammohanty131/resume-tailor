package com.resumetailor;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ResumeTailorerTest {

    private final ResumeTailorer tailorer = new ResumeTailorer();

    @Test
    public void shouldPrioritiseRelevantBulletsAndKeepEveryEntry() {

        List<String> original = List.of(
                "Created HTML reports.",
                "Validated balances using SQL.",
                "Built Selenium WebDriver checks."
        );

        List<String> actual = tailorer.prioritise(
                original,
                List.of("SQL")
        );

        Assert.assertEquals(
                actual,
                List.of(
                        "Validated balances using SQL.",
                        "Created HTML reports.",
                        "Built Selenium WebDriver checks."
                )
        );

        Assert.assertEquals(actual.size(), original.size());
        Assert.assertTrue(actual.containsAll(original));
    }

    @Test
    public void shouldPreserveOriginalOrderForEqualScores() {

        List<String> original = List.of(
                "Validated balances using SQL.",
                "Validated transactions using SQL.",
                "Created HTML reports."
        );

        List<String> actual = tailorer.prioritise(
                original,
                List.of("SQL")
        );

        Assert.assertEquals(
                actual,
                original,
                "Equal-score bullets should retain their order."
        );
    }

    @Test
    public void shouldPreserveOrderWhenNoSkillsMatch() {

        List<String> original = List.of(
                "Created HTML reports.",
                "Documented test results."
        );

        List<String> actual = tailorer.prioritise(
                original,
                List.of()
        );

        Assert.assertEquals(actual, original);
    }

    @Test
    public void shouldNotModifyTheOriginalList() {

        List<String> original = new ArrayList<>(List.of(
                "Created reports.",
                "Validated data using SQL."
        ));

        List<String> before = new ArrayList<>(original);

        List<String> ranked = tailorer.prioritise(
                original,
                List.of("SQL")
        );

        Assert.assertEquals(
                original,
                before,
                "The original profile content must remain unchanged."
        );

        Assert.assertNotSame(ranked, original);

        Assert.assertEquals(
                ranked.get(0),
                "Validated data using SQL."
        );
    }
}