package com.example.personalized_news_recommendation_system.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    // Email validation method
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Age validation method
    public static boolean isValidAge(String age) {
        try {
            int ageInt = Integer.parseInt(age);
            return ageInt > 0 && ageInt < 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Password validation method (at least 8 characters, uppercase, lowercase, and a number)
    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        return password.matches(passwordRegex);
    }

    // Category validation method (to ensure categories are distinct)
    public static boolean areCategoriesDistinct(String category1, String category2, String category3) {
        return !(category1.equals(category2) || category1.equals(category3) || category2.equals(category3));
    }
}
