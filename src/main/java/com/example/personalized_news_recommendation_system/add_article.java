package com.example.personalized_news_recommendation_system;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.time.LocalDate;

public class add_article {

    @FXML
    private Button addMainMenu;
    @FXML
    private Button exitArticle;
    @FXML
    private TextField articleNameField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;
    @FXML
    private TextArea contentArea;
    @FXML
    private Button submitArticle;
    @FXML
    private ProgressIndicator progressIndicator;

    private MongoDatabase database;
    private OpenNLPExample openNLPExample;

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    // Initialize OpenNLP
    public void initOpenNLP() {
        try {
            openNLPExample = new OpenNLPExample();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to initialize OpenNLP. Entity recognition will be disabled.");
        }
    }

    @FXML
    public void initialize() {
        // If the database is not yet set, show an error
        if (database == null) {
            showAlert("Database Error", "Database connection failed. Please ensure MongoDB is running.");
            return;
        }
        initOpenNLP();
        progressIndicator.setVisible(false); // Hide the progress indicator initially
    }

    // Submit article and save to MongoDB
    @FXML
    public void submitArticle(ActionEvent event) {
        // Show the progress indicator while processing
        progressIndicator.setVisible(true);

        if (database == null) {
            showAlert("Database Error", "Database is not connected.");
            progressIndicator.setVisible(false);
            return;
        }

        String articleName = articleNameField.getText().trim();
        String author = authorField.getText().trim();
        String content = contentArea.getText().trim();
        LocalDate publishedDate = publishedDatePicker.getValue();

        // Validate inputs
        if (articleName.isEmpty()) {
            showAlert("Input Error", "Article name cannot be empty.");
            progressIndicator.setVisible(false);
            return;
        }
        if (author.isEmpty()) {
            showAlert("Input Error", "Author name cannot be empty.");
            progressIndicator.setVisible(false);
            return;
        }
        if (content.isEmpty()) {
            showAlert("Input Error", "Content cannot be empty.");
            progressIndicator.setVisible(false);
            return;
        }
        if (publishedDate == null) {
            showAlert("Input Error", "Please select a published date.");
            progressIndicator.setVisible(false);
            return;
        }

        // Categorize article
        String category = categorizeArticle(content);

        // Prepare the document to insert into MongoDB
        Document article = new Document("article_name", articleName)
                .append("author", author)
                .append("published_date", publishedDate.toString())
                .append("content", content)
                .append("category", category);

        try {
            // Get or create Articles collection
            MongoCollection<Document> articlesCollection = database.getCollection("Articles");
            if (articlesCollection == null) {
                database.createCollection("Articles");
                articlesCollection = database.getCollection("Articles");
            }

            // Insert the article into the collection
            articlesCollection.insertOne(article);

            // Show success message
            showAlert("Success", "Article submitted successfully!");
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save the article to the database. Please check the connection and retry.");
        } finally {
            progressIndicator.setVisible(false); // Hide the progress indicator after the process is done
        }
    }

    // Categorize the article based on content
    private String categorizeArticle(String content) {
        if (openNLPExample == null) {
            return "Uncategorized";
        }

        try {
            String[] tokens = openNLPExample.getTokenizer().tokenize(content);
            Span[] personSpans = openNLPExample.getPersonNameFinder().find(tokens);
            Span[] locationSpans = openNLPExample.getLocationNameFinder().find(tokens);
            Span[] organizationSpans = openNLPExample.getOrganizationNameFinder().find(tokens);

            StringBuilder categories = new StringBuilder();

            if (personSpans.length > 0) {
                categories.append("Persons: ");
                for (Span span : personSpans) {
                    categories.append(joinTokens(tokens, span)).append(", ");
                }
            }
            if (locationSpans.length > 0) {
                categories.append("Locations: ");
                for (Span span : locationSpans) {
                    categories.append(joinTokens(tokens, span)).append(", ");
                }
            }
            if (organizationSpans.length > 0) {
                categories.append("Organizations: ");
                for (Span span : organizationSpans) {
                    categories.append(joinTokens(tokens, span)).append(", ");
                }
            }

            return categories.length() > 0 ? categories.toString().replaceAll(", $", "") : "General";

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error during categorization. Using 'Uncategorized'.");
            return "Uncategorized";
        }
    }

    // Helper method to join tokens from OpenNLP spans
    private String joinTokens(String[] tokens, Span span) {
        StringBuilder sb = new StringBuilder();
        for (int i = span.getStart(); i < span.getEnd(); i++) {
            sb.append(tokens[i]).append(" ");
        }
        return sb.toString().trim();
    }

    // Show alert dialog
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Clear input fields
    private void clearFields() {
        articleNameField.clear();
        authorField.clear();
        contentArea.clear();
        publishedDatePicker.setValue(null);
    }

    // Navigate to main menu
    @FXML
    public void addMainMenu(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminstrator_main_menu.fxml"));
            Stage stage = (Stage) addMainMenu.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the main menu.");
        }
    }

    // Exit the article form
    @FXML
    public void exitArticle(ActionEvent actionEvent) {
        Stage stage = (Stage) exitArticle.getScene().getWindow();
        stage.close();
    }
}
