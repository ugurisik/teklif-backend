package com.teklif.app.util;

public class Func {
    public static String normalize(String value) {
        if (value == null) return "";
        return value
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase()
                .replace("İ", "I")
                .replace("Ş", "S")
                .replace("Ğ", "G")
                .replace("Ü", "U")
                .replace("Ö", "O")
                .replace("Ç", "C");
    }

    public static String padToThree(String value) {
        if (value.length() >= 3) {
            return value.substring(0, 3);
        }
        return String.format("%-3s", value).replace(' ', 'X');
    }
}
