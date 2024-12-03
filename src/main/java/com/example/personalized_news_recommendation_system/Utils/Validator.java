package com.example.personalized_news_recommendation_system.Utils;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Validator {

    // Email validation method
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Age validation method
    public static boolean isValidAge(String age) {
        try {
            int ageInt = Integer.parseInt(age);
            return ageInt > 0 && ageInt < 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Password validation method (at least 8 characters, uppercase, lowercase, and a number)
    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        return password.matches(passwordRegex);
    }

    // Category validation method (to ensure categories are distinct)
    public static boolean areCategoriesDistinct(String category1, String category2, String category3) {
        return !(category1.equals(category2) || category1.equals(category3) || category2.equals(category3));
    }

    // Validate if username and password are not empty
    public static boolean areFieldsNotEmpty(String username, String password) {
        return !(username == null || username.isEmpty() || password == null || password.isEmpty());
    }

    // Authenticate the user
    public static boolean authenticateUser(MongoCollection<Document> userCollection, String username, String password) {
        Document user = userCollection.find(new Document("username", username)).first();
        return user != null && user.getString("password").equals(password);
    }

    // Validate if the collections are set
    public static boolean areCollectionsSet(MongoCollection<Document> userCollection, MongoCollection<Document> userLogCollection) {
        return userCollection != null && userLogCollection != null;
    }

    // Authenticate an admin from the admin MongoDB collection
    public static boolean authenticateAdmin(MongoCollection<Document> adminCollection, String username, String password) {
        if (adminCollection == null || username == null || password == null) {
            return false;
        }

        Document adminDoc = adminCollection.find(new Document("username", username)).first();
        if (adminDoc != null) {
            String storedPassword = adminDoc.getString("password");
            return storedPassword != null && storedPassword.equals(password);
        }

        return false;
    }

    // Validation for MongoDB collection (articles collection)
    public static boolean isArticleCollectionValid(MongoCollection<Document> articlesCollection) {
        return articlesCollection != null;
    }

    // Validate if article fields are not empty
    public static boolean areArticleFieldsNotEmpty(String id, String title, String author, String publishedDate, String articleDescription, String content) {
        return !(id == null || id.isEmpty() ||
                title == null || title.isEmpty() ||
                author == null || author.isEmpty() ||
                publishedDate == null || publishedDate.isEmpty() ||
                articleDescription == null || articleDescription.isEmpty() ||
                content == null || content.isEmpty());
    }

    // Validate if the article ID is unique
    public static boolean isUniqueArticleID(MongoCollection<Document> articlesCollection, String articleID) {
        return articlesCollection.find(new Document("article_id", articleID)).first() == null;
    }

    // Validate if the article content is unique
    public static boolean isUniqueArticleContent(MongoCollection<Document> articlesCollection, String content) {
        return articlesCollection.find(new Document("content", content)).first() == null;
    }

    // Validate if the category is valid (it matches a predefined set of categories)
    public static boolean isValidCategory(String category, String[] validCategories) {
        for (String validCategory : validCategories) {
            if (validCategory.equals(category)) {
                return true;
            }
        }
        return false;
    }

    // Validate input fields
    public static void validateInputFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("All fields must be filled before submitting.");
            }
        }
    }

    // Validate MongoDB collection
    public static void validateMongoCollection(MongoCollection<Document> collection) {
        if (collection == null) {
            throw new IllegalStateException("MongoDB collection not initialized.");
        }
    }

    // Check for duplicate articles by ID
    public static boolean isDuplicateArticle(String id, MongoCollection<Document> collection) {
        return collection.find(new Document("article_id", id)).first() != null;
    }

    // Check for duplicate articles by content
    public static boolean isDuplicateContent(String content, MongoCollection<Document> collection) {
        return collection.find(new Document("content", content)).first() != null;
    }
}


