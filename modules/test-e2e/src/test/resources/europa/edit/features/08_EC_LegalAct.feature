#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Legal Act Page in EC instance of Edit Application
@LegalActRegressionScenariosEditCommission
Feature: Legal Act Page Regression Features in Edit Commission

  @legalActScenario_Citation
  Scenario: LEOS-4146 EC Edition of elements - Browse Legal Act Citation Part
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
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
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version                  |
      | Download this version with annotations |
      | Import                                 |
      | Import from the Official Journal       |
      | View                                   |
      | See user guidance                      |
      | See navigation pane                    |
    When click on "See user guidance" option
    Then below sentences are present for user guidance
      | [where necessary]                                                                                                                                                                             |
      | [where necessary]                                                                                                                                                                             |
      | [Choose between the two options above. The second option must be used where the Regulation is binding and directly applicable only in certain Member States in accordance with the Treaties.] |
    When click on actions hamburger icon
    And  click on "See user guidance" option
    Then user guidance is not present in the page
    When click on preamble toggle link
    When click on citation link present in navigation pane
    And  double click on citation 1
    Then ck editor window is displayed
    And  get text from ck editor text box
    When add "New Text" and delete "Treaty " in the ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added to citation 1 in legal act
    And  "Treaty" is deleted from citation 1 in legal act
    When mouseHover and click on show all action button and click on edit button of citation 2
    Then ck editor window is displayed
    And  get text from ck editor text box
    When click on close button of ck editor
    Then ck editor window is not displayed
    When click on preamble text present in TOC
    When select content in citation 2
    Then suggest button is displayed
    Then highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "citation comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "citation comment" is showing in the comment text box
    And  switch from iframe to main window
    When select content in citation 3
    Then comment button is displayed
    Then highlight button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
#    And  "Comment" button is showing in suggest text box
    And  switch from iframe to main window
    When select content in citation 4
    Then comment button is displayed
    Then suggest button is displayed
    When click on highlight button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is displayed
    When mouse hover on highlight text box
    When click on edit button on highlight box
    Then highlight rich textarea is displayed
    When switch to "highlight" rich textarea iframe
    When enter "citation highlight" in highlight box rich textarea
    And  switch to parent frame
    And  click on "highlight" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "highlight" publish button
    Then "citation highlight" is showing in the highlight text box
    When mouse hover on comment text box
    When click on delete icon of comment text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment text box is not present
    When mouse hover on highlight text box
    When click on delete icon of highlight text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is not present
    When click on reject button of suggest text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest text box is not present
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    When click on the first preamble formula
    Then comment, suggest and highlight buttons are not displayed
    When select content on first preamble formula
    Then comment button is displayed
    Then suggest button is disabled
    Then highlight button is displayed
    When click on the first preamble formula
    Then comment, suggest and highlight buttons are not displayed
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @legalActScenario_Recital
  Scenario: LEOS-4146 [EC] Edition of elements - Browse Legal Act Recital Part
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
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
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on actions hamburger icon
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    When select option "DIRECTIVE" in Type field
    And  select option "2016" in Year field
    And  provide value "2102" in Nr. field
    And  click on search button in import office journal window
    And  sleep for 5000 milliseconds
    Then bill content is appeared in import window
    When click on checkbox of recital 1
    When click on checkbox of recital 2
    When click on checkbox of recital 3
    When click on import button
    Then 3 recitals are added in bill content
    When click on preamble toggle link
    When click on recital link present in navigation pane
    And  sleep for 1000 milliseconds
    And  double click on recital 3
    Then ck editor window is displayed
    And  get text from ck editor text box
    When add "New Text" and delete "society " in the ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added to recital 3 in legal act
    And  "society" is deleted from recital 3 in legal act
    When mouseHover and click on show all action button and click on edit button of recital 1
    Then ck editor window is displayed
    And  get text from ck editor text box
    When click on close button of ck editor
    Then ck editor window is not displayed
    When click on recital link present in navigation pane
    When select content in recital 1
    Then suggest button is displayed
    Then highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "recital comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "recital comment" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 2
    Then comment button is displayed
    Then suggest button is displayed
    When click on highlight button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is displayed
    When mouse hover on highlight text box
    When click on edit button on highlight box
    Then highlight rich textarea is displayed
    When switch to "highlight" rich textarea iframe
    When enter "recital highlight" in highlight box rich textarea
    And  switch to parent frame
    And  click on "highlight" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "highlight" publish button
    Then "recital highlight" is showing in the highlight text box
    And  switch from iframe to main window
    When select content in recital 4
    Then comment button is displayed
    Then highlight button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @legalActScenario_Article
  Scenario: LEOS-4146 [EC] Edition of elements - Browse Legal Act Article Part
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
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
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on toc edit button
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Citation     |
      | Recital      |
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Article      |
    When click on article 1 in navigation pane
    Then selected element section is displayed
    Then input value "Article" for element Type is disabled in selected element section
    Then input value "1" for element Number is disabled in selected element section
    Then input value "Scope" for element Heading is editable in selected element section
    Then delete button is displayed and enabled in selected element section
    When append text " New" to the heading of the element in selected element section
    And  click on save button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    Then heading of article 1 contains " New"
    When click on toc edit button
    Then cancel button in navigation pane is displayed and enabled
    When click on article 1 in navigation pane
    Then selected element section is displayed
    Then input value "Scope New" for element Heading is editable in selected element section
    When remove text " New" from the heading of the element in selected element section
    And  click on save button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    Then heading of article 1 doesn't contain " New"
    When click on toc edit button
    Then cancel button in navigation pane is displayed and enabled
    When click on article 1 in navigation pane
    Then selected element section is displayed
    Then input value "Article" for element Type is disabled in selected element section
    Then input value "1" for element Number is disabled in selected element section
    Then input value "Scope" for element Heading is editable in selected element section
    When click on cross symbol of the selected element
    Then selected element section is not displayed
    When click on "cancel" button present in navigation pane
    Then save button in navigation pane is not displayed
    Then save and close button in navigation pane is not displayed
    Then cancel button in navigation pane is not displayed
    Then elements section attached to navigation pane is not displayed
    When click on actions hamburger icon
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    When select option "DIRECTIVE" in Type field
    And  select option "2016" in Year field
    And  provide value "2102" in Nr. field
    And  click on search button in import office journal window
    And  sleep for 5000 milliseconds
    Then bill content is appeared in import window
    When click on checkbox of article 1
    When click on import button
    Then 1 articles are added in bill content
    When click on article 2 in navigation pane
    Then article 2 is displayed
    And  sleep for 2000 milliseconds
    And  double click on article 2
    Then ck editor window is displayed
    When add "New Text" and delete "force " in the ck editor of article list 1
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added to article 2 paragraph 1 in legal act
    And  "force" is deleted from article 2 paragraph 1 in legal act
    When click on article 1 in navigation pane
    When select content in article 1 paragraph 1
    Then suggest button is displayed
    Then highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "point comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "point comment" is showing in the comment text box
    And  switch from iframe to main window
    When click on article 2 in navigation pane
    Then article 2 is displayed
    And  sleep for 2000 milliseconds
    When select content in article 2 paragraph 1
    Then comment button is displayed
    Then highlight button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
#    Then "Collaborators" is showing in annotation header group name - cssSelector(".annotation-header__group .annotation-header__group-name")
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
#    And  "Comment" button is showing in suggest text box
    And  switch from iframe to main window
    When click on article 3 in navigation pane
    When select content in article 3 paragraph 1
    Then comment button is displayed
    Then suggest button is displayed
    When click on highlight button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is displayed
    When mouse hover on highlight text box
    When click on edit button on highlight box
    Then highlight rich textarea is displayed
    When switch to "highlight" rich textarea iframe
    When enter "point highlight" in highlight box rich textarea
    And  switch to parent frame
    And  click on "highlight" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "highlight" publish button
    Then "point highlight" is showing in the highlight text box
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @internalReference
  Scenario: LEOS-5684 [EC] test internal reference by uploading existing leg file
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on upload button present in the Repository Browser page
    Then upload window 'Upload a leg file 1/2' is showing
    When upload a leg file for creating proposal from location "commission/PROP_ACT_1383684831844402901.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on next button
    Then "Upload a legislative document - Document metadata (2/2)" is displayed
    When click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "Automation.....internal references....." keyword
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on preamble toggle link
    And  click on citation toggle link
    And  click on recital toggle link
    And  click on "Having regard to the proposal from the European..." link in navigation pane
    Then citation 2 is displayed
    When double click on citation 2
    Then ck editor window is displayed
    When click on internal reference icon present in ck editor panel
    Then internal reference window is displayed
    When click on "Article 3 - Subject matter and scope 1.In order..." link on the left side of internal reference window
    And  click on point 2 of paragraph 3 of article on the right side of internal reference window
    And  click on ok button of internal reference window
    And  click on save close button of ck editor
    Then "Article 3(3), point (b)" is added to internal reference 1 of citation 2
    When click on "(1) Recital..." link in navigation pane
    Then recital 1 is displayed
    When double click on recital 1
    Then ck editor window is displayed
    When click on internal reference icon present in ck editor panel
    Then internal reference window is displayed
    When click on "Article 11 - Monitoring and reporting 1.Member ..." link on the left side of internal reference window
    And  click on ok button of internal reference window
    And  click on save close button of ck editor
    Then "Article 11" is added to internal reference 1 of recital 1
    When click on "(2) Recital..." link in navigation pane
    Then recital 2 is displayed
    When double click on recital 2
    Then ck editor window is displayed
    When click on internal reference icon present in ck editor panel
    Then internal reference window is displayed
    When click on "Article 9 - Additional measures 1.Member States..." link on the left side of internal reference window
    And  click on point 2 of paragraph 1 of article on the right side of internal reference window
    And  click on ok button of internal reference window
    And  click on save close button of ck editor
    Then "Article 9(1), point (b)" is added to internal reference 1 of recital 2
    When click on "Article 4 - Definitions For the purposes of thi..." link in navigation pane
    Then article 4 is displayed
    When double click on article 4
    Then ck editor window is displayed
    When click on point 1 of ordered list in ck editor
    When click on internal reference icon present in ck editor panel
    Then internal reference window is displayed
    When click on "Having regard to the proposal from the European..." link on the left side of internal reference window
    And  click on ok button of internal reference window
    And  click on save close button of ck editor
    Then "second citation" is added to internal reference 1 of point 1 of paragraph 1 of article 4
    When click on "Article 4 - Definitions For the purposes of thi..." link in navigation pane
    Then article 4 is displayed
    When double click on article 4
    Then ck editor window is displayed
    When scroll to point 7 of ordered list in ck editor
    And  click on point 8 of ordered list in ck editor
    And  click on internal reference icon present in ck editor panel
    Then internal reference window is displayed
    When click on "(1) Recital...Article 11" link on the left side of internal reference window
    And  click on ok button of internal reference window
    And  click on save close button of ck editor
    Then "Recital (1)" is added to internal reference 1 of point 8 of paragraph 1 of article 4
    When click on "Article 8 - Article heading... 1.Text..." link in navigation pane
    Then article 8 is displayed
    When double click on article 8
    Then ck editor window is displayed
    When click on internal reference icon present in ck editor panel
    Then internal reference window is displayed
    When click on "Article 11 - Monitoring and reporting 1.Member ..." link on the left side of internal reference window
    And  click on paragraph 2 of article on the right side of internal reference window
    And  click on ok button of internal reference window
    And  click on save close button of ck editor
    Then "Article 11(2)" is added to internal reference 1 of paragraph 1 of article 8
    And  click on "Having regard to the proposal from the European..." link in navigation pane
    Then citation 2 is displayed
    When click on "(b)" of internal reference link 1 of citation 2
    Then point 2 of paragraph 3 of article 3 is displayed
    When click on "(1) Recital...Article 11" link in navigation pane
    Then recital 1 is displayed
    When click on "11" of internal reference link 1 of recital 1
    Then article 11 is displayed
    When click on "(2) Recital...Article 9(1), point (b)" link in navigation pane
    Then recital 2 is displayed
    When click on "(b)" of internal reference link 1 of recital 2
    Then point 2 of paragraph 1 of article 9 is displayed
    When click on "Article 4 - Definitions For the purposes of thi..." link in navigation pane
    Then article 4 is displayed
    When click on "second" of internal reference link 1 of point 1 of paragraph 1 of article 4
    Then citation 2 is displayed
    When click on "Article 4 - Definitions For the purposes of thi..." link in navigation pane
    Then article 4 is displayed
    When click on "(1)" of internal reference link 1 of point 8 of paragraph 1 of article 4
    Then recital 1 is displayed
    When click on "Article 8 - Article heading... 1.Text...Article..." link in navigation pane
    Then article 8 is displayed
    When click on "(2)" of internal reference link 1 of paragraph 1 of article 8
    Then paragraph 2 of article 11 is displayed
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser