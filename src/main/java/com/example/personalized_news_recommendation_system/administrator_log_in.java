package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

public class administrator_log_in {

    @FXML
    public Button administratorSignIn;
    @FXML
    public Button adminLogInHome;
    @FXML
    private TextField administratorUsername;
    @FXML
    private PasswordField administratorPassword;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> adminCollection;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in AdministratorLogIn controller.");
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.adminCollection = database.getCollection("Admin");
            System.out.println("Connected to Admin collection with " + adminCollection.countDocuments() + " documents.");
        } else {
            System.out.println("Database is null in AdministratorLogIn controller.");
        }
    }

    // Sign-In Method for Administrator
    @FXML
    void administratorSignIn(ActionEvent event) {
        String username = administratorUsername.getText();
        String password = administratorPassword.getText();

        // Check if the username or password fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.");
            return;
        }

        try {
            // Check if the admin with the given username exists in the database
            Document usernameQuery = new Document("username", username);
            Document adminDoc = adminCollection.find(usernameQuery).first();

            if (adminDoc != null) {
                String storedPassword = adminDoc.getString("password");

                if (storedPassword != null && storedPassword.equals(password)) {
                    String firstName = adminDoc.getString("first_name");
                    String lastName = adminDoc.getString("last_name");

                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Admin Sign-In Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Welcome, Administrator " + firstName + " " + lastName + "!");
                    alert.showAndWait();

                    // Redirect to Administrator Main Menu
                    openAdminMainMenu(event);
                } else {
                    showAlert("Sign-In Failed", "Invalid password.");
                    administratorPassword.clear();
                }
            } else {
                showAlert("Sign-In Failed", "Administrator not found.");
                administratorPassword.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while accessing the database.");
        }
    }

    // Method to open the Admin Main Menu page
    public void openAdminMainMenu(ActionEvent actionEvent) {
        try {
            // Load the Admin Main Menu page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
            Scene adminMainMenuScene = new Scene(loader.load());

            // Get the controller for the Admin Main Menu and pass the MongoClient and MongoDatabase
            administrator_main_menu adminMainMenuController = loader.getController();
            adminMainMenuController.setMongoClient(mongoClient);
            adminMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(adminMainMenuScene);
            currentStage.setTitle("Personalized News Recommendation System - Administrator Main Menu");
            currentStage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the Admin Main Menu page.");
            e.printStackTrace();
        }
    }

    // Method to show a simple alert with a message
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to navigate to the home page from the admin login
    @FXML
    public void adminLogInInHome(ActionEvent actionEvent) {
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
