package com.example.personalized_news_recommendation_system;

import java.time.LocalDate;

public class Article {
    private String id;
    private String name;
    private String category;
    private String author;
    private LocalDate publishedDate;
    private String description;
    private String content;

    public Article(String id, String name, String category, String author, String publishedDate, String description, String content) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.author = author;
        this.publishedDate = LocalDate.parse(publishedDate);
        this.description = description;
        this.content = content;

    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", author='" + author + '\'' +
                ", publishedDate=" + publishedDate +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                '}';
    }



}

