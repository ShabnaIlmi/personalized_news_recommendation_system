package com.example.personalized_news_recommendation_system.Model;

import java.util.List;

public class User extends Person {
    private List<String> categories;

    // Constructor for User class
    public User(String firstName, String lastName, String email, int age, String gender,
                String contactNumber, String username, String password, List<String> categories) {
        super(firstName, lastName, email, age, gender, contactNumber, username, password);
        this.categories = categories;
    }

    // Getter for categories
    public List<String> getCategories() { return categories; }

    // Implement abstract method
    @Override
    public String getRole() {
        return "User";
    }

    @Override
    public String toString() {
        return super.toString() + ", categories=" + categories + ", role='User'}";
    }
}

