package com.example.personalized_news_recommendation_system.Controller.AdminController;

import com.example.personalized_news_recommendation_system.Driver.homePage;
import com.example.personalized_news_recommendation_system.Utils.Validator;
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
import java.time.LocalDateTime;

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
    private MongoCollection<Document> adminLogCollection;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for MongoDatabase
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.adminCollection = database.getCollection("Admin");
            this.adminLogCollection = database.getCollection("Admin_Logs");
        }
    }

    @FXML
    void administratorSignIn(ActionEvent event) {
        String username = administratorUsername.getText();
        String password = administratorPassword.getText();

        // Validate fields are not empty
        if (!Validator.areFieldsNotEmpty(username, password)) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        // Validate collections are set
        if (!Validator.areCollectionsSet(adminCollection, adminLogCollection)) {
            showAlert("Configuration Error", "Database collections are not properly configured.", Alert.AlertType.ERROR);
            return;
        }

        // Authenticate admin
        if (Validator.authenticateAdmin(adminCollection, username, password)) {
            // Retrieve admin's first and last name from the database
            Document adminDoc = adminCollection.find(new Document("username", username)).first();
            if (adminDoc == null) {
                showAlert("Database Error", "Admin record not found.", Alert.AlertType.ERROR);
                return;
            }

            String firstName = adminDoc.containsKey("first_name") ? adminDoc.getString("first_name") : "Administrator";
            String lastName = adminDoc.containsKey("last_name") ? adminDoc.getString("last_name") : "";

            logAdminLogin(username);  // Log the admin login event

            // Show success message with admin's name
            showAlert("Sign-In Success", "Welcome, " + firstName + " " + lastName + "!", Alert.AlertType.INFORMATION);

            openAdminMainMenu(event);  // Open admin main menu after successful login
        } else {
            showAlert("Sign-In Failed", "Invalid credentials.", Alert.AlertType.ERROR);
            administratorPassword.clear();
        }
    }
    
    private void logAdminLogin(String username) {
        Document log = new Document("admin_id", username)
                .append("logged_date_time", LocalDateTime.now().toString());
        adminLogCollection.insertOne(log);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void openAdminMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/administrator_main_menu.fxml"));
            Scene adminMainMenuScene = new Scene(loader.load());

            administrator_main_menu adminMainMenuController = loader.getController();
            adminMainMenuController.setMongoClient(mongoClient);
            adminMainMenuController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(adminMainMenuScene);
            currentStage.setTitle("Administrator Main Menu");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the Admin Main Menu page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void adminLogInHome(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/homePage.fxml"));
            Scene homeScene = new Scene(loader.load());

            homePage homeController = loader.getController();
            homeController.setMongoClient(mongoClient);
            homeController.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.setTitle("Home");
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load the home page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
