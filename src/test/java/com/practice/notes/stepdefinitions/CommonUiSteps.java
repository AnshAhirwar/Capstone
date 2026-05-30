package com.practice.notes.stepdefinitions;

import com.practice.notes.drivers.DriverManager;
import com.practice.notes.pages.DashboardPage;
import com.practice.notes.pages.LoginPage;
import com.practice.notes.utils.ConfigReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class CommonUiSteps {
    private WebDriver driver;
    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    private void initPages() {
        driver = DriverManager.getDriver();
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
    }

    @Given("I open the Notes App home page")
    public void iOpenNotesAppHomePage() {
        initPages();
        try {
            driver.get(ConfigReader.getUiBaseUrl());
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[CommonUiSteps] Warning: Page load timed out on home page driver.get, but proceeding: " + e.getMessage());
        }
        // Expandtesting usually redirects to welcome, let's navigate to login directly if welcome shown or check landing URL
        String currentUrl = "";
        try {
            currentUrl = driver.getCurrentUrl();
        } catch (Exception ignored) {}
        if (currentUrl == null || currentUrl.endsWith("/app") || currentUrl.endsWith("/app/")) {
            try {
                driver.get(ConfigReader.getUiBaseUrl() + "/login");
            } catch (org.openqa.selenium.TimeoutException e) {
                System.out.println("[CommonUiSteps] Warning: Page load timed out on login page driver.get, but proceeding: " + e.getMessage());
            }
        }
    }

    @When("I enter email {string} on the UI")
    public void iEnterEmailOnUi(String email) {
        initPages();
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        org.openqa.selenium.WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        try { element.click(); } catch (Exception ignored) {}
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            element, email
        );
        System.out.println("[CommonUiSteps] Email set to: " + email);
    }

    @When("I enter password {string} on the UI")
    public void iEnterPasswordOnUi(String password) {
        initPages();
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        org.openqa.selenium.WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        try { element.click(); } catch (Exception ignored) {}
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            element, password
        );
        System.out.println("[CommonUiSteps] Password set (length: " + password.length() + ")");
    }

    @When("I click the Login button on the UI")
    public void iClickLoginButtonOnUi() {
        initPages();
        loginPage.clickSubmit();
    }

    @Then("I should observe the dashboard page after successful submit")
    public void iShouldObserveDashboardPage() {
        initPages();
        Assert.assertTrue(dashboardPage.isLogoutButtonDisplayed(), "Dashboard page not displayed after login!");
    }

    @Given("I am successfully logged in to the UI dashboard with email {string} and password {string}")
    public void iAmSuccessfullyLoggedInToUi(String email, String password) {
        initPages();
        try {
            driver.get(ConfigReader.getUiBaseUrl() + "/login");
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[CommonUiSteps] Warning: Page load timed out on login page driver.get, but proceeding: " + e.getMessage());
        }
        dashboardPage = loginPage.login(email, password);
        Assert.assertTrue(dashboardPage.isLogoutButtonDisplayed(), "Failed to log in visibly!");
    }

    @When("I click the {string} button")
    public void iClickAddNoteButton(String buttonText) {
        initPages();
        if (buttonText.equals("+ Add Note")) {
            try { Thread.sleep(1200); } catch (InterruptedException e) {}
            dashboardPage.clickAddNoteButton();
        }
    }

    @When("I create a new note with category {string}, title {string}, and description {string}")
    public void iCreateNewNote(String category, String title, String description) {
        initPages();
        dashboardPage.createNote(category, title, description);
    }

    @When("I select Category {string} on the form")
    public void iSelectCategory(String category) {
        initPages();
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        org.openqa.selenium.WebElement selectElement = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("category")));
        
        // React-safe select: use the native HTMLSelectElement prototype value setter.
        // Calling element.value = x directly doesn't trigger React's internal _valueTracker.
        // The prototype setter does, so React detects the change and updates its state.
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            selectElement, category
        );
        System.out.println("[CommonUiSteps] Set category to: " + category + " (React-safe native setter)");
    }

    @When("I enter Title {string} on the form")
    public void iEnterTitle(String title) {
        initPages();
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        org.openqa.selenium.WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        try { element.click(); } catch (Exception ignored) {}
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            element, title
        );
        System.out.println("[CommonUiSteps] Title set to: " + title);
    }

    @When("I enter Description {string} on the form")
    public void iEnterDescription(String desc) {
        initPages();
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        // Description field may be a textarea — detect tag and use matching prototype
        org.openqa.selenium.WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("description")));
        try { element.click(); } catch (Exception ignored) {}
        String tagName = element.getTagName().toLowerCase();
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var proto = arguments[2] === 'textarea'" +
            "  ? window.HTMLTextAreaElement.prototype" +
            "  : window.HTMLInputElement.prototype;" +
            "var nativeSetter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            element, desc, tagName
        );
        System.out.println("[CommonUiSteps] Description set to: " + desc);
    }

    @When("I click {string} on the form")
    public void iClickSave(String btnText) {
        initPages();
        if (btnText.equals("Save")) {
            By submitBtn = By.cssSelector("[data-testid='note-submit']");
            
            // Wait for modal animation to complete and React event handlers to bind securely
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            org.openqa.selenium.WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(submitBtn));
            
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception e) {
                element.click();
            }
            
            // Wait for modal to disappear
            try {
                wait.until(org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated(submitBtn));
            } catch (Exception e) {}
        }
    }

    @Then("I should observe the note {string} immediately in the UI list")
    public void iShouldObserveNoteImmediately(String title) {
        initPages();
        // Give UI a brief moment to load and display the new note card
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        Assert.assertTrue(dashboardPage.doesNoteExist(title), "Note titled '" + title + "' was not found on the UI dashboard!");
    }

    @Then("I should see the UI error message containing {string} or {string}")
    public void iShouldSeeUiErrorMessage(String expected1, String expected2) {
        initPages();
        String errorText = loginPage.getErrorMessageText();
        System.out.println("Observed Login Error: " + errorText);
        Assert.assertTrue(errorText.toLowerCase().contains(expected1.toLowerCase()) ||
                errorText.toLowerCase().contains(expected2.toLowerCase()),
                "Login error text mismatch! Observed: " + errorText);
    }

    // --- New steps for expanded coverage ---

    @When("I click the logout button on the UI")
    public void iClickLogoutButtonOnUi() {
        initPages();
        dashboardPage.clickLogout();
    }

    @Then("I should be redirected to the login page")
    public void iShouldBeRedirectedToLoginPage() {
        initPages();
        // Wait for the dashboard logout button to disappear — indicates successful logout
        org.openqa.selenium.support.ui.WebDriverWait wait =
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions
            .invisibilityOfElementLocated(By.cssSelector("[data-testid='logout']")));
        // Then verify login form (email input) is present OR we are on the landing page (successfully logged out)
        boolean hasLoginForm = !driver.findElements(By.id("email")).isEmpty();
        boolean onLandingPage = driver.getCurrentUrl().endsWith("/notes/app") || driver.getCurrentUrl().endsWith("/notes/app/");
        boolean onLoginPage = driver.getCurrentUrl().contains("/login");
        Assert.assertTrue(hasLoginForm || onLandingPage || onLoginPage,
                "Neither login page nor landing page shown after logout! Current URL: " + driver.getCurrentUrl());
    }

    @Then("the note {string} should show description {string} in the UI card")
    public void theNoteShouldShowDescriptionInUiCard(String title, String expectedDescription) {
        initPages();
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        String actualDescription = dashboardPage.getNoteDescription(title);
        System.out.println("UI Card Description for '" + title + "': " + actualDescription);
        Assert.assertEquals(actualDescription.toLowerCase(), expectedDescription.toLowerCase(),
                "Note description mismatch on UI card for note '" + title + "'!");
    }

    @When("I delete the note {string} via the UI dashboard")
    public void iDeleteNoteViaUiDashboard(String title) {
        initPages();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        dashboardPage.deleteNoteByTitle(title);
        dashboardPage.confirmDeleteIfModalAppears();
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
    }

    @Then("the note {string} should not be visible on the dashboard")
    public void theNoteShouldNotBeVisibleOnDashboard(String title) {
        initPages();
        Assert.assertFalse(dashboardPage.doesNoteExist(title),
                "Note '" + title + "' is still visible on the dashboard after UI deletion!");
    }

    @When("I enter an empty password on the UI")
    public void iEnterEmptyPasswordOnUi() {
        initPages();
        // Explicitly clear and submit empty — triggers HTML5 required field validation
        org.openqa.selenium.support.ui.WebDriverWait wait =
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        org.openqa.selenium.WebElement element =
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        element.clear();
        System.out.println("[CommonUiSteps] Password field cleared (empty password scenario)");
    }

    @Then("I should see a login validation error")
    public void iShouldSeeLoginValidationError() {
        initPages();
        // Empty password: browser HTML5 `required` prevents server call OR server returns error.
        // Either way, the user must NOT reach the dashboard. We verify they stay on login page.
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
        boolean notOnDashboard = driver.findElements(
            By.cssSelector("[data-testid='logout']")).isEmpty();
        boolean onLoginOrApp = driver.getCurrentUrl().contains("/login")
            || !driver.findElements(By.id("email")).isEmpty();
        Assert.assertTrue(notOnDashboard && onLoginOrApp,
                "Empty password login should have failed but user reached dashboard! URL: "
                + driver.getCurrentUrl());
    }

    @Then("I measure and log UI performance timings for {string}")
    public void iMeasureAndLogUiPerformanceTimingsFor(String pageName) {
        initPages();
        io.cucumber.java.Scenario scenario = com.practice.notes.hooks.Hooks.getCurrentScenario();
        String scenarioName = (scenario != null) ? scenario.getName() : "Unknown UI Scenario";
        com.practice.notes.utils.PerformanceTracker.logUiMetrics(scenarioName, pageName, driver);
    }
}
