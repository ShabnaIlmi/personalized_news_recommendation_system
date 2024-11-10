package com.example.personalized_news_recommendation_system;

import org.bson.Document;

public class article {
    private String articleName;
    private String category;
    private String author;
    private String publishedDate;
    private String content;

    public article(String articleName, String category, String author, String publishedDate, String content) {
        this.articleName = articleName;
        this.category = category;
        this.author = author;
        this.publishedDate = publishedDate;
        this.content = content;
    }

    // Getters and Setters
    public String getArticleName() { return articleName; }
    public void setArticleName(String articleName) { this.articleName = articleName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Document toDocument() {
        return new Document("article_name", articleName)
                .append("category", category)
                .append("author", author)
                .append("published_date", publishedDate)
                .append("content", content);
    }
}
