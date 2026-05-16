package com.hindu.lordpromptsai.util;

public class UtilityHelper {

    public static int getTabOrder(String tabName) {
        if (tabName == null) return 99;
        return switch (tabName.toLowerCase()) {
            case "krishna" -> 0;
            case "shiva" -> 1;
            case "vishnu" -> 2;
            case "ganesha" -> 3;
            case "ram" -> 4;
            case "hanuman" -> 5;
            case "durga" -> 6;
            default -> 99;
        };
    }

    public static int getImageIndex(String imageName) {
        //e.g ganesha_3 → 3
        try {
            String[] parts = imageName.split("_");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
