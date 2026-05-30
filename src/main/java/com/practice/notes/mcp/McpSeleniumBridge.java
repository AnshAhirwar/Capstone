package com.practice.notes.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.notes.drivers.DriverManager;
import com.practice.notes.utils.SelfHealingElement;
import com.practice.notes.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Base64;

/**
 * McpSeleniumBridge — MCP Implementation Layer (Section 3.4)
 *
 * Implements the Model Context Protocol (MCP) Selenium integration.
 * Provides a structured JSON-based command dispatch interface that allows
 * test scripts to be generated and executed programmatically — enabling
 * LLM-driven or agentic test script generation workflows.
 *
 * Supported MCP Commands:
 *   - navigate       : Navigate the browser to a given URL
 *   - click          : Click an element identified by a locator
 *   - type           : Enter text into a field (React-safe via native setter)
 *   - assert_text    : Assert that specified text is visible on the page
 *   - screenshot     : Capture the current screen as a Base64 PNG string
 *   - get_page_source: Return the raw HTML source of the current page
 *
 * Command JSON format:
 * {
 *   "command": "navigate",
 *   "params": { "url": "https://example.com" }
 * }
 *
 * All commands dispatch through DriverManager and inherit the framework's
 * self-healing locator and smart retry behaviour.
 */
public class McpSeleniumBridge {

    private final ObjectMapper mapper = new ObjectMapper();
    private WebDriver driver;
    private WebDriverWait wait;
    private SelfHealingElement selfHealing;

    public McpSeleniumBridge() {
        this.driver = DriverManager.getDriver();
        int waitSeconds = ConfigReader.getExplicitWait();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
        this.selfHealing = new SelfHealingElement(driver, waitSeconds);
    }

    /**
     * Dispatches a single MCP command represented as a JSON string.
     *
     * @param commandJson JSON string containing "command" and "params" fields
     * @return McpResult with status and optional output payload
     */
    public McpResult execute(String commandJson) {
        try {
            JsonNode root = mapper.readTree(commandJson);
            String command = root.path("command").asText();
            JsonNode params = root.path("params");
            System.out.println("[MCP] Executing command: " + command + " | params: " + params);

            switch (command) {
                case "navigate":
                    return executeNavigate(params);
                case "click":
                    return executeClick(params);
                case "type":
                    return executeType(params);
                case "assert_text":
                    return executeAssertText(params);
                case "screenshot":
                    return executeScreenshot();
                case "get_page_source":
                    return executeGetPageSource();
                default:
                    return McpResult.failure("Unknown MCP command: " + command);
            }
        } catch (Exception e) {
            System.err.println("[MCP] Command execution failed: " + e.getMessage());
            return McpResult.failure("MCP execution error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Command Implementations
    // -------------------------------------------------------------------------

    private McpResult executeNavigate(JsonNode params) {
        String url = params.path("url").asText();
        if (url.isEmpty()) return McpResult.failure("navigate: 'url' param is required");
        try {
            driver.get(url);
        } catch (TimeoutException e) {
            System.out.println("[MCP] navigate: page load timed out (EAGER mode), proceeding: " + e.getMessage());
        }
        System.out.println("[MCP] Navigated to: " + url);
        return McpResult.success("Navigated to " + url, null);
    }

    private McpResult executeClick(JsonNode params) {
        By locator = resolveLocator(params);
        if (locator == null) return McpResult.failure("click: no valid locator provided in params");

        By[] fallbacks = resolveFallbacks(params);
        WebElement el = selfHealing.findWithFallbacks(locator, fallbacks);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("[MCP] click: intercepted, retrying with JS click.");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
        System.out.println("[MCP] Clicked element: " + locator);
        return McpResult.success("Clicked " + locator, null);
    }

    private McpResult executeType(JsonNode params) {
        By locator = resolveLocator(params);
        if (locator == null) return McpResult.failure("type: no valid locator provided in params");
        String text = params.path("text").asText();

        By[] fallbacks = resolveFallbacks(params);
        WebElement el = selfHealing.findWithFallbacks(locator, fallbacks);
        String tagName = el.getTagName().toLowerCase();

        // React-safe value setter — triggers _valueTracker for controlled components
        ((JavascriptExecutor) driver).executeScript(
            "var proto = arguments[2] === 'textarea'" +
            "  ? window.HTMLTextAreaElement.prototype" +
            "  : window.HTMLInputElement.prototype;" +
            "var nativeSetter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
            "nativeSetter.call(arguments[0], arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            el, text, tagName
        );
        System.out.println("[MCP] Typed '" + text + "' into: " + locator);
        return McpResult.success("Typed into " + locator, null);
    }

    private McpResult executeAssertText(JsonNode params) {
        String expectedText = params.path("text").asText();
        if (expectedText.isEmpty()) return McpResult.failure("assert_text: 'text' param is required");

        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), expectedText));
            System.out.println("[MCP] assert_text: found '" + expectedText + "' on page.");
            return McpResult.success("Text '" + expectedText + "' is present on page", null);
        } catch (Exception e) {
            String pageText = driver.findElement(By.tagName("body")).getText();
            return McpResult.failure("assert_text: expected '" + expectedText
                    + "' not found. Page excerpt: " + pageText.substring(0, Math.min(200, pageText.length())));
        }
    }

    private McpResult executeScreenshot() {
        byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        System.out.println("[MCP] Screenshot captured (" + bytes.length + " bytes).");
        return McpResult.success("Screenshot captured", base64);
    }

    private McpResult executeGetPageSource() {
        String source = driver.getPageSource();
        System.out.println("[MCP] Page source retrieved (" + source.length() + " chars).");
        return McpResult.success("Page source retrieved", source);
    }

    // -------------------------------------------------------------------------
    // Locator Resolution Helpers
    // -------------------------------------------------------------------------

    /** Resolves the primary By locator from params. */
    private By resolveLocator(JsonNode params) {
        if (params.has("css"))       return By.cssSelector(params.path("css").asText());
        if (params.has("id"))        return By.id(params.path("id").asText());
        if (params.has("xpath"))     return By.xpath(params.path("xpath").asText());
        if (params.has("testid"))    return By.cssSelector("[data-testid='" + params.path("testid").asText() + "']");
        if (params.has("name"))      return By.name(params.path("name").asText());
        return null;
    }

    /** Resolves optional fallback By locators from params["fallbacks"] array. */
    private By[] resolveFallbacks(JsonNode params) {
        JsonNode fb = params.path("fallbacks");
        if (fb.isMissingNode() || !fb.isArray()) return new By[0];
        By[] result = new By[fb.size()];
        for (int i = 0; i < fb.size(); i++) {
            result[i] = resolveLocator(fb.get(i));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Result Model
    // -------------------------------------------------------------------------

    /** Immutable result object returned by every MCP command. */
    public static class McpResult {
        public final boolean success;
        public final String message;
        public final String payload; // optional — e.g. Base64 screenshot or page source

        private McpResult(boolean success, String message, String payload) {
            this.success = success;
            this.message = message;
            this.payload = payload;
        }

        public static McpResult success(String message, String payload) {
            return new McpResult(true, message, payload);
        }

        public static McpResult failure(String message) {
            return new McpResult(false, message, null);
        }

        @Override
        public String toString() {
            return "[MCP Result] " + (success ? "SUCCESS" : "FAILURE") + " — " + message;
        }
    }
}
