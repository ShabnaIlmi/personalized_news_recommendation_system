package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class log_in {

    @FXML
    public Button signIn;
    @FXML
    public Button userSignHome;
    @FXML
    private TextField userSignUsername;
    @FXML
    private PasswordField userSignPassword;

    private MongoClient mongoClient;
    private MongoCollection<Document> userCollection;

    // Setter for MongoClient and MongoDatabase to initialize userCollection
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in sign_in controller.");
    }

    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.userCollection = database.getCollection("User");
            System.out.println("Connected to User collection with " + userCollection.countDocuments() + " documents.");
        } else {
            System.out.println("Database is null in sign_in controller.");
        }
    }

    @FXML
    void signIn(ActionEvent event) {
        String username = userSignUsername.getText();
        String password = userSignPassword.getText();

        System.out.println("Username entered: " + username);
        System.out.println("Password entered: " + password);

        try {
            // Check if the user with the given username exists in the database
            Document usernameQuery = new Document("username", username);
            Document userDoc = userCollection.find(usernameQuery).first();

            if (userDoc != null) {
                System.out.println("User document found: " + userDoc.toJson());

                String storedPassword = userDoc.getString("password");
                System.out.println("Stored password: " + storedPassword);

                if (storedPassword != null && storedPassword.equals(password)) {
                    String firstName = userDoc.getString("first_name");
                    String lastName = userDoc.getString("last_name");

                    // Show a successful sign-in alert and navigate to the main menu immediately
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Sign-In Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Welcome, " + firstName + " " + lastName + "!");
                        alert.show();

                        navigateToMainMenu(event);
                    });

                } else {
                    System.out.println("Password entered does not match stored password.");
                    showAlert("Sign-In Failed", "Invalid password.");
                }
            } else {
                System.out.println("No user found for username: " + username);
                showAlert("Sign-In Failed", "User not found.");
            }
        } catch (Exception e) {
            System.out.println("Database query failed: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while accessing the database.");
        }
    }

    private void navigateToMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainMenu.fxml"));
            Scene mainMenuScene = new Scene(loader.load());

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(mainMenuScene);
            currentStage.setTitle("Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the main menu.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Handler for the 'userSignHome' button to navigate back to the home page
    @FXML
    public void userSignHome(ActionEvent actionEvent) {
        try {
            // Load the home page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homePage.fxml"));
            Scene homeScene = new Scene(loader.load());

            // Pass the MongoClient and MongoDatabase to the home page controller
            homePage homeController = loader.getController();
            homeController.setMongoClient(mongoClient);
            homeController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Get the current stage and set the home scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.setTitle("Personalized News Recommendation System - Home");
            currentStage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the home page.");
            e.printStackTrace();
        }
    }
}
