@ui
Feature: UI Scenarios
  As a capstone student, I want to validate login, note creation, deletion, and error handling via the UI.

  Scenario: Validate successful login via UI with credentials (Scenario A)
    Given I open the Notes App home page
    And I measure and log UI performance timings for "Home Page"
    When I enter email "ansh@gmail.com" on the UI
    And I enter password "12345678" on the UI
    And I click the Login button on the UI
    Then I should observe the dashboard page after successful submit
    And I measure and log UI performance timings for "Dashboard Page"

  Scenario: Validate note creation via UI and immediate appearance in list (Scenario B)
    Given I am successfully logged in to the UI dashboard with email "ansh@gmail.com" and password "12345678"
    When I click the "+ Add Note" button
    And I select Category "Work" on the form
    And I enter Title "Capstone Test Note" on the form
    And I enter Description "Automation testing note for capstone TC-UI-02" on the form
    And I click "Save" on the form
    Then I should observe the note "Capstone Test Note" immediately in the UI list

  @negative
  Scenario: Validate error message shown for invalid login credentials (Scenario G)
    Given I open the Notes App home page
    When I enter email "ansh@gmail.com" on the UI
    And I enter password "wrongpass99" on the UI
    And I click the Login button on the UI
    Then I should see the UI error message containing "Invalid" or "incorrect"


  Scenario: Validate note creation with Home category appears in the UI list (Scenario L)
    Given I am successfully logged in to the UI dashboard with email "ansh@gmail.com" and password "12345678"
    When I click the "+ Add Note" button
    And I select Category "Home" on the form
    And I enter Title "Home Task Note" on the form
    And I enter Description "A home category test note" on the form
    And I click "Save" on the form
    Then I should observe the note "Home Task Note" immediately in the UI list

  Scenario: Validate note description is visible on the UI note card (Scenario M)
    Given I am successfully logged in to the UI dashboard with email "ansh@gmail.com" and password "12345678"
    When I click the "+ Add Note" button
    And I select Category "Personal" on the form
    And I enter Title "Description Check Note" on the form
    And I enter Description "Visible description text" on the form
    And I click "Save" on the form
    Then the note "Description Check Note" should show description "Visible description text" in the UI card

  Scenario: Validate deleting a note via UI removes it from the dashboard (Scenario N)
    Given I am successfully logged in to the UI dashboard with email "ansh@gmail.com" and password "12345678"
    When I click the "+ Add Note" button
    And I select Category "Work" on the form
    And I enter Title "Note To Delete UI" on the form
    And I enter Description "This note will be deleted via UI" on the form
    And I click "Save" on the form
    And I delete the note "Note To Delete UI" via the UI dashboard
    Then the note "Note To Delete UI" should not be visible on the dashboard

