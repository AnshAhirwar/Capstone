package com.practice.notes.base;

import com.practice.notes.utils.ConfigReader;
import com.practice.notes.utils.SelfHealingElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected SelfHealingElement selfHealing;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWait()));
        this.selfHealing = new SelfHealingElement(driver, ConfigReader.getExplicitWait());
    }

    /**
     * Self-healing element lookup — tries the primary locator, then falls back
     * through the provided alternatives in order. Logs which strategy succeeded.
     * Use this in page objects where locators may drift across app releases.
     */
    protected WebElement findElementSelfHealing(By primary, By... fallbacks) {
        return selfHealing.findWithFallbacks(primary, fallbacks);
    }

    protected WebElement findElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> findElements(By locator) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    protected void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            element.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            System.out.println("Element click intercepted for " + locator + ". Retrying with JS click.");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected void click(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            System.out.println("Element click intercepted. Retrying with JS click.");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected void sendKeys(By locator, String text) {
        WebElement element = findElement(locator);
        // Click to focus the field first
        try { element.click(); } catch (Exception ignored) {}
        String tagName = element.getTagName().toLowerCase();
        try {
            // React-safe: use the native prototype value setter to trigger _valueTracker.
            // Regular element.value = x or element.clear()/sendKeys() does NOT update React's
            // internal state; the prototype setter does, just like the select fix.
            ((JavascriptExecutor) driver).executeScript(
                "var proto = arguments[2] === 'textarea'" +
                "  ? window.HTMLTextAreaElement.prototype" +
                "  : window.HTMLInputElement.prototype;" +
                "var nativeSetter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
                "nativeSetter.call(arguments[0], arguments[1]);" +
                "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                element, text, tagName
            );
        } catch (Exception e) {
            // Fallback for environments where the prototype trick is unavailable
            element.clear();
            element.sendKeys(text);
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    element
                );
            } catch (Exception ignored) {}
        }
    }


    protected String getText(By locator) {
        return findElement(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}
