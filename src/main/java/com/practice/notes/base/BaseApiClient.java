package com.practice.notes.base;

import com.practice.notes.utils.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class BaseApiClient {
    protected String baseUri;

    // ThreadLocal ensures each parallel runner thread has its own isolated auth token.
    // Previously a static field caused threads to clobber each other's tokens → 401 errors.
    private static final ThreadLocal<String> authToken = new ThreadLocal<>();

    public BaseApiClient() {
        this.baseUri = ConfigReader.getApiBaseUrl();
    }

    public static void setAuthToken(String token) {
        authToken.set(token);
    }

    public static void clearAuthToken() {
        authToken.remove();
    }

    protected RequestSpecification getRequestSpec() {
        String token = authToken.get();
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUri)
                .contentType("application/json")
                .accept("application/json");

        if (token != null && !token.isEmpty()) {
            spec.header("x-auth-token", token);
            spec.header("Authorization", "Bearer " + token);
        }

        return spec;
    }

    protected io.restassured.response.Response executeWithRetry(java.util.function.Supplier<io.restassured.response.Response> requestSupplier) {
        int maxRetries = 3;
        int delayMs = 1500;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return requestSupplier.get();
            } catch (Exception e) {
                lastException = e;
                System.out.println("[BaseApiClient] Warning: Request failed (attempt " + attempt + "/" + maxRetries + "): " + e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(delayMs * attempt); } catch (InterruptedException ignored) {}
                }
            }
        }
        throw new RuntimeException("HTTP Request failed after " + maxRetries + " attempts due to transient network/SSL error", lastException);
    }
}
