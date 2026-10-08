package com.resumetailor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidateProfile {

    private final String name;
    private final List<String> skills;
    private final String contact;
    private final Map<String, List<String>> sections;

    public CandidateProfile(String name, List<String> skills) {
        this(name, skills, "", Map.of());
    }

    public CandidateProfile(
            String name,
            List<String> skills,
            String contact,
            Map<String, List<String>> sections) {

        this.name = name;
        this.skills = List.copyOf(skills);
        this.contact = contact;

        Map<String, List<String>> copy = new LinkedHashMap<>();
        sections.forEach((title, entries) ->
                copy.put(title, List.copyOf(entries)));

        this.sections = Collections.unmodifiableMap(copy);
    }

    public String getName() {
        return name;
    }

    public List<String> getSkills() {
        return skills;
    }

    public String getContact() {
        return contact;
    }

    public Map<String, List<String>> getSections() {
        return sections;
    }
}