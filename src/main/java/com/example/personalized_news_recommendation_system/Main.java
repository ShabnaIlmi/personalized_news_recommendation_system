package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "News_Recommendation";

    @Override
    public void start(Stage stage) throws Exception {
        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign_in.fxml"));
        Scene scene = new Scene(loader.load());

        // Access the controller and inject the database
        sign_in controller = loader.getController();
        controller.setDatabase(database);

        // Set up the stage
        stage.setScene(scene);
        stage.setTitle("Personalized News Recommendation System - Sign In");
        stage.show();

        // Close MongoDB connection on exit
        stage.setOnCloseRequest(event -> mongoClient.close());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
