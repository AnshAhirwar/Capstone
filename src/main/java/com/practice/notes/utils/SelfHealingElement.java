package com.practice.notes.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * SelfHealingElement — Agentic Automation Component (Section 3.3)
 *
 * Provides resilient element location by attempting a prioritised chain of
 * Selenium By strategies. When the primary locator fails, it automatically
 * falls back through the provided alternatives and logs which strategy
 * succeeded, making tests robust to minor DOM attribute changes.
 *
 * Fallback resolution order (when using findWithFallbackChain):
 *   data-testid → id → name → CSS selector → XPath
 */
public class SelfHealingElement {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SelfHealingElement(WebDriver driver, int waitSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
    }

    /**
     * Attempts to find an element using the primary locator.
     * On failure, tries each fallback locator in order.
     * Logs which strategy was ultimately used.
     *
     * @param primary   The preferred By locator
     * @param fallbacks Additional By locators to try if the primary fails
     * @return The first successfully located WebElement
     * @throws org.openqa.selenium.NoSuchElementException if all strategies fail
     */
    public WebElement findWithFallbacks(By primary, By... fallbacks) {
        // Try primary locator first
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(primary));
            System.out.println("[SelfHealing] Primary locator succeeded: " + primary);
            return el;
        } catch (Exception primaryEx) {
            System.out.println("[SelfHealing] Primary locator failed (" + primary + "): " + primaryEx.getClass().getSimpleName()
                    + " — attempting fallbacks...");
        }

        // Try each fallback in order
        for (int i = 0; i < fallbacks.length; i++) {
            By fallback = fallbacks[i];
            try {
                WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(fallback));
                System.out.println("[SelfHealing] Fallback #" + (i + 1) + " succeeded: " + fallback);
                return el;
            } catch (Exception fallbackEx) {
                System.out.println("[SelfHealing] Fallback #" + (i + 1) + " failed (" + fallback + "): "
                        + fallbackEx.getClass().getSimpleName());
            }
        }

        throw new org.openqa.selenium.NoSuchElementException(
                "[SelfHealing] All locator strategies exhausted. Primary: " + primary
                        + ", Fallbacks tried: " + fallbacks.length);
    }

    /**
     * Resolves an element using the standard self-healing fallback chain:
     *   data-testid  → id  → name  → CSS  → XPath
     *
     * Derives alternative By selectors from a simple attribute value string.
     * Useful when only the target value (e.g. "email", "login") is known.
     *
     * @param dataTestId  value for [data-testid='...']
     * @param idValue     value for By.id(...)
     * @param nameValue   value for By.name(...)
     * @param cssSelector CSS selector string
     * @param xpathExpr   XPath expression string
     * @return The first successfully located WebElement
     */
    public WebElement findWithStandardChain(
            String dataTestId, String idValue, String nameValue,
            String cssSelector, String xpathExpr) {

        By[] chain = {
            By.cssSelector("[data-testid='" + dataTestId + "']"),
            By.id(idValue),
            By.name(nameValue),
            By.cssSelector(cssSelector),
            By.xpath(xpathExpr)
        };

        for (int i = 0; i < chain.length; i++) {
            try {
                WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(chain[i]));
                String[] strategyNames = {"data-testid", "id", "name", "CSS", "XPath"};
                System.out.println("[SelfHealing] Standard chain resolved via strategy [" + strategyNames[i] + "]: " + chain[i]);
                return el;
            } catch (Exception e) {
                // Continue to next strategy
            }
        }

        throw new org.openqa.selenium.NoSuchElementException(
                "[SelfHealing] Standard chain exhausted for dataTestId='" + dataTestId + "'");
    }
}
