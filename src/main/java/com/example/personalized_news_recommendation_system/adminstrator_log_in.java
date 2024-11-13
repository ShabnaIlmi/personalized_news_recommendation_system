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

public class adminstrator_log_in {

    @FXML
    private Button adminstratorSignIn;
    @FXML
    private Button adminstratorHome;
    @FXML
    private TextField adminstratorUsername;
    @FXML
    private PasswordField adminstratorPassword;

    private MongoClient mongoClient;
    private MongoCollection<Document> adminCollection;

    // Setter for MongoClient to initialize database connection
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in administrator_log_in controller.");
    }

    // Setter for MongoDatabase to initialize adminCollection
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.adminCollection = database.getCollection("Admin");
            System.out.println("Connected to Admin collection with " + adminCollection.countDocuments() + " documents.");
        } else {
            System.out.println("Database is null in administrator_log_in controller.");
        }
    }

    // Sign-In Method for Administrator
    @FXML
    void adminstratorSignIn(ActionEvent event) {
        String username = adminstratorUsername.getText();
        String password = adminstratorPassword.getText();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Sign-In Error", "Please fill in both the username and password fields.");
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

                    // Display success alert
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Admin Sign-In Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Welcome, Administrator " + firstName + " " + lastName + "!");
                    alert.showAndWait();

                    // Redirect to Administrator Main Menu
                    openAdminMainMenu(event);
                } else {
                    showAlert("Sign-In Failed", "Invalid password.");
                    adminstratorPassword.clear();
                }
            } else {
                showAlert("Sign-In Failed", "Administrator not found.");
                adminstratorPassword.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while accessing the database.");
        }
    }

    // Navigate to Administrator Main Page
    @FXML
    public void adminstratorHome(ActionEvent actionEvent) {
        openAdminMainMenu(actionEvent);
    }

    // Method to open the Administrator Main Menu after successful login
    private void openAdminMainMenu(ActionEvent actionEvent) {
        try {
            // Load the administrator main menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminstrator_main_menu.fxml"));
            Scene adminMainScene = new Scene(loader.load());

            // Get the current stage and set the new scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(adminMainScene);
            currentStage.setTitle("Administrator Main Menu");
            currentStage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the administrator main menu.");
            e.printStackTrace();
        }
    }

    // Helper method to show alert messages
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
