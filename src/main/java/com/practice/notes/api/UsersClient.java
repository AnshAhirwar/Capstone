package com.practice.notes.api;

import com.practice.notes.base.BaseApiClient;
import com.practice.notes.api.models.User;
import io.restassured.response.Response;

public class UsersClient extends BaseApiClient {

    public Response register(User user) {
        return executeWithRetry(() -> getRequestSpec()
                .body(user)
                .post("/users/register"));
    }

    public Response login(User user) {
        return executeWithRetry(() -> getRequestSpec()
                .body(user)
                .post("/users/login"));
    }

    public void loginAndSetToken(String email, String password) {
        User credentials = new User(email, password);
        Response response = login(credentials);
        if (response.getStatusCode() == 200) {
            String token = response.jsonPath().getString("data.token");
            setAuthToken(token);
        } else {
            throw new RuntimeException("Login failed with status code " + response.getStatusCode() + ": " + response.getBody().asString());
        }
    }
    
    public Response getProfile() {
        return executeWithRetry(() -> getRequestSpec()
                .get("/users/profile"));
    }
}
