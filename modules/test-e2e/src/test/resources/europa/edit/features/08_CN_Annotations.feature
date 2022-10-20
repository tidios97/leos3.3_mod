#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in annotations in CN instance of Edit Application
@annotationsScenariosEditCouncil
Feature: Annotation Regression Features in Edit Council

  @annotationsVisibilityForDifferentDGs
  Scenario: LEOS-4966 [CN] Test scenario- Annotations visibility for different DGs(DG and Presidency Users)
    Given navigate to "Council" edit application
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
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
    When click on preamble toggle link
    And  click on recital toggle link
    And  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When select content in recital 1
    And  click on annotation pop up button
    And  click on comment button
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
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(10) It is necessary that national regulatory a..." link in navigation pane
    Then recital 10 is displayed
    When select content in recital 10
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to Collaborators" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to Collaborators" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(3) The market for cross-border parcel delivery..." link in navigation pane
    Then recital 3 is displayed
    When select content in recital 3
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to Only Me" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to Only Me" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(4) In order to improve the affordability of cr..." link in navigation pane
    Then recital 4 is displayed
    When select content in recital 4
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoDG" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "DG" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoDG" is showing in the suggest text box
    When  switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(5) In most Member States there are several pro..." link in navigation pane
    Then recital 5 is displayed
    When select content in recital 5
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoCollaborators" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoCollaborators" is showing in the suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(7) An estimated 80 % of addressed postal items..." link in navigation pane
    Then recital 7 is displayed
    When select content in recital 7
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoOnlyMe" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoOnlyMe" is showing in the suggest text box
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then OverView screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.2.name" and password "user.nonsupport.2.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on preamble toggle link
    And  click on recital toggle link
    When click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When select content in recital 1
    And  click on annotation pop up button
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
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(10) It is necessary that national regulatory a..." link in navigation pane
    Then recital 10 is displayed
    When select content in recital 10
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Collaborators" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Collaborators" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(3) The market for cross-border parcel delivery..." link in navigation pane
    Then recital 3 is displayed
    When select content in recital 3
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Only Me" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Only Me" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(4) In order to improve the affordability of cr..." link in navigation pane
    Then recital 4 is displayed
    When select content in recital 4
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoPresidency" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Presidency" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoPresidency" is showing in the suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(5) In most Member States there are several pro..." link in navigation pane
    Then recital 5 is displayed
    When select content in recital 5
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoCollaborators" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoCollaborators" is showing in the suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(7) An estimated 80 % of addressed postal items..." link in navigation pane
    Then recital 7 is displayed
    When select content in recital 7
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoOnlyMe" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoOnlyMe" is showing in the suggest text box
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then OverView screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.3.name" and password "user.nonsupport.3.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
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
    Then OverView screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.4.name" and password "user.nonsupport.4.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
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
    Then OverView screen is displayed
    And  close the browser

  @annotationScenarioCouncil1
  Scenario: different annotation scenarios in recitals
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " annotation changes" keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "annotation changes" keyword
    When click on open button of legal act
    Then navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    And  click on recital toggle link
    When click on "(22) In order to ensure uniform conditions for ..." link in navigation pane
    Then recital 22 is displayed
    When select content in recital 22
    Then comment button is not displayed
    And  highlight button is not displayed
    And  suggest button is not displayed
    When click on annotation pop up button
    Then comment button is displayed
    And  suggest button is displayed
    And  highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "This is very important" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "This is very important" is showing in the comment text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(23) This Regulation respects the fundamental r..." link in navigation pane
    Then recital 23 is displayed
    When select content "Charter of Fundamental Rights" from recital 23
    Then suggest button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "EU" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "EU" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    And  click on selected suggested portion in recital 23
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then at least one suggestion box is selected
    When click on accept button present in selected suggest box
    Then suggestion successfully merged is displayed
    And  wait for disappearance of the message suggestion successfully merged
    When switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    Then "EU" is showing as bold in recital 23
    When click on "(22) In order to ensure uniform conditions for ..." link in navigation pane
    Then recital 22 is displayed
    And  sleep for 2000 milliseconds
    When click on position 1 commented portion in recital 22
    And  sleep for 2000 milliseconds
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then "This is very important" is showing in the selected comment box
    When click on edit icon of selected comment box
    And  switch to "comment" rich textarea iframe
    Then rich text area editor is displayed in selected comment box
    When replace content "This is not very important" with existing content in rich text area of selected comment box
    Then "This is very important" is not present in rich text area of selected comment box
    When switch to parent frame
    And  click on cancel button present in selected comment box
    Then cancel button is not present in selected comment box
    When switch from iframe to main window
    And  click on toggle bar move to right
    And  click on selected commented portion in recital 22
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then no comment box is selected
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Preamble" link in navigation pane
    And  click on preamble toggle link
    Then recitals section is not displayed
    When click on "Article 1 - Scope" link in navigation pane
    Then article 1 is displayed
    When select content in subparagraph 1 of paragraph 1 of article 1 in legal act page
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for article 1 paragraph 1 subparagraph 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "comment for article 1 paragraph 1 subparagraph 1" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    And  click on selected commented portion in subparagraph 1 of paragraph 1 of article 1 in legal act page
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then at least one comment box is selected
    When mouse hover on selected comment text box
    And  click on reply button in selected comment box
    Then add reply button is displayed and enabled in selected comment box
    When switch to "comment" rich textarea iframe
    And  enter "reply to comment for article 1 paragraph 1 subparagraph 1" in comment box rich textarea
    And  switch to parent frame
#    And  click on "comment" annotation sharing setting
#    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on post button
    Then hide reply button is displayed and enabled in selected comment box
    And  "reply to comment for article 1 paragraph 1 subparagraph 1" is showing in the reply list of selected comment box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Article 2 - Definitions" link in navigation pane
    Then article 2 is displayed
    When select content in point 1 of list 1 of paragraph 2 of article 2 in legal act page
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for article 2 paragraph 2 point 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "comment for article 2 paragraph 2 point 1" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    And  click on selected commented portion in point 1 of paragraph 2 of article 2 in legal act page
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then at least one comment box is selected
    When mouse hover on selected comment text box
    And  click on reply button in selected comment box
    Then add reply button is displayed and enabled in selected comment box
    When switch to "comment" rich textarea iframe
    And  enter "reply to comment for article 2 paragraph 2 point 1" in comment box rich textarea
    And  switch to parent frame
#    And  click on "comment" annotation sharing setting
#    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on post button
    Then "reply to comment for article 2 paragraph 2 point 1" is showing in the reply list of selected comment box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Article 3 - Provision of information" link in navigation pane
    Then article 3 is displayed
    When select content in subparagraph 1 of paragraph 1 of article 3 in legal act page
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    And  add justification button is displayed and enabled in selected suggestion box
    When enter "suggestion for article 3 paragraph 1 subparagraph 1" in suggest box textarea
    And  click on add justification link of selected suggestion box
    And  switch to "suggest" rich textarea iframe
    And  enter "justification on suggestion of article 3 paragraph 1 subparagraph 1" in justification box of selected suggest box
    And  switch to parent frame
    And  click on "suggest" annotation sharing setting
    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    And  switch from iframe to main window
    And  click on toggle bar move to right
    And  click on selected commented portion in subparagraph 1 of paragraph 1 of article 3 in legal act page
    And  switch from main window to iframe "hyp_sidebar_frame"
    And  click on show justification link in selected suggestion box
    Then "justification on suggestion of article 3 paragraph 1 subparagraph 1" is showing in justification list of selected suggest box
    When click on accept button present in selected suggest box
    And  switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    And  "suggestion for article 3 paragraph 1 subparagraph 1hall" is showing as bold in subparagraph 1 of paragraph 1 of article 3 in legal act page
    When click on "3. By 31 March of each calendar year, all parce..." link in navigation pane
    Then subparagraph 1 of paragraph 3 of article 3 is displayed
    When select content in subparagraph 1 of paragraph 3 of article 3 in legal act page
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    And  add justification button is displayed and enabled in selected suggestion box
    When enter "suggestion for article 3 paragraph 3 subparagraph 1" in suggest box textarea
    And  click on add justification link of selected suggestion box
    And  switch to "suggest" rich textarea iframe
    And  enter "justification on suggestion of article 3 paragraph 3 subparagraph 1" in justification box of selected suggest box
    And  switch to parent frame
    And  click on "suggest" annotation sharing setting
    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    And  switch from iframe to main window
    And  click on toggle bar move to right
    And  click on position 1 commented portion in subparagraph 1 of paragraph 3 of article 3 in legal act page
    And  switch from main window to iframe "hyp_sidebar_frame"
    And  click on show justification link in selected suggestion box
    Then "justification on suggestion of article 3 paragraph 3 subparagraph 1" is showing in justification list of selected suggest box
    When click on reject button of selected suggest text box
    And  click on ok button present in windows alert pop up
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    And  no suggestion is present for subparagraph 1 of paragraph 3 of article 3 in legal act page
    When click on "4. The Commission shall, by means of an impleme..." link in navigation pane
    Then paragraph 4 of article 3 is displayed
    When select content in paragraph 4 of article 3 in legal act page
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for article 3 paragraph 4" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "comment for article 3 paragraph 4" is showing in the comment text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Article 2 - Definitions" link in navigation pane
    Then article 2 is displayed
    When click on position 1 commented portion in point 1 of paragraph 2 of article 2 in legal act page
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then "comment for article 2 paragraph 2 point 1" is showing in the selected comment box
    When mouse hover on selected comment text box
    And  click on reply button in selected comment box
    Then add reply button is displayed and enabled in selected comment box
    When switch to "comment" rich textarea iframe
    And  enter "second reply for article 2 paragraph 2 point 1" in comment box rich textarea
    And  switch to parent frame
#    And  click on "comment" annotation sharing setting
#    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on post button
    Then "second reply for article 2 paragraph 2 point 1" is showing in the reply list of selected comment box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Article 5 - Assessing affordability of tariffs" link in navigation pane
    Then article 5 is displayed
    When select content "affordability" from heading of article 5
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for article 5 heading" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "comment for article 5 heading" is showing in the comment text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Preamble" link in navigation pane
    And  click on preamble toggle link
    Then recitals section is displayed
    When click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When select content in recital 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for recital 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And   click on "comment" publish button
    Then "comment for recital 1" is showing in the comment text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on "(6) Currently, postal services are regulated by..." link in navigation pane
    Then recital 6 is displayed
    When click on insert before icon present in show all actions icon of recital 6
    Then recital "(5a)" is added to the bill in legal act page
    And  content of recital "(5a)" is showing bold in legal act live page
    When click on "(5a) Recital..." link in navigation pane
    Then recital 6 is displayed
    When select content in recital 6
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for recital 5a" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And   click on "comment" publish button
    Then "comment for recital 5a" is showing in the comment text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When select content in recital 6
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggestion for recital 5a" in suggest box textarea
    And  click on add justification link of selected suggestion box
    And  switch to "suggest" rich textarea iframe
    And  enter "justification on suggestion for recital 5a" in justification box of selected suggest box
    And  switch to parent frame
    And  click on "suggest" annotation sharing setting
    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    And  switch from iframe to main window
    And  click on toggle bar move to right
    And  click on selected suggested portion in recital 6
    And  switch from main window to iframe "hyp_sidebar_frame"
    When click on show justification link in selected suggestion box
    And  "justification on suggestion for recital 5a" is showing in justification list of selected suggest box
    When click on accept button present in selected suggest box
    And  click on sort by button
    Then below options are showing in sort by list
      | Newest |
      | Oldest |
      | Location |
    And  "Location" option is default selection in sort by list
    When click on option "Oldest" in sort by list
#    Then all the annotations are showing in ascending order
    And  switch from iframe to main window
#    And  click on toggle bar move to right
#    Then toggle bar moved to right
    And  "suggestion for recital 5a" is showing as bold in recital 6
    When click on close button present in legal act page
    Then OverView screen is displayed
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Meeting of the Council" option is selected by default
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  "Meeting of the Council" is showing in row 1 of title column of milestones table in council instance
    And  today's date is showing under date column row 1 of milestones table
    And  "File ready" is showing under status column row 1 of milestones table
    When click on the link present in the row 1 of title column in milestones table
    Then milestone explorer page is displayed
    And  explanatory memorandum section is displayed
    And  legal act section is displayed in milestone explorer page
    When click on legal act section in milestone explorer page
    Then citations section is displayed in milestone explorer page
    And  recitals section is displayed in milestone explorer page
    And  annotations section is opened in milestone explorer page
    When switch from main window to iframe "hyp_sidebar_frame"
    Then there are 6 annotations present in annotations window in milestone explorer page
    When click on Orphans link present in annotations window in milestone explorer page
    Then there are 1 annotations present in orphans window in milestone explorer page
    And  switch from iframe to main window
    When click on close button present in milestone explorer page
    Then OverView screen is displayed
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Meeting of the Council" option is selected by default
    When click on milestone option as Other
    And  type "No comment" in title box
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  "No comment" is showing in row 1 of title column of milestones table in council instance
    And  today's date is showing under date column row 1 of milestones table
    And  "File ready" is showing under status column row 1 of milestones table
    And  close the browser

  @MarkAsProcessed
  Scenario: LEOS-5973 [CN][Annotation][DIALOGIKA] - Only Author role can mark Comments and Suggestions as processed
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    #####################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Support Role Mark as Processed" keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Support Role Mark as Processed" keyword
    #####################################End Renaming Proposal Part####################################
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Preamble" link in navigation pane
    When click on preamble toggle link
    Then recitals section is displayed
    When click on recital toggle link
    And  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When click on annotation pop up button
    And  select content "tariffs applicable" from recital 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment" is showing in the comment text box
    And  mark as processed text is present in the footer of comment 1
    When click on the checkbox of mark as processed element in comment 1
    And  click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    And  select content in recital 1
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  mark as processed text is present in the footer of suggestion 1
    When click on the checkbox of mark as processed element in suggestion 1
    And  click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on close button present in legal act page
    Then OverView screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.support.2.name" and password "user.support.2.pwd"
    Then navigate to Repository Browser page
    When search keyword "Support Role Mark as Processed" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Support Role Mark as Processed"
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Preamble" link in navigation pane
    When click on preamble toggle link
    Then recitals section is displayed
    When click on recital toggle link
    And  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When click on annotation pop up button
    And  select content in recital 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment" is showing in the comment text box
    And  mark as processed text is present in the footer of comment 1
    When click on the checkbox of mark as processed element in comment 1
    And  click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    And  select content in recital 1
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  mark as processed text is present in the footer of suggestion 1
    When click on the checkbox of mark as processed element in suggestion 1
    And  click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then OverView screen is displayed
    When click on add collaborator button
    Then collaborator save button is displayed
    And  collaborator cancel button is displayed
    And  search input box is enabled for name column in Collaborator section
    When search "Test1" in the name input field
    Then "Test1" user is showing in the list
    When click on first user showing in the list
    Then "Test1" user is selected in the name input field
    When click on down arrow button present for the role input field
    Then below roles are shown in role dropdown
      | Author      |
      | Contributor |
      | Reviewer    |
    When click on "Contributor" from role dropdown list
    Then "Contributor" role is selected in the role input field
    When click on save button in Collaborator section
    Then "Test1" user is showing in row 2 of the collaborator list
    And  "Contributor" role is showing in row 2 of the collaborator list
    When click on add collaborator button
    When search "Test2" in the name input field
    Then "Test2" user is showing in the list
    When click on first user showing in the list
    Then "Test2" user is selected in the name input field
    When click on down arrow button present for the role input field
    Then below roles are shown in role dropdown
      | Author      |
      | Contributor |
      | Reviewer    |
    When click on "Reviewer" from role dropdown list
    Then "Reviewer" role is selected in the role input field
    When click on save button in Collaborator section
    Then "Test2" user is showing in row 3 of the collaborator list
    And  "Reviewer" role is showing in row 3 of the collaborator list
    When click on add collaborator button
    When search "Test6" in the name input field
    Then "Test6" user is showing in the list
    When click on first user showing in the list
    Then "Test6" user is selected in the name input field
    And  "Author" role is selected in the role input field
    When click on save button in Collaborator section
    Then "Test6" user is showing in row 4 of the collaborator list
    And  "Author" role is showing in row 4 of the collaborator list
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    When search keyword "Support Role Mark as Processed" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Support Role Mark as Processed"
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Preamble" link in navigation pane
    When click on preamble toggle link
    Then recitals section is displayed
    When click on recital toggle link
    And  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When click on annotation pop up button
    And  select content in recital 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment from contributor" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment from contributor" is showing in the comment text box
    And  mark as processed text is not present in comment action footer
    And  switch from iframe to main window
    When  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    And  select content in recital 1
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  mark as processed text is not present in suggestion action footer
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then OverView screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.2.name" and password "user.nonsupport.2.pwd"
    Then navigate to Repository Browser page
    When search keyword "Support Role Mark as Processed" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Support Role Mark as Processed"
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Preamble" link in navigation pane
    When click on preamble toggle link
    Then recitals section is displayed
    When click on recital toggle link
    And  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When click on annotation pop up button
    And  select content in recital 3
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment from reviewer" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment from reviewer" is showing in the comment text box
    And  mark as processed text is not present in comment action footer
    And  switch from iframe to main window
    When  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    And  select content in recital 3
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  mark as processed text is not present in suggestion action footer
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then OverView screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Council" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.3.name" and password "user.nonsupport.3.pwd"
    Then navigate to Repository Browser page
    When search keyword "Support Role Mark as Processed" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Support Role Mark as Processed"
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When switch from main window to iframe "hyp_sidebar_frame"
    Then total annotation count is 4 in annotation module
    When click on the checkbox of mark as processed element in comment 1
    And  click on ok button present in windows alert pop up
    When switch from main window to iframe "hyp_sidebar_frame"
    Then total annotation count is 3 in annotation module
    When click on the checkbox of mark as processed element in comment 1
    And  click on ok button present in windows alert pop up
    When switch from main window to iframe "hyp_sidebar_frame"
    Then total annotation count is 2 in annotation module
    When click on the checkbox of mark as processed element in suggestion 1
    And  click on ok button present in windows alert pop up
    When switch from main window to iframe "hyp_sidebar_frame"
    Then total annotation count is 1 in annotation module
    When click on the checkbox of mark as processed element in suggestion 1
    And  click on ok button present in windows alert pop up
    When switch from main window to iframe "hyp_sidebar_frame"
    Then there is no annotations in this group
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser