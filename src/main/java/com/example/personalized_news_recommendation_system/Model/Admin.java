package com.example.personalized_news_recommendation_system.Model;

public class Admin extends Person {
    private String jobTitle;

    // Constructor for Admin class
    public Admin(String firstName, String lastName, String email, int age, String gender,
                 String contactNumber, String username, String password, String jobTitle) {
        super(firstName, lastName, email, age, gender, contactNumber, username, password);
        this.jobTitle = jobTitle;
    }

    // Getter for jobTitle
    public String getJobTitle() { return jobTitle; }

    // Implement abstract method
    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String toString() {
        return super.toString() + ", jobTitle='" + jobTitle + '\'' + ", role='Admin'}";
    }
}

