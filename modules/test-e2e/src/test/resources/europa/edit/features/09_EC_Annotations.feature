#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in annotations in EC instance of Edit Application
@annotationsScenariosEditCommission
Feature: Annotation Regression Features in Edit Commission

  @annotationsVisibilityForDifferentDGs
  Scenario: LEOS-4966 [EC] Test scenario- Annotations visibility for different DGs(DG and Presidency Users)
    Given navigate to "Commission" edit application
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When provide document title "Regression Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Annotation Testing" keyword in the title of the proposal/mandate
    And  click on title save button
    Then title of the proposal/mandate contains "Annotation Testing" keyword
    And  collaborators section is Present
    When click on add collaborator button
    Then collaborator save button is displayed
    And  collaborator cancel button is displayed
    And  search input box is enabled for name column in Collaborator section
    When search "Test2" in the name input field
    Then "Test2" user is showing in the list
    When click on first user showing in the list
    Then "Test2" user is selected in the name input field
    When click on save button in Collaborator section
    Then "Test2" user is showing in the collaborator list
    When click on add collaborator button
    When search "Test6" in the name input field
    Then "Test6" user is showing in the list
    When click on first user showing in the list
    Then "Test6" user is selected in the name input field
    When click on save button in Collaborator section
    Then "Test6" user is showing in the collaborator list
    When click on add collaborator button
    When search "Test7" in the name input field
    Then "Test7" user is showing in the list
    When click on first user showing in the list
    Then "Test7" user is selected in the name input field
    When click on save button in Collaborator section
    Then "Test7" user is showing in the collaborator list
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    When click on actions hamburger icon
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    When select option "DIRECTIVE" in Type field
    And  select option "2016" in Year field
    And  provide value "2102" in Nr. field
    And  click on search button in import office journal window
    Then bill content is appeared in import window
    When click on checkbox of recital 1
    When click on checkbox of recital 2
    When click on checkbox of recital 3
    When click on checkbox of recital 4
    When click on import button
    Then 4 recitals are added in bill content
    When click on preamble toggle link
    When click on recital link present in navigation pane
    When select content in recital 1
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to DG" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
      | DG            |
    When click on "DG" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to DG" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 2
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then selection pane is displayed
    And  "Click on annotation to select several or" label is displayed in selection pane
    And  "Actions on selected annotations" action label is displayed in selection pane
    And  below options are showing for click on annotation to select several or in selection pane
      | Select all   |
      | Deselect all |
    And  "Select all" option is selected by default in selection pane
    And  below options are showing for actions on selected annotations in selection pane
      | Accept                |
      | Reject                |
      | Delete                |
      | Mark as processed     |
      | Mark as not processed |
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to Collaborators" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to Collaborators" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 3
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to Only Me" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to Only Me" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 4
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoDG" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "DG" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoDG" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 5
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoCollaborators" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoCollaborators" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 6
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoOnlyMe" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoOnlyMe" is showing in the suggest text box
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.2.name" and password "user.nonsupport.2.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on preamble toggle link
    When click on recital link present in navigation pane
    When select content in recital 1
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Presidency" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
      | Presidency    |
    When click on "Presidency" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Presidency" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 2
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Collaborators" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Collaborators" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 3
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Only Me" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Only Me" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 4
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoPresidency" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Presidency" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoPresidency" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 5
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoCollaborators" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoCollaborators" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 6
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoOnlyMe" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoOnlyMe" is showing in the suggest text box
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.3.name" and password "user.nonsupport.3.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on toggle bar move to left
    Then toggle bar moved to left
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then below comments are showing in the comment text boxes
      | DG to DG            |
      | DG to Collaborators |
    Then below suggestions are showing in the suggestion text boxes
      | DGtoDG            |
      | DGtoCollaborators |
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.4.name" and password "user.nonsupport.4.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on toggle bar move to left
    Then toggle bar moved to left
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then below comments are showing in the comment text boxes
      | Presidency to Presidency    |
      | Presidency to Collaborators |
    Then below suggestions are showing in the suggestion text boxes
      | PresidencytoPresidency    |
      | PresidencytoCollaborators |
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @AnnotationNotVisibleAfterContributionDoneFromOtherDG
  Scenario: LEOS-5868 [EC] Fork & Merge: Annotations from SJ are sent to LeadDG after second contribution
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    And  create proposal button is displayed and enabled
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" window is displayed
    When provide document title "Automation Annotation Restriction Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "Automation Annotation Restriction Testing" keyword
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  "For Interservice Consultation" is showing in row 1 of title column of milestones table in commission instance
    And  today's date is showing in date column of milestones table
    And  "In preparation" is showing in status column of milestones table
    And  "For Interservice Consultation has been updated." message is displayed
    And  "File ready" is showing in status column of milestones table
    When click on actions hamburger icon of first milestone
    Then below options are displayed under milestone actions hamburger icon
      | View                     |
      | Send a copy for contribution |
    When click on send a copy for contribution option
    Then "Send a copy of the milestone for contribution" window is displayed
    When search "DAS Satyabrata" in the target user field
    Then user "DAS Satyabrata" is showing in the list
    When click on first user showing in the list
    Then "DAS Satyabrata" user is selected in the target user input field
    And  send for contribution button is displayed and enabled
    When click on send for contribution button
    Then "Copy sent for contribution" message is displayed
    And  click on message "Copy sent for contribution"
    Then "Sent for contribution to DAS Satyabrata" is showing under title column row 2 of milestones table
    And  today's date is showing under date column row 2 of milestones table
    And  "Sent for contribution" is showing under status column row 2 of milestones table
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    And  first proposal name contains "Automation Annotation Restriction Testing"
    And  colour of first proposal is "rgba(226, 226, 226, 1)"
    And  first proposal contains keyword Contribution status: Sent for contribution
    And  first proposal contains keyword CONTRIBUTION EdiT
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  proposal title has a label CONTRIBUTION EdiT in proposal viewer page
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Preamble" link in navigation pane
    Then recital 1 is displayed
    When select content "Recital" from recital 1
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment iteration 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment iteration 1" is showing in the comment text box
    And  switch from iframe to main window
    When click on "Article 1 - Scope 1. Text..." link in navigation pane
    Then article 1 is displayed
    When select "Text" from content of paragraph 1 of article 1
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggestion iteration 1" in suggest box textarea
    And  click on "suggest" publish button
    Then "suggestion iteration 1" is showing in the suggest text box
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on add button in milestones section
    Then create contribution milestone window is displayed
    And  "Suggestions will be excluded in the milestone for contribution. Ok to proceed?" message is showing in create contribution milestone window
    When click on create milestone button in create contribution milestone window
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Contribution from Legal Service" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | Contribution from Legal Service |
      | Other |
    When click on milestone dropdown icon
    And  click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  "Contribution from Legal Service has been created for this proposal." message is displayed
    And  click on message "Milestone creation has been requested"
    And  click on message "Contribution from Legal Service has been created for this proposal."
    And  "Contribution from Legal Service" is showing in row 1 of title column of milestones table in commission instance
    And  today's date is showing under date column row 1 of milestones table
    And  "In preparation" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of milestone 1
    Then "Send contribution" option is disabled in milestone actions hamburger icon
    And  "Contribution from Legal Service has been updated." message is displayed
    And  click on message "Contribution from Legal Service has been updated."
    And  "File ready" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of milestone 1
    Then below options are displayed under milestone actions hamburger icon
      | View              |
      | Send contribution |
    When click on option "Send contribution" in milestone actions hamburger icon
    Then "Contribution sent and notification sent to Lead DG" message is displayed
    And  click on message "Contribution sent and notification sent to Lead DG"
    And  "Contribution sent" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of milestone 1
    Then "Send contribution" option is disabled in milestone actions hamburger icon
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  "Ready to merge" is showing under status column row 2 of milestones table
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on toggle bar move to left
    Then toggle bar moved to left
    When switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    When click on the open button of proposal/mandate 2
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Preamble" link in navigation pane
    Then recital 2 is displayed
    When select content "Recital" from recital 2
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment iteration 2" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment iteration 2" is showing in the comment text box
    And  switch from iframe to main window
    When click on "Article 2 - Entry into force This Regulation sh..." link in navigation pane
    Then article 2 is displayed
    When select "enter into force" from content of paragraph 1 of article 2
    Then comment button is displayed
    And  highlight button is displayed
    And  suggest button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggestion iteration 2" in suggest box textarea
    And  click on "suggest" publish button
    Then "suggestion" is showing in the suggest text box
    And  "iteration" is showing in the suggest text box
    And  "2" is showing in the suggest text box
    When switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on add button in milestones section
    Then create contribution milestone window is displayed
    And  "Suggestions will be excluded in the milestone for contribution. Ok to proceed?" message is showing in create contribution milestone window
    When click on create milestone button in create contribution milestone window
    Then Add a milestone window is displayed
    And  click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  "Contribution from Legal Service has been created for this proposal." message is displayed
    And  click on message "Milestone creation has been requested"
    And  click on message "Contribution from Legal Service has been created for this proposal."
    And  "Contribution from Legal Service" is showing in row 1 of title column of milestones table in commission instance
    And  today's date is showing under date column row 1 of milestones table
    And  "In preparation" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of milestone 1
    Then "Send contribution" option is disabled in milestone actions hamburger icon
    And  "Contribution from Legal Service has been updated." message is displayed
    And  click on message "Contribution from Legal Service has been updated."
    And  "File ready" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of milestone 1
    Then below options are displayed under milestone actions hamburger icon
      | View              |
      | Send contribution |
    When click on option "Send contribution" in milestone actions hamburger icon
    Then "Contribution sent and notification sent to Lead DG" message is displayed
    And  click on message "Contribution sent and notification sent to Lead DG"
    And  "Contribution sent" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of milestone 1
    Then "Send contribution" option is disabled in milestone actions hamburger icon
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  "Ready to merge" is showing under status column row 2 of milestones table
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on toggle bar move to left
    Then toggle bar moved to left
    When switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
