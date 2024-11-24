package com.example.personalized_news_recommendation_system;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Article {
    private String name;
    private String category;
    private String author;
    private LocalDate publishedDate;
    private String description; // Article description
    private String content; // Full article content

    // Constructor with description and content
    public Article(String name, String category, String author, String publishedDate, String description, String content) {
        this.name = name;
        this.category = category;
        this.author = author;
        this.description = description;
        this.content = content;

        // Check if the publishedDate is null or empty and handle accordingly
        if (publishedDate != null && !publishedDate.trim().isEmpty()) {
            try {
                this.publishedDate = LocalDate.parse(publishedDate);  // Try to parse the date
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format for article: " + name + ". Setting to default.");
                this.publishedDate = LocalDate.now();  // Default to current date on failure
            }
        } else {
            this.publishedDate = LocalDate.now();  // Default to current date if null or empty
        }
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
