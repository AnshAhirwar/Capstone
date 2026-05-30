package com.practice.notes.pages;

import com.practice.notes.base.BasePage;
import com.practice.notes.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {
    private final By nameInput = By.id("name");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By confirmPasswordInput = By.id("confirmPassword");
    private final By registerButton = By.cssSelector("button[type='submit']");
    private final By successMessage = By.cssSelector(".alert-success");
    private final By errorMessage = By.cssSelector(".alert-danger");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void navigateTo() {
        try {
            driver.get(ConfigReader.getUiBaseUrl() + "/register");
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[RegisterPage] Warning: Page load timed out on register page driver.get, but proceeding: " + e.getMessage());
        }
    }

    public void register(String name, String email, String password) {
        sendKeys(nameInput, name);
        sendKeys(emailInput, email);
        sendKeys(passwordInput, password);
        sendKeys(confirmPasswordInput, password);
        click(registerButton);
    }

    public String getSuccessMessageText() {
        return getText(successMessage);
    }

    public String getErrorMessageText() {
        return getText(errorMessage);
    }
}
