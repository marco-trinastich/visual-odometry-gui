package com.mtm.vogui.utilities;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class StringUtils {
    public final static String WHITE_SPACE = " ";
    public final static String EMPTY_STRING = "";

    // Hex
    public final static String HEX_PREFIX = "0x";

    // Guid
    public final static int GUID_LENGTH = 36;
    public final static String GUID_SEPARATOR = "-";
    public final static List<Integer> GUID_SEPARATOR_POS = Arrays.asList(8, 13, 18, 23);
    public final static List<String> HEX_DIGITS = Arrays.asList(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");

    public static @NotNull String removeHexAddress(@NotNull String value) {
        String newValue = value;
        String newValueLower = value.toLowerCase();
        while (newValueLower.contains(HEX_PREFIX)) {
            int start = newValueLower.indexOf(HEX_PREFIX);
            int spacePos = newValueLower.substring(start).indexOf(WHITE_SPACE);
            int end = spacePos != -1 ? spacePos : newValueLower.length();

            newValue = newValue.replace(newValue.substring(start, end), EMPTY_STRING);
            newValueLower = newValue.toLowerCase();
        }

        return !newValue.trim().isEmpty() ? newValue.trim() : value;
    }

    public static @NotNull String removeGuid(String value) {
        String newValue = value;
        String[] splitValues = newValue.split(WHITE_SPACE);
        for (String splitValue : splitValues) {
            var isGuid = isGuid(splitValue);
            if (isGuid) {
                newValue = newValue.replace(splitValue, EMPTY_STRING);
            }
        }

        return newValue.trim();
    }

    public static boolean isGuid(@NotNull String guid) {
        // Guid structure:
        // [8 hex digits]-[4 hex digits]-[4 hex digits]-[4 hex digits]-[12 hex digits]
        // (32 hex digits/128 bit total)

        if (guid.length() != GUID_LENGTH) {
            // Invalid size
            return false;
        }

        char[] guidChars = guid.toLowerCase().toCharArray();
        for (int i = 0; i < guidChars.length; i++) {
            char guidChar = guidChars[i];
            if (!GUID_SEPARATOR_POS.contains(i)) {
                if (!HEX_DIGITS.contains(Character.toString(guidChar))) {
                    // Not an hex digit
                    return false;
                }
            } else {
                if (!GUID_SEPARATOR.equals(Character.toString(guidChar))) {
                    // Missing separator at group separation position
                    return false;
                }
            }
        }

        return true;
    }
}
