package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    // MongoDB connection string and database name
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "News_Recommendation";

    @Override
    public void start(Stage stage) throws Exception {
        // Connect to MongoDB server
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

        // Load the home page (home.fxml file path)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("homePage.fxml"));
        Scene scene = new Scene(loader.load());

        // Access the homePage controller
        homePage homeController = loader.getController();

        // Set up the MongoDB client and database in case needed
        homeController.setMongoClient(mongoClient);
        homeController.setDatabase(database);

        // Set up the stage and scene
        stage.setScene(scene);
        stage.setTitle("Personalized News Recommendation System - Home");
        stage.show();

        // Ensure MongoClient is closed when the application exits
        stage.setOnCloseRequest(event -> mongoClient.close());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
