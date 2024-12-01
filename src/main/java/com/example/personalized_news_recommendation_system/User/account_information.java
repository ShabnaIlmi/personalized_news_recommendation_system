package com.example.personalized_news_recommendation_system.User;

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
    private String generateUniqueNumber(String username) {
        int count = (int) database.getCollection("User").countDocuments(new Document("username", new Document("$regex", "^" + username)));
        return String.format("%03d", count + 1);
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
            String previousFirstName = userDoc.getString("first_name");
            String previousLastName = userDoc.getString("last_name");

            // Determine if first name or last name has changed
            String newUsername = userId; // Default to existing username

            if (!firstName.getText().equals(previousFirstName) || !lastName.getText().equals(previousLastName)) {
                // Generate a new username if first or last name has changed
                newUsername = generateUsername(firstName.getText(), lastName.getText());
            }

            // If the username has changed, logout the user
            if (!newUsername.equals(userId)) {
                // Log the user out
                logout(actionEvent);  // This will close the current window and redirect to the login screen
                return;  // Exit the update process as the username has changed
            }

            // Update user details in the Users collection with new username and password
            var usersCollection = database.getCollection("User");
            var updatedData = new Document("first_name", firstName.getText())
                    .append("last_name", lastName.getText())
                    .append("email", email.getText())
                    .append("age", Integer.parseInt(age.getText()))
                    .append("categories", Arrays.asList(category1.getValue(), category2.getValue(), category3.getValue()))  // Changed to List<String>
                    .append("password", password.getText())
                    .append("Updated_date_time", LocalDateTime.now().toString());

            // If the username has changed, add it to the update
            if (!newUsername.equals(userId)) {
                updatedData.append("username", newUsername);
            }

            // Update user information using user_id
            usersCollection.updateOne(new Document("username", userId), new Document("$set", updatedData));

            // Log the update action in the User_Updated_Details collection
            var updatedDetailsCollection = database.getCollection("User_Manage_Profile");
            var updateLog = new Document("user_id", userId)
                    .append("session_id", sessionId)
                    .append("action", "Updated Information")
                    .append("updated_date_time", LocalDateTime.now().toString());

            updatedDetailsCollection.insertOne(updateLog);

            // Show a success message with the new username and password
            showAlert("Success", "User details updated successfully.\nUsername: " + newUsername + "\nNew Password: " + password.getText(), Alert.AlertType.INFORMATION);

            // Clear the fields after successful update
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

    // Method to logout the user by closing the current window and redirecting to login screen
    private void logout(ActionEvent actionEvent) {
        // Close the current session window
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        currentStage.close();  // Close the current stage/window

        // Optionally, open the login screen or redirect the user to the login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Login");

            // Close the login screen when the user closes the window
            stage.setOnCloseRequest(event -> System.exit(0));

            stage.show();  // Show the login window
        } catch (IOException e) {
            showAlert("Error", "Failed to load login screen: " + e.getMessage(), Alert.AlertType.ERROR);
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
