package com.practice.notes.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Sorry, unable to find config.properties");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getBrowser() {
        return getProperty("browser");
    }

    public static String getUiBaseUrl() {
        return getProperty("ui.base.url");
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public static int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit.wait"));
    }

    public static int getExplicitWait() {
        return Integer.parseInt(getProperty("explicit.wait"));
    }

    // UI / E2E Test Credentials
    public static String getUiTestEmail() {
        return getProperty("ui.test.email");
    }

    public static String getUiTestPassword() {
        return getProperty("ui.test.password");
    }

    // API Test Credentials
    public static String getApiTestEmail() {
        return getProperty("api.test.email");
    }

    public static String getApiTestPassword() {
        return getProperty("api.test.password");
    }

    // Retry Configuration
    public static int getRetryMaxCount() {
        String val = getProperty("retry.max.count");
        return (val != null) ? Integer.parseInt(val) : 2;
    }

    // API Test Data
    public static String getApiNoteTempCategory() {
        return getProperty("api.note.temp.category");
    }

    public static String getApiNoteTempTitle() {
        return getProperty("api.note.temp.title");
    }

    public static String getApiNoteTempDescription() {
        return getProperty("api.note.temp.description");
    }

    public static String getApiNoteXssTitle() {
        return getProperty("api.note.xss.title");
    }

    public static String getApiNoteXssCategory() {
        return getProperty("api.note.xss.category");
    }

    public static String getApiNoteXssDescription() {
        return getProperty("api.note.xss.description");
    }
}
