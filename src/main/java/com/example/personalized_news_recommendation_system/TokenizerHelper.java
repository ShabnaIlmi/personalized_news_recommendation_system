package com.example.personalized_news_recommendation_system;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TokenizerHelper {
    private TokenizerME tokenizer;

    public TokenizerHelper(String modelPath) throws IOException {
        try (InputStream modelIn = new FileInputStream(modelPath)) {
            TokenizerModel tokenModel = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(tokenModel);
        }
    }

    public String[] tokenizeText(String text) {
        return tokenizer.tokenize(text);
    }
}

