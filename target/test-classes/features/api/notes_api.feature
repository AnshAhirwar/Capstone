@api
Feature: API Scenarios
  As a capstone student, I want to execute CRUD, performance, and negative checks on note API endpoints.

  Scenario: Validate GET notes returns list and responds within 2 seconds (Scenario C)
    Given I log in via API with credentials from config
    When I retrieve all my notes via API with the authorization token
    Then the response status code should be 200
    And the response body should contain the notes "data" array
    And the API response time should be less than 2000 milliseconds

  Scenario: Validate note deletion via DELETE notes ID (Scenario D)
    Given I log in via API with credentials from config
    And I have at least one note created via API using temp note config data
    When I retrieve the ID of the temp note from config
    And I delete the note with that retrieved ID via API
    Then the note deletion response status code should be 200
    And the temp note from config should be absent from the GET notes API list



  Scenario: Validate note count in GET notes increases by 1 after creating a new note (Scenario L-API)
    Given I log in via API with credentials from config
    And I record the current note count from the API
    When I create a note via API using temp note config data
    Then the note count from the API should have increased by 1

  Scenario: Validate POST notes response time is less than 3000 milliseconds (Scenario M-API)
    Given I log in via API with credentials from config
    When I create a note via API using temp note config data and record the response time
    Then the POST notes API response time should be less than 3000 milliseconds

  Scenario: Validate DELETE notes response time is less than 3000 milliseconds (Scenario N-API)
    Given I log in via API with credentials from config
    And I have at least one note created via API using temp note config data
    When I retrieve the ID of the temp note from config
    And I delete the note with that retrieved ID via API and record the response time
    Then the DELETE notes API response time should be less than 3000 milliseconds

  @negative
  Scenario: Validate DELETE notes returns 404 for a non-existent note ID (Scenario O-API)
    Given I log in via API with credentials from config
    When I attempt to delete a note with a non-existent ID via API
    Then the delete response status code should be 404

  @negative
  Scenario: Validate API returns 401 when using an invalid auth token (Scenario P-API)
    Given I send a GET notes request with an invalid auth token
    Then the API response status code should be 401
    And the API response body message should indicate unauthorized access


