#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Create Proposal Page
@CreateProposalRegressionScenariosEditCommission
Feature: Create Proposal Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @createProposalByUploadingAndDownloadingLegFile
  Scenario Outline: LEOS-4897,4517 [EC] Verify user is able to create the proposal successfully
    And  create proposal button is displayed and enabled
    When click on create proposal button
    When select template "<templateProposal>"
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When click on previous button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    And  cancel button is displayed and enabled
    When click on cancel button
    Then navigate to Repository Browser page
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    And  previous button is disabled
    When select template "<templateProposal>"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    And  previous button is enabled
    When provide document title "<oldProposalName>" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then title of the proposal/mandate contains "<oldProposalName>" keyword
    Then Proposal Viewer screen is displayed
    And  export button is displayed and enabled
    And  download button is displayed and enabled
    And  delete button is displayed and enabled
    And  close button is displayed and enabled
    And  cover page section is present
    And  explanatory memorandum section is present
    And  legal act section is present
    And  annexes section is present
    And  collaborators section is Present
    And  milestones section is present
    When click on download button
    And  sleep for 10000 milliseconds
    Then Proposal Viewer screen is displayed
    When find the recent "zip" file in download path and unzip it in "upload" and get the latest "leg" file
    When click on home button
    Then upload button is present
    When click on upload button present in the Repository Browser page
    Then upload window 'Upload a leg file 1/2' is showing
    When upload a latest "leg" file for creating proposal from location "upload"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on next button
    Then "Upload a legislative document - Document metadata (2/2)" is displayed
    When provide document title "<NewProposalName>" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "<NewProposalName>" keyword
    When click on close button
    Then navigate to Repository Browser page
    And  close the browser
    Examples:
      | templateProposal                                                                                    | oldProposalName              | NewProposalName              |
      | SJ-023 - Proposal for a Regulation of the European Parliament and of the Council                    | Automation Testing SJ-023 v1 | Automation Testing SJ-023 v2 |
      | SJ-024 - Proposal for a Directive of the European Parliament and of the Council                     | Automation Testing SJ-024 v1 | Automation Testing SJ-024 v2 |
      | SJ-025 - Proposal for a Decision of the European Parliament and of the Council                      | Automation Testing SJ-025 v1 | Automation Testing SJ-025 v2 |
      | SJ-026 - Proposal for a Decision of the European Parliament and of the Council (without addressees) | Automation Testing SJ-026 v1 | Automation Testing SJ-026 v2 |
      | SJ-019 - Proposal for a Council Decision                                                            | Automation Testing SJ-019 v1 | Automation Testing SJ-019 v2 |

