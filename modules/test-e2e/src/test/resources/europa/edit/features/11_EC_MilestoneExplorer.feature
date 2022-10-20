#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Milestone Explorer in EC instance of Edit Application
@MileStoneExplorerRegressionScenariosEditCommission
Feature: MileStone Explorer Page Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @validateDifferentSectionInMileStoneExplorerPDF
  Scenario: LEOS-5537,5649 [EC] Create annexes in Proposal Viewer Page
    And  create proposal button is displayed and enabled
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-024 - Proposal for a Directive of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" window is displayed
    When provide document title " Automation Testing Validate PDF in MileStone Explorer" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on add a new annex button
    Then "Annex " is added to Annexes
    When click on add a new annex button
    Then "Annex II" is added to Annexes
    Then "Annex" is changed to "Annex I"
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "For Interservice Consultation" option is selected by default
    Then milestone title textbox is disabled
    When click on milestone option as Other
    And  type "Commission proposal" in title box
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  "Commission proposal" is showing in row 1 of title column of milestones table in commission instance
    And  today's date is showing in date column of milestones table
    And  "In preparation" is showing in status column of milestones table
    And  "Commission proposal has been updated." message is displayed
    And  "File ready" is showing in status column of milestones table
    When click on the hamburger menu showing in row 1 of milestones table
    Then below options are displayed under milestone actions hamburger icon
      | View                     |
      | Send a copy for contribution |
    When click on view option displayed under milestone actions hamburger icon
    Then milestone explorer page is displayed
    ######### START ###### LEOS-5984 [EC] Milestone explorer tabs order ###########
    And  "Cover Page [1.0.0]" is the tab 1 in milestone explorer window
    And  "Explanatory Memorandum [1.0.0]" is the tab 2 in milestone explorer window
    And  "Legal Act [1.0.0]" is the tab 3 in milestone explorer window
    And  "Annex 1 [1.0.0]" is the tab 4 in milestone explorer window
    And  "Annex 2 [1.0.0]" is the tab 5 in milestone explorer window
    ######### END ###### LEOS-5984 [EC] Milestone explorer tabs order ############
    When click on export button present in milestone explorer page
    And  sleep for 2000 milliseconds
    And  move the recent pdf file from download folder to location "pdf\EC"
    And  below words are present in the recent pdf file present in location "pdf\EC"
      | EXPLANATORY MEMORANDUM                                                                                         |
      | THE EUROPEAN PARLIAMENT AND THE COUNCIL OF THE EUROPEAN UNION,                                                 |
      | Having regard to the Treaty on the Functioning of the European Union, and in particular                        |
      | Having regard to the proposal from the European Commission,                                                    |
      | After transmission of the draft legislative act to the national Parliaments,                                   |
      | (1) Recital...                                                                                                 |
      | (2) Recital...                                                                                                 |
      | Article 1                                                                                                      |
      | Scope                                                                                                          |
      | Article 2                                                                                                      |
      | For the European Parliament                                                                                    |
      | For the Council                                                                                                |
      | The President                                                                                                  |
      | ANNEX I                                                                                                        |
      | ANNEX II                                                                                                       |
    When click on close button present in milestone explorer page
    Then Proposal Viewer screen is displayed
    And  close the browser





