package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class database_connection {
    private static final Logger logger = LoggerFactory.getLogger(database_connection.class);
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "User";

    private MongoClient mongoClient;
    private MongoDatabase database;

    public database_connection() {
        this.mongoClient = MongoClients.create(new ConnectionString(CONNECTION_STRING));
        this.database = mongoClient.getDatabase(DATABASE_NAME);
        logger.info("Connected to MongoDB database: {}", DATABASE_NAME);
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB client connection closed.");
        }
    }
}
