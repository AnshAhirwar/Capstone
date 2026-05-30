package com.practice.notes.pages;

import com.practice.notes.base.BasePage;
import com.practice.notes.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class LoginPage extends BasePage {
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");

    // Multiple selector candidates for error messages across different Bootstrap/React patterns
    private static final List<By> ERROR_SELECTORS = Arrays.asList(
        By.cssSelector(".alert-danger"),
        By.cssSelector("[data-testid='login-message']"),
        By.cssSelector("[data-testid='alert-message']"),
        By.cssSelector(".toast-body"),
        By.cssSelector("[role='alert']"),
        By.cssSelector(".alert"),
        By.cssSelector(".error-message"),
        By.cssSelector(".text-danger")
    );

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void navigateTo() {
        try {
            driver.get(ConfigReader.getUiBaseUrl() + "/login");
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[LoginPage] Warning: Page load timed out on login page driver.get, but proceeding: " + e.getMessage());
        }
    }

    public DashboardPage login(String email, String password) {
        sendKeys(emailInput, email);
        sendKeys(passwordInput, password);
        click(loginButton);
        return new DashboardPage(driver);
    }

    public String getErrorMessageText() {
        // Wait up to 20s for ANY of the known error selectors to show non-empty text.
        // Checks all selectors simultaneously on each poll, returns first non-empty text found.
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            String found = longWait.until(d -> {
                for (By selector : ERROR_SELECTORS) {
                    try {
                        WebElement el = d.findElement(selector);
                        String text = el.getText().trim();
                        if (el.isDisplayed() && !text.isEmpty()) {
                            System.out.println("[LoginPage] Error message found via " + selector + ": " + text);
                            return text;
                        }
                    } catch (Exception ignored) {}
                }
                return null; // null = keep polling
            });
            return found != null ? found : "";
        } catch (Exception e) {
            System.out.println("[LoginPage] Warning: no error message found after 20s. Page title: " + driver.getTitle());
            return "";
        }
    }


    public void clickSubmit() {
        click(loginButton);
    }
}
