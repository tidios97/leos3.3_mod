#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Cover Page in EC instance of Edit Application
@CoverPageRegressionScenariosEditCommission
Feature: Cover Page Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page

  @coverPage
  Scenario: LEOS-5845 [EC] Implement cover page section on proposal viewer
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    And  click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When provide document title "Cover Page" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then title of the proposal/mandate contains "Cover Page" keyword
    Then Proposal Viewer screen is displayed
    And  cover page section is present

    When click on title of the mandate
    When replace "Automation Testing Cover Page" keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Automation Testing Cover Page" keyword
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  document title contains "Automation Testing Cover Page" in legal act page
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  search button is displayed in versions pane section
    And  "Recent changes" is displayed







    When click on open button of cover page
    Then cover page is displayed
    And  annotation side bar is present
    And  only "Title" element is present in TOC
    When click on "Title" link in navigation pane
    Then cover page long title is "Automation Testing Cover Page"
    When select "Brussels" from cover page container main doc location
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is disabled
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "long title comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "long title comment" is showing in the comment text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When select "Proposal" from cover page long title doc stage
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is disabled
    When select "EUROPEAN PARLIAMENT" from cover page long title doc type
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is disabled
    When select "Automation Testing" from cover page long title doc purpose
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "Testing Automation" in suggest box textarea
    And  click on "suggest" publish button
    Then "Testing Automation" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    And  click on selected suggested portion in cover page long title doc purpose
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then at least one suggestion box is selected
    When click on accept button present in selected suggest box
    Then suggestion successfully merged is displayed
    And  wait for disappearance of the message suggestion successfully merged
    When switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    And  cover page long title is "Testing Automation Cover Page"
#    When mouse hover and click on edit icon in long title of cover page
    When double click on long title of doc purpose
    Then ck editor window is displayed
    And  13 plugins are available in ck editor window
    And  save button is disabled in ck editor
    And  save close button is disabled in ck editor
    And  close button is enabled in ck editor
    And  cut button is disabled in ck editor
    And  copy button is disabled in ck editor
    And  paste button is enabled in ck editor
    And  undo button is disabled in ck editor
    And  redo button is disabled in ck editor
    And  subscript button is enabled in ck editor
    And  superscript button is enabled in ck editor
    And  special character button is enabled in ck editor
    And  show blocks button is enabled in ck editor
    And  source button is enabled in ck editor
    When replace content "Automation Cover Page Testing" with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then cover page long title is "Automation Cover Page Testing"
    When click on close button present in cover page
    Then Proposal Viewer screen is displayed
    And  title of the proposal/mandate contains "Automation Cover Page Testing" keyword
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  document title contains "Automation Cover Page Testing" in legal act page
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    And  click on milestone option as Other
    And  type "cover page testing" in title box
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  "cover page testing" is showing in row 1 of title column of milestones table in commission instance
    And  today's date is showing in date column of milestones table
    And  "In preparation" is showing in status column of milestones table
    And  "cover page testing has been updated." message is displayed
    And  click on message "cover page testing has been updated."
    And  "File ready" is showing in status column of milestones table
    When click on the hamburger menu showing in row 1 of milestones table
    And  click on view option displayed under milestone actions hamburger icon
    Then milestone explorer page is displayed
    And  cover page section is displayed in milestone explorer page
    And  explanatory memorandum section is displayed
    And  legal act section is displayed in milestone explorer page
    When click on close button present in milestone explorer page
    Then Proposal Viewer screen is displayed
    And  close the browser