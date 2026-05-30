package com.practice.notes.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PerformanceTracker {
    private static final String LOG_FILE_PATH = "performance-trend.log";
    private static final Object lock = new Object();

    public static void logApiMetric(String scenarioName, String endpoint, long responseTimeMs) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String logEntry = String.format("%s | [API] | Scenario: %s | Endpoint: %s | Response Time: %d ms",
                timestamp, scenarioName, endpoint, responseTimeMs);

        System.out.println("[PERFORMANCE-API] " + logEntry);
        writeToLogFile(logEntry);
    }

    public static void logUiMetrics(String scenarioName, String pageName, WebDriver driver) {
        if (!(driver instanceof JavascriptExecutor)) {
            System.err.println("Driver does not support JavascriptExecutor. Cannot measure UI timings.");
            return;
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Wait up to 5 seconds for document.readyState to be complete just in case
        int retries = 50;
        while (retries > 0) {
            String readyState = (String) js.executeScript("return document.readyState;");
            if ("complete".equals(readyState)) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            retries--;
        }

        try {
            // Retrieve performance.timing metrics
            Number navigationStartNum = (Number) js.executeScript("return window.performance.timing.navigationStart;");
            Number loadEventEndNum = (Number) js.executeScript("return window.performance.timing.loadEventEnd;");
            Number domContentLoadedEventEndNum = (Number) js.executeScript("return window.performance.timing.domContentLoadedEventEnd;");

            if (navigationStartNum == null || navigationStartNum.longValue() == 0) {
                System.out.println("[PERFORMANCE-UI] Performance timing not available or navigationStart is 0.");
                return;
            }

            long navigationStart = navigationStartNum.longValue();
            long loadEventEnd = loadEventEndNum != null ? loadEventEndNum.longValue() : 0;
            long domContentLoadedEventEnd = domContentLoadedEventEndNum != null ? domContentLoadedEventEndNum.longValue() : 0;

            // Fallback to current time if loadEventEnd hasn't fired yet
            if (loadEventEnd == 0) {
                loadEventEnd = System.currentTimeMillis();
            }
            if (domContentLoadedEventEnd == 0) {
                domContentLoadedEventEnd = System.currentTimeMillis();
            }

            long pageNavigationTime = loadEventEnd - navigationStart;
            long domReadyTime = domContentLoadedEventEnd - navigationStart;

            // Make sure values are non-negative
            if (pageNavigationTime < 0) pageNavigationTime = 0;
            if (domReadyTime < 0) domReadyTime = 0;

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            String logEntry = String.format("%s | [UI]  | Scenario: %s | Page: %s | Navigation Time: %d ms | DOM Ready Time: %d ms",
                    timestamp, scenarioName, pageName, pageNavigationTime, domReadyTime);

            System.out.println("[PERFORMANCE-UI] " + logEntry);
            writeToLogFile(logEntry);

        } catch (Exception e) {
            System.err.println("[PERFORMANCE-UI] Error measuring UI timings: " + e.getMessage());
        }
    }

    private static void writeToLogFile(String entry) {
        synchronized (lock) {
            File logFile = new File(LOG_FILE_PATH);
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(entry);
            } catch (IOException e) {
                System.err.println("Failed to write to performance-trend.log: " + e.getMessage());
            }
        }
    }
}
