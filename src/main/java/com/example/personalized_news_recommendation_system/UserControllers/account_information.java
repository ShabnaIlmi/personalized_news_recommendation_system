package com.example.personalized_news_recommendation_system.UserControllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

public class account_information {
    @FXML
    public Button mainMenu;
    @FXML
    public Button updateDetails;
    @FXML
    public Button exit;
    @FXML
    public TextField firstName;
    @FXML
    public TextField lastName;
    @FXML
    public TextField email;
    @FXML
    public TextField age;
    @FXML
    public ChoiceBox<String> category1;
    @FXML
    public ChoiceBox<String> category2;
    @FXML
    public ChoiceBox<String> category3;
    @FXML
    public PasswordField password;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String userId;
    private String sessionId;

    // Setters for MongoDB connection
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        System.out.println("MongoClient set successfully in Account Information controller.");
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
        System.out.println("Connected to database successfully.");
    }

    // Setter for User ID and Session ID
    public void setUserInfo(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    @FXML
    public void initialize() {
        // Populate categories in the ChoiceBoxes
        String[] categories = {"AI", "Technology", "Education", "Health", "Sports", "Fashion", "Entertainment", "Environment", "General"};
        category1.getItems().addAll(categories);
        category2.getItems().addAll(categories);
        category3.getItems().addAll(categories);
    }

    @FXML
    public void mainMenu(ActionEvent actionEvent) {
        try {
            // Load the administrator main menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/user_main_menu.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller of the new FXML
            user_main_menu controller = loader.getController();

            // Pass the MongoClient and Database to the new controller
            controller.setMongoClient(mongoClient);
            controller.setDatabase(mongoClient.getDatabase("News_Recommendation"));
            controller.setUserInfo(userId, sessionId);

            // Reuse the current stage (instead of opening a new one)
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Main Menu");
        } catch (IOException e) {
            showAlert("Error", "Failed to open Main Menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Method to generate a new username
    private String generateUsername(String firstName, String lastName) {
        String lowerFirstName = firstName.toLowerCase();
        String camelLastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
        String baseUsername = lowerFirstName + camelLastName;
        String uniqueNumber = generateUniqueNumber(baseUsername);
        return baseUsername + uniqueNumber + "@swiftly.com";
    }

    // Method to generate a unique number to ensure the username is not taken
    private String generateUniqueNumber(String baseUsername) {
        int count = 1;
        while (isUsernameTaken(baseUsername + String.format("%03d", count))) {
            count++;
        }
        return String.format("%03d", count);
    }

    // Check if the username is already taken
    private boolean isUsernameTaken(String username) {
        return database.getCollection("User").find(new Document("username", username)).first() != null;
    }

    @FXML
    public void updateDetails(ActionEvent actionEvent) {
        // Validate input fields
        if (firstName.getText().isEmpty() || lastName.getText().isEmpty() || email.getText().isEmpty() || age.getText().isEmpty() || password.getText().isEmpty()) {
            showAlert("Validation Error", "All fields must be filled before updating details.", Alert.AlertType.WARNING);
            return;
        }

        // Validate that three distinct categories are selected
        HashSet<String> selectedCategories = new HashSet<>(Arrays.asList(category1.getValue(), category2.getValue(), category3.getValue()));
        if (selectedCategories.contains(null) || selectedCategories.size() != 3) {
            showAlert("Validation Error", "Please select exactly three distinct categories.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Fetch current first name and last name from the database
            Document userDoc = database.getCollection("User").find(new Document("username", userId)).first();
            if (userDoc == null) {
                showAlert("Error", "User not found in the database.", Alert.AlertType.ERROR);
                return;
            }

            String previousFirstName = userDoc.getString("first_name");
            String previousLastName = userDoc.getString("last_name");

            // Check if the first name or last name has changed
            boolean nameChanged = !firstName.getText().equals(previousFirstName) || !lastName.getText().equals(previousLastName);
            String newUsername = userId; // Default to existing username

            if (nameChanged) {
                // Ask the user if they want to change their username
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Username Update");
                confirmation.setHeaderText("Change Detected");
                confirmation.setContentText("Your first name or last name has changed. Do you want to update your username?");

                if (confirmation.showAndWait().get() == ButtonType.OK) {
                    // Generate a new username
                    newUsername = generateUsername(firstName.getText(), lastName.getText());
                }
            }

            // Prepare updated data
            var updatedData = new Document("first_name", firstName.getText())
                    .append("last_name", lastName.getText())
                    .append("email", email.getText())
                    .append("age", Integer.parseInt(age.getText()))
                    .append("categories", Arrays.asList(category1.getValue(), category2.getValue(), category3.getValue()))
                    .append("password", password.getText())
                    .append("Updated_date_time", LocalDateTime.now().toString());

            // Add new username to the update if it has changed
            if (!newUsername.equals(userId)) {
                updatedData.append("username", newUsername);
            }

            // Update user details in the database
            database.getCollection("User").updateOne(new Document("username", userId), new Document("$set", updatedData));

            // Log the update action
            database.getCollection("User_Manage_Profile").insertOne(new Document("user_id", userId)
                    .append("session_id", sessionId)
                    .append("action", "Updated Information")
                    .append("updated_date_time", LocalDateTime.now().toString()));

            // Notify the user and log them out if the username changed
            if (!newUsername.equals(userId)) {
                userId = newUsername; // Update the in-memory userId
                logout(actionEvent, newUsername); // Redirect to login with a new username
                return; // Prevent further processing
            }

            // Show success message
            showAlert("Success", "User details updated successfully.", Alert.AlertType.INFORMATION);

            // Clear fields after update
            firstName.clear();
            lastName.clear();
            email.clear();
            age.clear();
            password.clear();
            category1.getSelectionModel().clearSelection();
            category2.getSelectionModel().clearSelection();
            category3.getSelectionModel().clearSelection();

        } catch (Exception e) {
            showAlert("Database Error", "Failed to update user details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void logout(ActionEvent actionEvent, String newUsername) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/user_log_in.fxml"));
            Scene scene = new Scene(loader.load());

            // Set MongoDB connection in the login controller
            user_log_in controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Reuse the current stage for the login scene
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Login Page");

            // Notify the user about username change
            Alert noticeAlert = new Alert(Alert.AlertType.INFORMATION);
            noticeAlert.setTitle("Notice");
            noticeAlert.setHeaderText("Username Updated");
            noticeAlert.setContentText(
                    "Your username has been updated successfully!\n" +
                            "New Username: " + newUsername + "\n" +
                            "Updated Password: " + password.getText() + "\n" +
                            "Please log in with your new credentials."
            );
            noticeAlert.showAndWait();


        } catch (IOException e) {
            showAlert("Error", "Failed to load login screen: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    public void exit(ActionEvent actionEvent) {
        Stage currentStage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();
    }

    public void back(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/manage_profile.fxml"));
            Scene scene = new Scene(loader.load());
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            manage_profile controller = loader.getController();
            controller.setMongoClient(mongoClient);
            controller.setDatabase(mongoClient.getDatabase("News_Recommendation"));
            controller.setUserInfo(userId, sessionId);

            currentStage.setScene(scene);
            currentStage.setTitle("Manage Profile");
        } catch (IOException e) {
            showAlert("Error", "Failed to load article view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
