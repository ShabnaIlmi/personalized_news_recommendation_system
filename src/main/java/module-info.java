module com.example.personalized_news_recommendation_system {
    // Required JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // MongoDB-related dependencies
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;

    // Other dependencies
    requires org.slf4j;
    requires java.logging;
    requires java.sql;
    requires json;

    // Allow reflective access to specific packages (for JavaFX)
    opens com.example.personalized_news_recommendation_system.Driver to javafx.fxml;
    opens com.example.personalized_news_recommendation_system.Controller.AdminController to javafx.fxml;
    opens com.example.personalized_news_recommendation_system.Controller.UserController to javafx.fxml;
    opens com.example.personalized_news_recommendation_system.Model to javafx.fxml;
    opens com.example.personalized_news_recommendation_system.Database to javafx.fxml;

    // Export packages for external usage
    exports com.example.personalized_news_recommendation_system.Driver;
    exports com.example.personalized_news_recommendation_system.Controller.AdminController;
    exports com.example.personalized_news_recommendation_system.Controller.UserController;
    exports com.example.personalized_news_recommendation_system.Model;
    exports com.example.personalized_news_recommendation_system.Database;
}
