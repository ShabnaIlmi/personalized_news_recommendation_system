package com.example.personalized_news_recommendation_system;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenNLPExample {

    private static final Logger logger = Logger.getLogger(OpenNLPExample.class.getName());
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private NameFinderME personNameFinder;
    private NameFinderME organizationNameFinder;
    private NameFinderME locationNameFinder;

    public OpenNLPExample() throws Exception {
        String tokenModelPath = System.getenv("TOKEN_MODEL_PATH");
        String posModelPath = System.getenv("POS_MODEL_PATH");
        String nerModelBasePath = System.getenv("NER_LOCATION_MODEL_PATH");

        if (tokenModelPath == null || posModelPath == null || nerModelBasePath == null) {
            throw new Exception("Environment variables 'TOKEN_MODEL_PATH', 'POS_MODEL_PATH', and 'NER_LOCATION_MODEL_PATH' must be set.");
        }

        initializeModels(tokenModelPath, posModelPath, nerModelBasePath);
    }

    private void initializeModels(String tokenModelPath, String posModelPath, String nerModelBasePath) throws Exception {
        try {
            tokenizer = new TokenizerME(loadTokenizerModel("C:\\Users\\HP\\personalized_news_recommendation_system\\src\\main\\resources\\com\\example\\personalized_news_recommendation_system\\en-token.bin"));
            posTagger = new POSTaggerME(loadPOSModel("C:\\Users\\HP\\personalized_news_recommendation_system\\src\\main\\resources\\com\\example\\personalized_news_recommendation_system\\en-pos-maxent.bin"));
            personNameFinder = new NameFinderME(loadNERModel("C:\\Users\\HP\\personalized_news_recommendation_system\\src\\main\\resources\\com\\example\\personalized_news_recommendation_system\\en-ner-person.bin"));


            logger.info("All OpenNLP models loaded successfully.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading OpenNLP models.", e);
            throw new Exception("Failed to load OpenNLP models. Ensure the paths are correct and the files exist.", e);
        }
    }

    private TokenizerModel loadTokenizerModel(String filePath) throws IOException {
        try (InputStream modelIn = new FileInputStream(filePath)) {
            return new TokenizerModel(modelIn);
        }
    }

    private POSModel loadPOSModel(String filePath) throws IOException {
        try (InputStream modelIn = new FileInputStream(filePath)) {
            return new POSModel(modelIn);
        }
    }

    private TokenNameFinderModel loadNERModel(String filePath) throws IOException {
        try (InputStream modelIn = new FileInputStream(filePath)) {
            return new TokenNameFinderModel(modelIn);
        }
    }

    public TokenizerME getTokenizer() {
        return tokenizer;
    }

    public POSTaggerME getPosTagger() {
        return posTagger;
    }

    public NameFinderME getPersonNameFinder() {
        return personNameFinder;
    }

    public NameFinderME getOrganizationNameFinder() {
        return organizationNameFinder;
    }

    public NameFinderME getLocationNameFinder() {
        return locationNameFinder;
    }
}
