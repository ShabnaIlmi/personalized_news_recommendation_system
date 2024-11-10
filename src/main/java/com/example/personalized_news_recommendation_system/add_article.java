package com.example.personalized_news_recommendation_system;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class add_article {

    @FXML
    private TextField articleNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField authorField;
    @FXML
    private DatePicker publishedDatePicker;
    @FXML
    private TextArea contentArea;
    @FXML
    private Button submitArticleButton;

    private final article_service articleService;

    public add_article(article_service articleService) {
        this.articleService = articleService;
    }

    @FXML
    private void initialize() {
        // Initialization logic if needed
    }

    @FXML
    private void submitArticle() {
        String articleName = articleNameField.getText();
        String category = categoryField.getText();
        String author = authorField.getText();
        LocalDate publishedDate = publishedDatePicker.getValue();
        String content = contentArea.getText();

        if (articleName.isEmpty() || category.isEmpty() || author.isEmpty() || publishedDate == null || content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please complete all fields");
            return;
        }

        article newArticle = new article(articleName, category, author, publishedDate.toString(), content);
        articleService.addArticle(newArticle);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Article added successfully!");
        clearForm();
    }

    @FXML
    private void addMainMenu() {
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Returning to the Main Menu.");
    }

    @FXML
    private void exitArticle() {
        showAlert(Alert.AlertType.INFORMATION, "Exit", "Exiting the application.");
        System.exit(0);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        articleNameField.clear();
        categoryField.clear();
        authorField.clear();
        publishedDatePicker.setValue(null);
        contentArea.clear();
    }
}
