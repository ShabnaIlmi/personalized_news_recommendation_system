package com.example.personalized_news_recommendation_system.Driver;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // MongoDB connection string and database name
    private static final String CONNECTION_STRING = "mongodb+srv://Shabna_2409661:VictoriousEstablishment%40123@cluster0.wdxej.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private static final String DATABASE_NAME = "News_Recommendation";

    private MongoClient mongoClient; // Instance variable for MongoClient
    private ExecutorService executorService; // Instance variable for ExecutorService

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Initialize ExecutorService with a fixed thread pool
            executorService = Executors.newFixedThreadPool(4);

            // Connect to MongoDB server
            mongoClient = MongoClients.create(CONNECTION_STRING);
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
                shutdownResources();
            });
        } catch (Exception e) {
            logger.error("Error initializing the application", e);
            throw e; // Re-throw exception to prevent silent failure
        }
    }

    private void shutdownResources() {
        if (mongoClient != null) {
            mongoClient.close(); //MongoClient Closed

        }
        if (executorService != null) {
            executorService.shutdown(); //Executor Service Shutdown
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
