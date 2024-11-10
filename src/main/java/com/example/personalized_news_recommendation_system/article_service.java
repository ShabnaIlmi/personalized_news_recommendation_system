package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class article_service {
    private static final Logger logger = LoggerFactory.getLogger(article_service.class);
    private final MongoCollection<Document> articleCollection;

    public article_service(MongoCollection<Document> articleCollection) {
        this.articleCollection = articleCollection;
    }

    public void addArticle(article article) {
        Document articleDocument = article.toDocument();
        articleCollection.insertOne(articleDocument);
        logger.info("Article added to the database: {}", article);
    }
}
