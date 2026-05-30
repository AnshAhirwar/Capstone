package com.practice.notes.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.notes.mcp.McpSeleniumBridge.McpResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * McpScriptRunner — MCP Implementation Layer (Section 3.4)
 *
 * Reads MCP JSON script files from the classpath (src/test/resources/mcp/)
 * and executes each command step sequentially via {@link McpSeleniumBridge}.
 *
 * Script file format — a JSON array of command objects:
 * [
 *   { "command": "navigate", "params": { "url": "https://example.com" } },
 *   { "command": "click",    "params": { "testid": "login-button" } },
 *   { "command": "type",     "params": { "id": "email", "text": "user@test.com" } },
 *   { "command": "assert_text", "params": { "text": "Dashboard" } },
 *   { "command": "screenshot", "params": {} }
 * ]
 *
 * Usage:
 *   McpScriptRunner runner = new McpScriptRunner();
 *   McpScriptRunner.ScriptResult result = runner.runScript("mcp/login_flow.json");
 *   System.out.println(result.getSummary());
 */
public class McpScriptRunner {

    private final McpSeleniumBridge bridge;
    private final ObjectMapper mapper = new ObjectMapper();

    public McpScriptRunner() {
        this.bridge = new McpSeleniumBridge();
    }

    /**
     * Loads and executes an MCP script file from the classpath.
     *
     * @param classpathResource path relative to classpath root, e.g. "mcp/login_flow.json"
     * @return ScriptResult containing per-step outcomes and overall pass/fail
     */
    public ScriptResult runScript(String classpathResource) {
        System.out.println("[MCP Runner] Loading script: " + classpathResource);
        List<StepResult> stepResults = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            if (is == null) {
                throw new IllegalArgumentException("MCP script not found on classpath: " + classpathResource);
            }

            JsonNode steps = mapper.readTree(is);
            if (!steps.isArray()) {
                throw new IllegalArgumentException("MCP script must be a JSON array of command objects.");
            }

            System.out.println("[MCP Runner] Found " + steps.size() + " step(s) to execute.");

            for (int i = 0; i < steps.size(); i++) {
                JsonNode step = steps.get(i);
                String commandName = step.path("command").asText("unknown");
                String stepJson = step.toString();

                System.out.println("[MCP Runner] Step " + (i + 1) + "/" + steps.size() + ": " + commandName);
                long startMs = System.currentTimeMillis();
                McpResult result = bridge.execute(stepJson);
                long durationMs = System.currentTimeMillis() - startMs;

                StepResult stepResult = new StepResult(i + 1, commandName, result, durationMs);
                stepResults.add(stepResult);
                System.out.println("[MCP Runner] " + stepResult);

                // Stop script execution on first failure
                if (!result.success) {
                    System.err.println("[MCP Runner] Script halted at step " + (i + 1)
                            + " due to failure: " + result.message);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[MCP Runner] Script execution error: " + e.getMessage());
            McpResult errorResult = McpResult.failure("Script load/parse error: " + e.getMessage());
            stepResults.add(new StepResult(0, "script_load", errorResult, 0));
        }

        return new ScriptResult(classpathResource, stepResults);
    }

    // -------------------------------------------------------------------------
    // Result Models
    // -------------------------------------------------------------------------

    /** Result for a single MCP script step. */
    public static class StepResult {
        public final int stepNumber;
        public final String command;
        public final McpResult mcpResult;
        public final long durationMs;

        public StepResult(int stepNumber, String command, McpResult mcpResult, long durationMs) {
            this.stepNumber = stepNumber;
            this.command = command;
            this.mcpResult = mcpResult;
            this.durationMs = durationMs;
        }

        @Override
        public String toString() {
            return "Step " + stepNumber + " [" + command + "]: "
                    + (mcpResult.success ? "PASSED" : "FAILED")
                    + " (" + durationMs + "ms) — " + mcpResult.message;
        }
    }

    /** Aggregated result for an entire MCP script run. */
    public static class ScriptResult {
        public final String scriptName;
        public final List<StepResult> steps;

        public ScriptResult(String scriptName, List<StepResult> steps) {
            this.scriptName = scriptName;
            this.steps = steps;
        }

        public boolean isSuccess() {
            return steps.stream().allMatch(s -> s.mcpResult.success);
        }

        public int getPassedCount() {
            return (int) steps.stream().filter(s -> s.mcpResult.success).count();
        }

        public int getFailedCount() {
            return (int) steps.stream().filter(s -> !s.mcpResult.success).count();
        }

        public String getSummary() {
            return "[MCP Script: " + scriptName + "] "
                    + (isSuccess() ? "ALL PASSED" : "FAILED")
                    + " | Steps: " + steps.size()
                    + " | Passed: " + getPassedCount()
                    + " | Failed: " + getFailedCount();
        }
    }
}
