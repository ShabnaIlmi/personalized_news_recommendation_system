package com.example.personalized_news_recommendation_system.Admin;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class manage_articles {

    @FXML
    private Button updateButton, deleteButton, manageMainMenu, manageExit;
    @FXML
    private TableView<Document> articleTable;
    @FXML
    private TableColumn<Document, String> articleIDColumn, articleNameColumn, categoryColumn, authorColumn, dateColumn;
    @FXML
    private TextField articleNameField, categoryField, authorField;
    @FXML
    private TextArea manageDescription, manageContent;
    @FXML
    private DatePicker publishedDatePicker;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articlesCollection;
    private MongoCollection<Document> updatedArticlesCollection;
    private MongoCollection<Document> deletedArticlesCollection;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    // Set the MongoClient
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        if (mongoClient != null) {
            System.out.println("MongoClient successfully set in Manage Articles.");
        } else {
            System.err.println("MongoClient is null in Manage Articles.");
        }
    }

    // Set the MongoDatabase and initialize collections
    public void setDatabase(MongoDatabase database) {
        if (database != null) {
            this.articlesCollection = database.getCollection("Articles");
            this.updatedArticlesCollection = database.getCollection("Updated_Articles");
            this.deletedArticlesCollection = database.getCollection("Deleted_Articles");
            System.out.println("Collections initialized successfully.");
            populateArticleTable(); // Populate table after collections are initialized
        } else {
            System.err.println("Database is null. Cannot initialize collections.");
        }
    }

    // Populate the article table
    private void populateArticleTable() {
        try {
            if (articlesCollection == null) {
                throw new IllegalStateException("Articles collection not initialized.");
            }

            List<Document> articles = articlesCollection.find().into(new ArrayList<>());
            System.out.println("Articles Retrieved: " + articles.size());

            if (articles.isEmpty()) {
                articleTable.setPlaceholder(new Label("No articles available."));
            }

            articleIDColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("article_id"))
            );
            articleNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("article_name"))
            );
            categoryColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("category"))
            );
            authorColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("author"))
            );
            dateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getString("published_date"))
            );

            articleTable.getItems().setAll(articles);

        } catch (Exception e) {
            showAlert("Error", "Failed to populate article table: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void updateArticle(ActionEvent actionEvent) {
        try {
            Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle == null) {
                showAlert("Error", "Please select an article to update.", Alert.AlertType.ERROR);
                return;
            }

            if (!validateInputs()) {
                showAlert("Error", "Please fill in all required fields.", Alert.AlertType.ERROR);
                return;
            }

            // Create a background task for the API call and update process
            Task<Void> updateTask = new Task<Void>() {
                // Declare predictedCategory as a field to make it accessible in both call and succeeded
                private String predictedCategory = "";

                @Override
                protected Void call() throws Exception {
                    try {
                        // Get the content of the article to predict the category
                        String content = manageContent.getText();

                        // Predict the category using Hugging Face BART model and store the result in predictedCategory
                        predictedCategory = predictCategory(content, new String[]{"AI", "Technology", "Education", "Health", "Sports", "Fashion", "Entertainment", "Environment", "General"});

                    } catch (Exception e) {
                        // Handle error in prediction and display the category that was predicted before failure
                        Platform.runLater(() -> showAlert("Prediction Error", "Failed to predict category. Predicted category: " + predictedCategory, Alert.AlertType.ERROR));
                        throw e;  // Rethrow exception after showing the error
                    }

                    // Get the current time for the update
                    String currentTime = LocalDateTime.now().toString();

                    // Prepare the updated article document with the predicted category
                    Document updatedArticle = new Document("article_name", articleNameField.getText())
                            .append("category", predictedCategory)  // Use the predicted category
                            .append("author", authorField.getText())
                            .append("description", manageDescription.getText())
                            .append("content", manageContent.getText())
                            .append("published_date", publishedDatePicker.getValue().toString())
                            .append("article_added_time", currentTime); // Add or update the time

                    // Perform the update in the database
                    articlesCollection.updateOne(
                            new Document("article_id", selectedArticle.getString("article_id")),
                            new Document("$set", updatedArticle)
                    );

                    // Insert updated record into Updated_Articles collection
                    updatedArticlesCollection.insertOne(updatedArticle);

                    return null;
                }

                @Override
                protected void succeeded() {
                    // Show success message and clear fields
                    Platform.runLater(() -> {
                        showAlert("Success", "Article updated successfully under category: " + predictedCategory, Alert.AlertType.INFORMATION);

                        // Clear the fields after the successful update
                        articleNameField.clear();
                        authorField.clear();
                        manageDescription.clear();
                        manageContent.clear();
                        publishedDatePicker.setValue(null);  // Clear the date picker
                    });

                    populateArticleTable(); // Refresh the table after update
                }

                @Override
                protected void failed() {
                    showAlert("Error", "Failed to update article: " + getException().getMessage(), Alert.AlertType.ERROR);
                    getException().printStackTrace();
                }
            };

            // Run the update task asynchronously
            executorService.submit(updateTask);

        } catch (Exception e) {
            showAlert("Error", "Failed to update article: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }



    // Delete selected article with concurrency
    @FXML
    public void deleteArticle(ActionEvent actionEvent) {
        try {
            Document selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if (selectedArticle == null) {
                showAlert("Error", "Please select an article to delete.", Alert.AlertType.ERROR);
                return;
            }

            // Create a background task for the delete process
            Task<Void> deleteTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Perform delete operation
                    articlesCollection.deleteOne(new Document("article_id", selectedArticle.getString("article_id")));

                    // Add deleted article to Deleted_Articles collection
                    deletedArticlesCollection.insertOne(selectedArticle);

                    return null;
                }

                @Override
                protected void succeeded() {
                    showAlert("Success", "Article deleted successfully!", Alert.AlertType.INFORMATION);
                    populateArticleTable(); // Refresh table
                }

                @Override
                protected void failed() {
                    showAlert("Error", "Failed to delete article: " + getException().getMessage(), Alert.AlertType.ERROR);
                    getException().printStackTrace();
                }
            };

            // Run the delete task asynchronously
            executorService.submit(deleteTask);

        } catch (Exception e) {
            showAlert("Error", "Failed to delete article: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Handle the "Main Menu" button action
    @FXML
    public void manageMainMenu(ActionEvent actionEvent) {
        try {
            // Close the current stage
            Stage currentStage = (Stage) articleNameField.getScene().getWindow();
            currentStage.close();

            // Load the administrator main menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/personalized_news_recommendation_system/administrator_main_menu.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller of the new FXML
            administrator_main_menu controller = loader.getController();

            // Pass the mongoClient, database, and any other required information to the new controller
            controller.setMongoClient(mongoClient);
            controller.setDatabase(mongoClient.getDatabase("News_Recommendation"));

            // Set up the new stage and show the main menu
            Stage mainMenuStage = new Stage();
            mainMenuStage.setScene(scene);
            mainMenuStage.setTitle("Main Menu");
            mainMenuStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open Main Menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    // Handle the "Exit" button action
    @FXML
    public void manageExit(ActionEvent actionEvent) {
        shutdownExecutor();
        Stage stage = (Stage) articleNameField.getScene().getWindow();
        stage.close();
    }

    // Validate inputs for update operation
    private boolean validateInputs() {
        return !(articleNameField.getText().isEmpty() ||
                authorField.getText().isEmpty() ||
                manageDescription.getText().isEmpty() ||
                manageContent.getText().isEmpty() ||
                publishedDatePicker.getValue() == null);
    }

    // Show an alert dialog
    private void showAlert(String title, String content, Alert.AlertType error) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // BART-based categorization (Hugging Face API integration)
    private String predictCategory(String content, String[] labels) throws Exception {
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
        String token = "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";

        String payload = constructPayload(content, labels);

        // Send request and parse response
        return sendCategoryPredictionRequest(apiUrl, token, payload);
    }

    private String constructPayload(String content, String[] labels) {
        return String.format("{\"inputs\": \"%s\", \"parameters\": {\"candidate_labels\": [\"%s\"]}}",
                content.replace("\"", "\\\""),
                String.join("\", \"", labels));
    }

    private String sendCategoryPredictionRequest(String apiUrl, String token, String payload) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Write payload
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return parseCategoryFromResponse(response.toString());
    }

    private String parseCategoryFromResponse(String response) throws Exception {
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray labelsArray = jsonResponse.optJSONArray("labels");

        if (labelsArray == null || labelsArray.length() == 0) {
            throw new Exception("Response does not contain valid 'labels' field.");
        }

        // Return the top label
        return labelsArray.getString(0);
    }

    public void shutdownExecutor() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
