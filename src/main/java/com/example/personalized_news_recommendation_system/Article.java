package com.example.personalized_news_recommendation_system;

import java.time.LocalDate;

public class Article {
    private String name;
    private String category;
    private String author;
    private LocalDate publishedDate;

    public Article(String name, String category, String author, String publishedDate) {
        this.name = name;
        this.category = category;
        this.author = author;
        this.publishedDate = LocalDate.parse(publishedDate);
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }


}

