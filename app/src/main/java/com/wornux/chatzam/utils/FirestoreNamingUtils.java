package com.wornux.chatzam.utils;

public class FirestoreNamingUtils {
    
    private FirestoreNamingUtils() {
        //not required
    }
    
    public static String toCollectionName(String className) {
        String snakeCase = camelCaseToSnakeCase(className);
        return pluralize(snakeCase);
    }
    
    private static String camelCaseToSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    private static String pluralize(String word) {
        if (word.endsWith("s") || word.endsWith("x") || 
            word.endsWith("ch") || word.endsWith("sh")) {
            return word + "es";
        } else if (word.matches(".*[^aeiou]y$")) {
            return word.substring(0, word.length() - 1) + "ies";
        } else {
            return word + "s";
        }
    }
}
