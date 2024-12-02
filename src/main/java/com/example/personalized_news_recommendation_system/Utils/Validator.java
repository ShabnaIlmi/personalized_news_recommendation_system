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

}
