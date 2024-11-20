package com.example.personalized_news_recommendation_system;

import opennlp.tools.doccat.*;
import opennlp.tools.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class TextClassifier {
    public static void main(String[] args) {
        try {
            // Load the training data from a file
            File trainingDataFile = new File("src/main/resources/com/example/trainingData.txt");  // Adjust path if needed
            InputStream dataIn = new FileInputStream(trainingDataFile);
            //ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, StandardCharsets.UTF_8);
            //DocumentSampleStream sampleStream = new DocumentSampleStream(lineStream);

            // Train the model using the provided data
            //DocumentCategorizerModel model = DocumentCategorizerME.train(sampleStream);

            // Save the trained model to a file
            try (OutputStream modelOut = new FileOutputStream("doccat-model.bin")) {
            //    model.serialize(modelOut);
            }

            System.out.println("Model trained and saved successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

