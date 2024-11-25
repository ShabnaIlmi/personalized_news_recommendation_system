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

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        if (authenticateAdmin(username, password)) {
            logAdminLogin(username); // Log the admin login event
            showAlert("Sign-In Success", "Welcome, Administrator!", Alert.AlertType.INFORMATION);
            openAdminMainMenu(event);
        } else {
            showAlert("Sign-In Failed", "Invalid credentials.", Alert.AlertType.ERROR);
            administratorPassword.clear();
        }
    }

    private Boolean authenticateAdmin(String username, String password) {
        Document adminDoc = adminCollection.find(new Document("username", username)).first();
        if (adminDoc != null) {
            String storedPassword = adminDoc.getString("password");
            return storedPassword != null && storedPassword.equals(password);
        }
        return false;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator_main_menu.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homePage.fxml"));
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
