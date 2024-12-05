Personalized News Recommendation System

Overview
This project is a Personalized News Recommendation System that leverages MongoDB Atlas (cloud-based) for data storage, the Hugging Face API for Natural Language Processing (NLP) based categorization, and a custom Java algorithm to recommend articles to users. The system is designed to provide personalized news recommendations based on past user interactions. For new users, default articles are generated based on their preferred categories.

Features
MongoDB Atlas: Cloud-based database to store user data, article information, and user interactions.
Hugging Face API: Categorizes articles into predefined categories using NLP techniques.
Java Algorithm: Recommends articles to users based on their past interactions.
Default Articles for New Users: For new users, default articles are generated according to their preferred categories.
Prerequisites
Java 11 or higher
MongoDB Atlas account (for cloud-based MongoDB instance)
MongoDB Compass 8.0.3 (for managing your database locally)
Hugging Face API key (for NLP categorization)
MongoDB Java Driver (e.g., MongoDB Java Driver 4.2.x or higher)
Maven for project management (optional)

Setup Instructions
1. Clone the repository
   git clone https://github.com/your-username/personalized-news-recommendation-system.git <!-- Github repository Link -->

2. Install Dependencies
Add the required dependencies in your pom.xml if using Maven, or manually install the libraries in your project for MongoDB and Hugging Face integration.
<dependencies>
    <!-- MongoDB Driver -->
    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongodb-driver-sync</artifactId>
        <version>4.2.x</version>
    </dependency>

    <!-- Hugging Face Integration (Optional: You may use an HTTP client like Apache HttpClient) -->
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
        <version>4.5.13</version>
    </dependency>
</dependencies>

3. MongoDB Atlas Setup
Create a MongoDB Atlas account: If you don't have one, go to MongoDB Atlas and sign up.

Create a Cluster: After signing in, create a free-tier MongoDB cluster.

Get your connection string: Once your cluster is created, go to the Connect button in Atlas and follow the instructions to get the connection string for connecting your Java application to the cloud-based MongoDB instance.

<!-- Connection string-->
String uri = "mongodb+srv://<Shabna_2409661>:<VictoriousEstablishment>@cluster0.mongodb.net/test?retryWrites=true&w=majority";
MongoClient mongoClient = MongoClients.create(uri);
MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
MongoCollection<Document> articlesCollection = database.getCollection("Articles");
MongoCollection<Document> usersCollection = database.getCollection("User");
MongoCollection<Document> usersCollection = database.getCollection("User_Preferences");

Connect using MongoDB Compass:
Download MongoDB Compass 8.0.3 from MongoDB Compass Download.
Open MongoDB Compass and enter the connection string (from MongoDB Atlas) in the "New Connection" field to connect and view/manage your cloud database locally.

<!-- Setting up the Hugging Face API (Cloud based NLP)-->
4. Set up Hugging Face API
Sign up for a Hugging Face account at Hugging Face.
Obtain API key: After signing in, navigate to Hugging Face API Keys and generate an API key.
Integrate Hugging Face API: Use the key to interact with the Hugging Face NLP models for article categorization.

Example of HTTP request to Hugging Face API (using Apache HttpClient):
HttpPost request = new HttpPost("https://api-inference.huggingface.co/models/your-model");
request.setHeader("Authorization", "Bearer YOUR_HUGGING_FACE_API_KEY");

<!-- Requirements before running the application-->
5. Running the Application
The system can be run as a standard Java application. Make sure that:

Your MongoDB Atlas database is properly set up and connected.
Hugging Face API is integrated correctly.
MongoDB Compass is connected to Atlas for local management.

<!-- To run the application-->
java -jar personalized-news-recommendation-system.jar

Usage

1. User Registration
New users register by providing their preferred news categories (e.g., Technology, Health, Sports).
The system will generate default articles based on their preferences.

3. Article Categorization
Articles are sent to the Hugging Face API to classify them into predefined categories like Technology, Health, etc.
This categorization helps the recommendation system filter articles according to user preferences.

5. User Interactions
The system stores user interactions (clicks, likes, etc.) with articles in MongoDB.
Based on these interactions, a Java algorithm uses a simple recommendation logic to suggest relevant articles to the user.
Example recommendation process:

Collaborative Filtering: Based on articles liked or disliked by similar users.
Content-based Filtering: Suggests articles with similar content to those the user has interacted with.

4. Recommendations
For returning users, the system suggests articles based on their past interactions.
For new users, the system recommends default articles based on the selected preferred category.

Admin Operations (Add, Update, Delete Articles)

The Admin has the following functionalities:
1. Manually Add Articles: Admin can input articles directly, including title, content, and category.
2. Update Existing Articles: Admin can update articles in the database based on article ID.
3. Delete Articles: Admin can delete articles from the database using article ID.
4. Bulk Add via JSON: Admin can upload a JSON file containing multiple articles to be added to the database

File Structure

The project follows a standard Java Maven structure. Below is an overview:
├── src/
│   ├── com/
│   │   ├── example/
│   │   │   ├── personalized_news_recommendation_system/
│   │   │   │   ├── Controller/
│   │   │   │   │   ├── AdminController/
│   │   │   │   │   └── UserController/
│   │   │   │   ├── Model/
│   │   │   │   │   ├── Admin.java
│   │   │   │   │   ├── Article.java
│   │   │   │   │   ├── Person.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── Service/
│   │   │   │   │   └── DatabaseService.java
│   │   │   │   └── Utils/
│   │   │   │       ├── ShowAlerts.java
│   │   │   │       ├── ShowErrors.java
│   │   │   │       └── Validator.java
│   └── resources/
│       └── config/
│           └── application.properties
├── pom.xml
├── README.md
└── LICENSE

Description of Key Folders & Files

src/com/example/personalized_news_recommendation_system/Controller/
AdminController.java: Handles the logic for managing articles (add, update, delete) and admin login.
UserController.java: Manages user-related actions such as sign-up, login, article viewing, and recommendations.

src/com/example/personalized_news_recommendation_system/Model/
Admin.java: Represents the admin entity.
Article.java: Represents an article with properties like title, content, category, and date.
Person.java: A base class for common user/admin properties.
User.java: Represents the user entity and their preferences.

src/com/example/personalized_news_recommendation_system/Service/
DatabaseService.java: Handles the connection and interaction with the MongoDB Atlas database.

src/com/example/personalized_news_recommendation_system/Utils/
ShowAlerts.java: A utility for displaying alert messages to the user.
ShowErrors.java: A utility for showing error messages.
Validator.java: A utility for input validation.

src/resources/config/
application.properties: Contains configuration for the MongoDB connection and other system properties.

