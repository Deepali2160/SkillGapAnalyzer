package com.deepali.skillgap.model;

public class JobPosting {

    private String role;
    private String title;
    private String description;

    public JobPosting(String role, String title, String description) {
        this.role = role;
        this.title = title;
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
