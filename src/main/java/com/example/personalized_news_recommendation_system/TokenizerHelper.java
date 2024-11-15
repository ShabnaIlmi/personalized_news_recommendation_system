package com.example.personalized_news_recommendation_system;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenizerHelper {
    private static final Logger logger = Logger.getLogger(TokenizerHelper.class.getName());
    private TokenizerME tokenizer;

    public TokenizerHelper(String modelPath) throws IOException {
        if (modelPath == null || modelPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Tokenizer model path cannot be null or empty.");
        }

        try (InputStream modelIn = new FileInputStream(modelPath)) {
            TokenizerModel tokenModel = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(tokenModel);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load tokenizer model from path: " + "C:\\Users\\HP\\personalized_news_recommendation_system\\src\\main\\resources\\com\\example\\personalized_news_recommendation_system\\en-token.bin", e);
            throw e;
        }
    }

    public String[] tokenizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text to tokenize cannot be null or empty.");
        }
        return tokenizer.tokenize(text);
    }
}
