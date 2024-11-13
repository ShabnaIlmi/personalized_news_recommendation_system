module com.example.personalized_news_recommendation_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.slf4j;
    requires org.apache.opennlp.tools;


    opens com.example.personalized_news_recommendation_system to javafx.fxml;
    exports com.example.personalized_news_recommendation_system;
}