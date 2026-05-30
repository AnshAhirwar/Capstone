@e2e
Feature: E2E Hybrid Integration Scenarios
  As a capstone student, I want to validate frontend-backend data parity, API-to-UI sync, and persistence.

  Scenario: Validate UI-created note data matches API GET notes response exactly (Scenario E)
    Given I open the Notes App home page
    And I login via UI with email "ansh@gmail.com" and password "12345678"
    When I create a new note with category "Personal", title "Budget Plan", and description "Monthly expense tracker"
    Then the UI note titled "Budget Plan" should display category "Personal", title "Budget Plan", and description "Monthly expense tracker"
    And I retrieve note details for "Budget Plan" via GET notes API
    Then the API note fields should exactly match the UI note fields

  Scenario: Validate note deleted via API disappears from UI list (Scenario F)
    Given I open the Notes App home page
    And I login via UI with email "ansh@gmail.com" and password "12345678"
    When I create a new note with category "Work", title "Meeting Notes", and description "Discussing project details"
    And I retrieve the ID of the note titled "Meeting Notes" via GET notes API
    And I execute DELETE note for that ID via API
    And I refresh the UI browser page
    Then the note "Meeting Notes" should be absent from the UI list

  Scenario: Validate UI-created note persists in the list after a full page refresh (Scenario Q)
    Given I open the Notes App home page
    And I login via UI with email "ansh@gmail.com" and password "12345678"
    When I create a new note with category "Home", title "Persist After Refresh", and description "Should survive a reload"
    And I refresh the UI browser page
    Then the note "Persist After Refresh" should be absent from the UI list is false

  Scenario: Validate two notes created via UI both appear in the API GET notes list (Scenario R)
    Given I open the Notes App home page
    And I login via UI with email "ansh@gmail.com" and password "12345678"
    When I create a new note with category "Work", title "E2E Note Alpha", and description "First E2E note"
    And I create a new note with category "Personal", title "E2E Note Beta", and description "Second E2E note"
    Then the API should contain a note with title "E2E Note Alpha"
    And the API should contain a note with title "E2E Note Beta"


  Scenario: Validate the UI note count decreases by 1 after deleting a note via API (Scenario T)
    Given I open the Notes App home page
    And I login via UI with email "ansh@gmail.com" and password "12345678"
    When I create a new note with category "Personal", title "Count Check Note", and description "Will be deleted via API"
    And I record the current UI note count
    And I retrieve the ID of the note titled "Count Check Note" via GET notes API
    And I execute DELETE note for that ID via API
    And I refresh the UI browser page
    Then the UI note count should have decreased by 1
