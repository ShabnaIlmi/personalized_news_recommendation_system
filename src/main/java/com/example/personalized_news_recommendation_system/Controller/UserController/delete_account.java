package com.example.personalized_news_recommendation_system.Controller.UserController;

import com.example.personalized_news_recommendation_system.Utils.ShowAlerts;
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
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDateTime;

public class delete_account {

    @FXML
    private Button deleteMainMenu;
    @FXML
    private Button delete;
    @FXML
    private Button deleteExit;
    @FXML
    private Button back;
    @FXML
    private Label userName;
    @FXML
    private Label fullName;
    @FXML
    private Label email;
    @FXML
    private Label age;
    @FXML
    private Label categories;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String userId;
    private String sessionId;

    // Setter for MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Setter for Database
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // Setter for User ID and Session ID
    public void setUserInfo(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
        displayUserDetails();
    }

    // Fetch user details from the database and display them
    private void displayUserDetails() {
        try {
            if (userId == null || userId.isEmpty()) {
                showErrorDetails("Invalid user ID. Cannot fetch details.");
                return;
            }

            MongoCollection<Document> usersCollection = database.getCollection("User");
            Document userDoc = usersCollection.find(new Document("username", userId)).first();

            if (userDoc != null) {
                userName.setText(userDoc.getString("username"));
                fullName.setText(userDoc.getString("first_name") + " " + userDoc.getString("last_name"));
                email.setText(userDoc.getString("email"));
                age.setText(String.valueOf(userDoc.getInteger("age")));
                categories.setText(String.join(", ", userDoc.getList("categories", String.class)));
            } else {
                showErrorDetails("No user information found.");
            }
        } catch (Exception e) {
            ShowAlerts.showAlert("Database Error", "Failed to fetch user details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showErrorDetails(String message) {
        userName.setText(message);
        fullName.setText("");
        email.setText("");
        age.setText("");
        categories.setText("");
    }

    @FXML
    public void deleteMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/user_main_menu.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the correct controller for user_main_menu
            user_main_menu controller = loader.getController(); // Corrected to match the actual controller class
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserInfo(userId, sessionId);

            // Reuse the current stage
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Main Menu");
        } catch (IOException e) {
            ShowAlerts.showAlert("Error", "Failed to load Main Menu view: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    public void back(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/manage_profile.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller and pass data
            manage_profile controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(database);
            controller.setUserInfo(userId, sessionId);

            // Reuse the current stage
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Manage Profile");
        } catch (IOException e) {
            ShowAlerts.showAlert("Error", "Failed to load Manage Profile view: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void delete(ActionEvent actionEvent) {
        try {
            MongoCollection<Document> usersCollection = database.getCollection("User");

            // Perform the delete operation based on user ID
            Document deletedUser = usersCollection.findOneAndDelete(new Document("username", userId));

            if (deletedUser != null) {
                MongoCollection<Document> userUpdatedAccountCollection = database.getCollection("User_Manage_Profile");
                Document logEntry = new Document("user_id", userId)
                        .append("session_id", sessionId)
                        .append("action", "Deleted Account")
                        .append("deleted_date_time", LocalDateTime.now().toString());
                userUpdatedAccountCollection.insertOne(logEntry);

                ShowAlerts.showAlert("Success", "Account deleted successfully.", Alert.AlertType.INFORMATION);
                showErrorDetails("User account has been deleted.");

                // Call the exit method after successful deletion
                deleteExit(actionEvent);

            } else {
                ShowAlerts.showAlert("Error", "User account not found for deletion.", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            ShowAlerts.showAlert("Database Error", "Failed to delete user account: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    public void deleteExit(ActionEvent actionEvent) {
        Stage currentStage = (Stage) deleteExit.getScene().getWindow();
        currentStage.close();
    }

}

