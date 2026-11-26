package com.deepali.skillgap.model;

import java.util.List;

public class GapResult {

    private String role;
    private double matchPercent;
    private List<SkillGapEntry> entries;
    private List<String> missingSkills;

    public GapResult(String role, double matchPercent, List<SkillGapEntry> entries, List<String> missingSkills) {
        this.role = role;
        this.matchPercent = matchPercent;
        this.entries = entries;
        this.missingSkills = missingSkills;
    }

    public String getRole() {
        return role;
    }

    public double getMatchPercent() {
        return matchPercent;
    }

    public List<SkillGapEntry> getEntries() {
        return entries;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }
}
