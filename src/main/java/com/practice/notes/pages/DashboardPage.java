package com.practice.notes.pages;

import com.practice.notes.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class DashboardPage extends BasePage {
    private final By logoutButton = By.cssSelector("[data-testid='logout']");
    private final By addNoteButton = By.cssSelector("[data-testid='add-new-note']");
    
    // Add Note Modal Form Elements
    private final By categorySelect = By.id("category");
    private final By titleInput = By.id("title");
    private final By descriptionInput = By.id("description");
    private final By noteSubmitButton = By.cssSelector("[data-testid='note-submit']");
    
    // Note Cards Elements
    private final By noteCard = By.cssSelector("[data-testid='note-card']");
    private final By noteCardTitle = By.cssSelector("[data-testid='note-card-title']");
    private final By noteCardDescription = By.cssSelector("[data-testid='note-card-description']");
    private final By deleteNoteButton = By.cssSelector("[data-testid='note-delete']");
    private final By editNoteButton = By.cssSelector("[data-testid='note-edit']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLogoutButtonDisplayed() {
        return isDisplayed(logoutButton);
    }

    public void clickLogout() {
        click(logoutButton);
    }

    public void clickAddNoteButton() {
        WebElement element = findElement(addNoteButton);
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            click(addNoteButton);
        }
    }

    public void createNote(String category, String title, String description) {
        // Brief wait to ensure dashboard is fully settled
        try { Thread.sleep(800); } catch (InterruptedException e) {}
        clickAddNoteButton();
        
        // Wait for modal to be fully rendered (category select must be visible)
        org.openqa.selenium.support.ui.WebDriverWait modalWait =
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        WebElement selectElement = modalWait.until(
            org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(categorySelect)
        );
        
        // React-safe select: use the native HTMLSelectElement prototype value setter.
        // Calling element.value = x directly doesn't trigger React's internal _valueTracker.
        // The prototype setter does, so React detects the change and updates its state.
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            selectElement, category
        );
        System.out.println("[DashboardPage] Set category to: " + category + " (React-safe native setter)");
        
        sendKeys(titleInput, title);
        sendKeys(descriptionInput, description);
        
        // Wait for React to finish processing state updates before submitting
        try { Thread.sleep(600); } catch (InterruptedException e) {}
        
        // JS-click the submit button directly — avoids intercept issues from overlapping elements
        WebElement submitBtn = modalWait.until(
            org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(noteSubmitButton)
        );
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        System.out.println("[DashboardPage] Clicked note-submit button");
        
        // Wait for modal to close (submit button disappears) — up to 15 seconds
        try {
            waitForInvisibility(noteSubmitButton);
            System.out.println("[DashboardPage] Modal closed successfully.");
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[DashboardPage] Warning: modal did not close within timeout. Checking for any error on form...");
            // Print any visible form error for debugging
            try {
                WebElement formErr = driver.findElement(By.cssSelector(".alert-danger, .invalid-feedback, .form-error"));
                System.out.println("[DashboardPage] Form error visible: " + formErr.getText());
            } catch (Exception ignored) {}
        }
    }

    public boolean doesNoteExist(String title) {
        try {
            List<WebElement> titles = driver.findElements(noteCardTitle);
            for (WebElement titleElement : titles) {
                String displayedTitle = titleElement.getText().trim();
                System.out.println("Observed Note Title on Dashboard: '" + displayedTitle + "'");
                if (displayedTitle.equalsIgnoreCase(title)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking note existence on UI: " + e.getMessage());
            return false;
        }
        return false;
    }

    public String getNoteDescription(String noteTitle) {
        List<WebElement> cards = findElements(noteCard);
        for (WebElement card : cards) {
            WebElement titleEl = card.findElement(noteCardTitle);
            if (titleEl.getText().trim().equals(noteTitle)) {
                return card.findElement(noteCardDescription).getText().trim();
            }
        }
        throw new RuntimeException("Note card with title '" + noteTitle + "' not found");
    }

    public void deleteNoteByTitle(String noteTitle) {
        List<WebElement> cards = findElements(noteCard);
        for (WebElement card : cards) {
            WebElement titleEl = card.findElement(noteCardTitle);
            if (titleEl.getText().trim().equals(noteTitle)) {
                WebElement deleteBtn = card.findElement(deleteNoteButton);
                click(deleteBtn);
                
                // Confirm delete modal button if any - let's check.
                // React Notes app usually has a direct click or confirmation dialog.
                // We'll wait until the card title is invisible or cards list shrinks.
                return;
            }
        }
        throw new RuntimeException("Note card with title '" + noteTitle + "' not found to delete");
    }

    public void confirmDeleteIfModalAppears() {
        // Wait up to 5s for a confirmation modal button to appear after clicking delete.
        // The expandtesting Notes app shows a confirmation dialog before permanently deleting.
        By confirmBtn = By.cssSelector("[data-testid='note-delete-confirm']");
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(confirmBtn));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            System.out.println("[DashboardPage] Clicked note-delete-confirm button.");
            // Wait for the modal to close and card to disappear
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[DashboardPage] No confirm modal appeared — deletion may be immediate.");
        }
    }

    public void waitForLogoutButtonToDisappear() {
        waitForInvisibility(logoutButton);
    }
}
