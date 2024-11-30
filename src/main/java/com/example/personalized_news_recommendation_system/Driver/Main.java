package com.example.personalized_news_recommendation_system.Driver;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    // MongoDB connection string and database name
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "News_Recommendation";

    public static ExecutorService executorService;  // Declare ExecutorService here

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize ExecutorService with a fixed thread pool
        executorService = Executors.newFixedThreadPool(4);

        // Connect to MongoDB server
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

        // Load the home page (homePage.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/homePage.fxml"));
        Scene scene = new Scene(loader.load());


        // Access the home page controller and set MongoDB dependencies
        homePage homeController = loader.getController();
        homeController.setMongoClient(mongoClient);
        homeController.setDatabase(database);

        // Set up the stage and scene
        stage.setScene(scene);
        stage.setTitle("Personalized News Recommendation System - Home");
        stage.show();

        // Ensure MongoClient is closed when the application exits
        stage.setOnCloseRequest(event -> {
            mongoClient.close();
            executorService.shutdown(); // Properly shut down ExecutorService
        });
    }

    // Main method to launch the application
    public static void main(String[] args) {
        launch(args);
    }
}
