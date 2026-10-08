package com.resumetailor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResumeTailorer {

    public List<String> prioritise(
            List<String> entries,
            List<String> matchingSkills) {

        List<String> ranked = new ArrayList<>(entries);

        if (matchingSkills.isEmpty()) {
            return ranked;
        }

        CandidateProfile relevantSkills = new CandidateProfile(
                "Content Ranking",
                matchingSkills
        );

        SkillMatcher matcher = new SkillMatcher();

        // Higher match counts appear first.
        // Equal scores retain their original order.
        ranked.sort(
                Comparator.comparingInt(
                        (String entry) -> matcher.findMatchingSkills(
                                relevantSkills, entry).size()
                ).reversed()
        );

        return ranked;
    }
}