package com.telecom.model;

/**
 * Enumeration representing telephone call classification categories.
 */
public enum CallType {
    LOCAL("Local Call", "Within same telecom circle"),
    STD("National STD", "Inter-circle national long-distance call"),
    ISD("International ISD", "International overseas call");

    private final String displayName;
    private final String description;

    CallType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static CallType fromString(String text) {
        if (text == null) return LOCAL;
        try {
            return CallType.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LOCAL;
        }
    }
}
