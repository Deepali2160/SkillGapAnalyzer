package com.deepali.skillgap.model;

public class SkillGapEntry {

    private String skillName;
    private double demandPercent;
    private boolean has;

    public SkillGapEntry(String skillName, double demandPercent, boolean has) {
        this.skillName = skillName;
        this.demandPercent = demandPercent;
        this.has = has;
    }

    public String getSkillName() {
        return skillName;
    }

    public double getDemandPercent() {
        return demandPercent;
    }

    public boolean isHas() {
        return has;
    }

    // Thymeleaf/EL friendly getter (so ${entry.has} resolves correctly)
    public boolean getHas() {
        return has;
    }
}
