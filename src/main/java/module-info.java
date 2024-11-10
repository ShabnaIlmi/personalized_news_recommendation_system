module com.example.personalized_news_recommendation_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.personalized_news_recommendation_system to javafx.fxml;
    exports com.example.personalized_news_recommendation_system;
}