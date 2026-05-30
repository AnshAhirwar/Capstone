package com.practice.notes.retry;

import com.practice.notes.utils.ConfigReader;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * SmartRetryAnalyzer — Agentic Automation Component (Section 3.3)
 *
 * Decision-based rerun logic for TestNG Cucumber scenarios.
 *
 * Retry policy:
 *   - Retries ONLY on transient infrastructure failures:
 *       TimeoutException, StaleElementReferenceException, WebDriverException
 *   - Does NOT retry genuine assertion failures (AssertionError) — these
 *     indicate real test defects that should be reported immediately.
 *   - Max retries is configurable via {@code retry.max.count} in config.properties.
 *
 * Each retry attempt is logged with: exception type, scenario name, attempt number.
 */
public class SmartRetryAnalyzer implements IRetryAnalyzer {

    private int currentAttempt = 0;
    private final int maxRetries;

    // Exception types that warrant a retry — transient infrastructure issues
    private static final Class<?>[] RETRYABLE_EXCEPTIONS = {
        TimeoutException.class,
        StaleElementReferenceException.class,
        WebDriverException.class
    };

    public SmartRetryAnalyzer() {
        this.maxRetries = ConfigReader.getRetryMaxCount();
    }

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && currentAttempt < maxRetries) {
            Throwable cause = result.getThrowable();

            // Never retry assertion failures — they represent real test defects
            if (cause instanceof AssertionError) {
                System.out.println("[SmartRetry] AssertionError detected — NOT retrying '"
                        + result.getName() + "'. This is a genuine test failure.");
                return false;
            }

            // Retry only if the exception is a known transient type
            if (isRetryable(cause)) {
                currentAttempt++;
                System.out.println("[SmartRetry] Transient failure detected ("
                        + (cause != null ? cause.getClass().getSimpleName() : "unknown") + ") on '"
                        + result.getName() + "'. Retry attempt " + currentAttempt
                        + " of " + maxRetries + ".");
                return true;
            }

            System.out.println("[SmartRetry] Non-retryable exception ("
                    + (cause != null ? cause.getClass().getSimpleName() : "unknown") + ") on '"
                    + result.getName() + "' — skipping retry.");
        }
        return false;
    }

    /**
     * Checks whether the given throwable (or its cause chain) is a retryable
     * transient exception.
     */
    private boolean isRetryable(Throwable throwable) {
        if (throwable == null) return false;
        for (Class<?> retryableType : RETRYABLE_EXCEPTIONS) {
            if (retryableType.isInstance(throwable)) {
                return true;
            }
        }
        // Check the root cause chain
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return isRetryable(cause);
        }
        return false;
    }
}
