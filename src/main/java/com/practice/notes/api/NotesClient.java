package com.practice.notes.api;

import com.practice.notes.base.BaseApiClient;
import com.practice.notes.api.models.Note;
import io.restassured.response.Response;

import java.util.List;

public class NotesClient extends BaseApiClient {

    public Response createNote(Note note) {
        return executeWithRetry(() -> getRequestSpec()
                .body(note)
                .post("/notes"));
    }

    public Response getAllNotes() {
        return executeWithRetry(() -> getRequestSpec()
                .get("/notes"));
    }

    public Response getNoteById(String noteId) {
        return executeWithRetry(() -> getRequestSpec()
                .pathParam("id", noteId)
                .get("/notes/{id}"));
    }

    public Response updateNote(String noteId, Note note) {
        return executeWithRetry(() -> getRequestSpec()
                .pathParam("id", noteId)
                .body(note)
                .put("/notes/{id}"));
    }

    public Response deleteNote(String noteId) {
        return executeWithRetry(() -> getRequestSpec()
                .pathParam("id", noteId)
                .delete("/notes/{id}"));
    }

    public void deleteAllNotes() {
        try {
            Response response = getAllNotes();
            if (response.getStatusCode() == 200) {
                List<String> ids = response.jsonPath().getList("data.id");
                if (ids != null) {
                    for (String id : ids) {
                        deleteNote(id);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Clean-up failed: " + e.getMessage());
        }
    }
}
