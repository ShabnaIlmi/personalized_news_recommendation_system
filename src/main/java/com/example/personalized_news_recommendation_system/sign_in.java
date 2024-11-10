package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.scene.control.Button;
import org.bson.Document;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class sign_in {
    @FXML
    public Button signIn;
    @FXML
    public Button userSignHome;
    @FXML
    private TextField userSignUsername;
    @FXML
    private PasswordField userSignPassword;

    private MongoCollection<Document> userCollection;

    // Setter for MongoDatabase to initialize userCollection
    public void setDatabase(MongoDatabase database) {
        this.userCollection = database.getCollection("User");
    }

    @FXML
    void signIn(ActionEvent event) {
        String username = userSignUsername.getText();
        String password = userSignPassword.getText();

        // Query MongoDB for the user based on username and password
        Document query = new Document("username", username).append("password", password);
        Document userDoc = userCollection.find(query).first();

        if (userDoc != null) {
            // User found, show a welcome alert with the user's first and last name
            String firstName = userDoc.getString("first_name");
            String lastName = userDoc.getString("last_name");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sign-In Success");
            alert.setHeaderText(null);
            alert.setContentText("Welcome, " + firstName + " " + lastName + "!");
            alert.showAndWait();
        } else {
            // User not found, show an error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Sign-In Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password.");
            alert.showAndWait();
        }
    }

    @FXML
    void userSignHome(ActionEvent event) {
        // Logic to navigate back to the home page
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Home");
        alert.setHeaderText(null);
        alert.setContentText("Navigating to Home...");
        alert.showAndWait();
    }
}
