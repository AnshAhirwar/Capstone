package com.practice.notes.hooks;

import com.practice.notes.base.BaseApiClient;
import com.practice.notes.drivers.DriverManager;
import com.practice.notes.utils.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.practice.notes.api.UsersClient;
import com.practice.notes.api.models.User;

public class Hooks {
    private static volatile boolean usersRegistered = false;
    private static final ThreadLocal<Scenario> currentScenario = new ThreadLocal<>();

    public static Scenario getCurrentScenario() {
        return currentScenario.get();
    }

    @Before(order = 0)
    public void captureScenario(Scenario scenario) {
        currentScenario.set(scenario);
    }

    @After(order = 10000)
    public void clearScenario() {
        currentScenario.remove();
    }

    @Before(order = 1)
    public void ensureUsersExist() {
        if (!usersRegistered) {
            synchronized (Hooks.class) {
                if (!usersRegistered) {
                    // Register UI / E2E user
                    try {
                        UsersClient client = new UsersClient();
                        User uiUser = new User("Ansh", ConfigReader.getUiTestEmail(), ConfigReader.getUiTestPassword());
                        client.register(uiUser);
                        System.out.println("Ensured UI/E2E user '" + ConfigReader.getUiTestEmail() + "' exists.");
                    } catch (Exception e) {
                        // Ignore — email already registered is perfectly fine
                    }

                    // Register dedicated API test user
                    try {
                        UsersClient client = new UsersClient();
                        User apiUser = new User("AnshApi", ConfigReader.getApiTestEmail(), ConfigReader.getApiTestPassword());
                        client.register(apiUser);
                        System.out.println("Ensured API user '" + ConfigReader.getApiTestEmail() + "' exists.");
                    } catch (Exception e) {
                        // Ignore — email already registered is perfectly fine
                    }

                    usersRegistered = true;
                }
            }
        }
    }

    @Before(order = 2, value = "@api")
    public void prepareDatabase() {
        // Cleans notes only for the dedicated API test user — never touches the UI user's data
        try {
            UsersClient usersClient = new UsersClient();
            User creds = new User(ConfigReader.getApiTestEmail(), ConfigReader.getApiTestPassword());
            io.restassured.response.Response response = usersClient.login(creds);
            if (response.getStatusCode() == 200) {
                String token = response.jsonPath().getString("data.token");
                UsersClient.setAuthToken(token);
                new com.practice.notes.api.NotesClient().deleteAllNotes();
                System.out.println("Cleaned up all existing notes for '" + ConfigReader.getApiTestEmail() + "' to guarantee a pristine database state.");
            }
        } catch (Exception e) {
            System.err.println("Database pre-clean skipped: " + e.getMessage());
        } finally {
            BaseApiClient.clearAuthToken();
        }
    }

    @Before(order = 2, value = "@ui or @e2e")
    public void prepareUiDatabase() {
        try {
            UsersClient usersClient = new UsersClient();
            User creds = new User(ConfigReader.getUiTestEmail(), ConfigReader.getUiTestPassword());
            io.restassured.response.Response response = usersClient.login(creds);
            if (response.getStatusCode() == 200) {
                String token = response.jsonPath().getString("data.token");
                UsersClient.setAuthToken(token);
                new com.practice.notes.api.NotesClient().deleteAllNotes();
                System.out.println("Cleaned up all existing notes for '" + ConfigReader.getUiTestEmail() + "' to guarantee a pristine database state.");
            }
        } catch (Exception e) {
            System.err.println("UI/E2E Database pre-clean skipped: " + e.getMessage());
        } finally {
            BaseApiClient.clearAuthToken();
        }
    }

    @Before("@ui or @e2e")
    public void setupUi() {
        DriverManager.getDriver();
    }

    @After("@ui or @e2e")
    public void teardownUi(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                WebDriver driver = DriverManager.getDriver();
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failed Scenario Screenshot");
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Before("@api or @e2e")
    public void setupApi() {
        BaseApiClient.clearAuthToken();
    }
}

