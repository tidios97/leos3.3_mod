#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Council Explanatory Page in CN instance of Edit Application
@CouncilExplanatoryActRegressionScenariosEditCouncil
Feature: Council Explanatory Page Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @basicCouncilExplanatoryFunctionalities
  Scenario: LEOS-5715 Addition and removal of 3 templates of council explanatory
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    And  "Council Explanatory" section is displayed
    And  there are below columns displayed under council explanatory section
      | TITLE           |
      | LANGUAGE        |
      | LAST UPDATED ON |
      | LAST UPDATED BY |
    And  add new explanatory button is displayed and enabled
    When click on add new explanatory button
    Then "Create new draft - Template selection (1/1)" window is displayed
    And  below 3 templates are displayed under council explanatories section in council explanatory template selection window
      | Explanatory              |
      | Coreper/Council note     |
      | Working Party cover page |
    When click on "Explanatory" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 1
    And  title of council explanatory 1 is "Council Explanatory"
    And  delete button is enabled for council explanatory 1
    When click on open button of "Council Explanatory" explanatory
    Then "Council Explanatory" council explanatory page is displayed
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | View                                   |
      | See navigation pane                    |
    When click on toc edit button
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Division     |
      | Crossheading |
      | Point 1.     |
      | Paragraph    |
      | Subparagraph |
      | Point (a)    |
      | Bullet       |
      | Indent       |
    When click on cancel button in navigation pane
    When click on annotation pop up button
    And  select "TEXT" from heading of division 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    And  enter "comment in Division 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment in Division 1" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    And  select "Text" from content of level 1
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "updated" in suggest box textarea
    And  click on "suggest" publish button
    Then "updated" is showing in the suggest text box
    When  switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on close button in Council Explanatory page
    Then OverView screen is displayed
    When click on add new explanatory button
    Then "Create new draft - Template selection (1/1)" window is displayed
    When click on "Coreper/Council note" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 2
    And  title of council explanatory 1 is "Coreper/Council note"
    And  delete button is enabled for council explanatory 1
    When click on open button of "Coreper/Council note" explanatory
    Then "Coreper/Council note" council explanatory page is displayed
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | View                                   |
      | See navigation pane                    |
    When click on toc edit button
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Division     |
      | Crossheading |
      | Point 1.     |
      | Paragraph    |
      | Subparagraph |
      | Point (a)    |
      | Bullet       |
      | Indent       |
    When click on cancel button in navigation pane
    When click on annotation pop up button
    And  select "INTRODUCTION" from heading of division 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    And  enter "comment in Division 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment in Division 1" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    And  select "national parliament" from content of level 7
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "np" in suggest box textarea
    And  click on "suggest" publish button
    Then "np" is showing in the suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on close button in Council Explanatory page
    Then OverView screen is displayed
    When click on add new explanatory button
    Then "Create new draft - Template selection (1/1)" window is displayed
    When click on "Working Party cover page" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 3
    And  title of council explanatory 1 is "Working Party cover page"
    And  delete button is enabled for council explanatory 1
    When click on open button of "Working Party cover page" explanatory
    Then "Working Party cover page" council explanatory page is displayed
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | View                                   |
      | See navigation pane                    |
    When click on toc edit button
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Division     |
      | Crossheading |
      | Point 1.     |
      | Paragraph    |
      | Subparagraph |
      | Point (a)    |
      | Bullet       |
      | Indent       |
    When click on cancel button in navigation pane
    When click on annotation pop up button
    And  select "Working Party" from content of paragraph 1
    And  click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    And  enter "comment in Paragraph 1" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "comment in Paragraph 1" is showing in the comment text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    And  select "previous version" from content of paragraph 2
    And  click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "pv" in suggest box textarea
    And  click on "suggest" publish button
    Then "pv" is showing in the suggest text box
    When switch from iframe to main window
    And  click on toggle bar move to right
    Then toggle bar moved to right
    When click on close button in Council Explanatory page
    Then OverView screen is displayed
    When click on delete button of council explanatory 3
    Then "Explanatory deletion : confirmation" pop up should be displayed with cancel and delete button enabled
    And  messages "Are sure you want to delete" and " the selected Explanatory and all its versions ?" are displayed in explanatory deletion : confirmation pop up window
    When click on delete button in Explanatory deletion : confirmation pop up
    Then number of council explanatory is 2
    And  title of council explanatory 1 is "Working Party cover page"
    And  title of council explanatory 2 is "Coreper/Council note"
    When click on delete button of council explanatory 2
    And  click on delete button in Explanatory deletion : confirmation pop up
    Then number of council explanatory is 1
    And  title of council explanatory 1 is "Working Party cover page"
    When click on delete button of council explanatory 1
    And  click on delete button in Explanatory deletion : confirmation pop up
    Then number of council explanatory is 0
    And  close the browser

  @exportToEConsiliumInCouncilExplanatory
  Scenario: LEOS-5715 Addition and removal of 3 templates of council explanatory
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    ######################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    When append " verify export to eConsilium functionality in Council Expalantories...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "verify export to eConsilium functionality in Council Expalantories...." keyword
    ######################################End Renaming Proposal Part####################################
    And  "Council Explanatory" section is displayed
    When click on add new explanatory button
    Then "Create new draft - Template selection (1/1)" window is displayed
    And  below 3 templates are displayed under council explanatories section in council explanatory template selection window
      | Explanatory              |
      | Coreper/Council note     |
      | Working Party cover page |
    When click on "Explanatory" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 1
    And  title of council explanatory 1 is "Council Explanatory"
    When click on add new explanatory button
    Then "Create new draft - Template selection (1/1)" window is displayed
    When click on "Coreper/Council note" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 2
    And  title of council explanatory 1 is "Coreper/Council note"
    And  title of council explanatory 2 is "Council Explanatory"
    When click on add new explanatory button
    Then "Create new draft - Template selection (1/1)" window is displayed
    When click on "Working Party cover page" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 3
    And  title of council explanatory 1 is "Working Party cover page"
    And  title of council explanatory 2 is "Coreper/Council note"
    And  title of council explanatory 3 is "Council Explanatory"
    And  delete button is enabled for council explanatory 1
    And  delete button is enabled for council explanatory 2
    And  delete button is enabled for council explanatory 3
    When click on open button of council explanatory 1
    Then "Working Party cover page" council explanatory page is displayed
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download clean version                 |
      | Export to eConsilium                   |
      | View                                   |
      | See navigation pane                    |
    When click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    And  print style option "DocuWrite" is disabled in Export to eConsilium window
    And  print style option "Internal" is disabled in Export to eConsilium window
    And  print style option "DocuWrite" is checked in Export to eConsilium window
    And  print style option "Internal" is not checked in Export to eConsilium window
    When provide title "test1" in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on actions hamburger icon
    And  click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When untick "All text" option in Export to eConsilium window
    Then "All text" option is unticked in Export to eConsilium window
    When provide title "test2" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on actions hamburger icon
    And  click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When provide title "test3" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on close button present in legal act page
    Then OverView screen is displayed
    Then "test3" is showing under title column row 1 in Export to eConsilium section
    And  today's date is showing under date column row 1 in Export to eConsilium section
    And  "exported" is showing under status column row 1 in Export to eConsilium section
    And  "test2" is showing under title column row 2 in Export to eConsilium section
    And  today's date is showing under date column row 2 in Export to eConsilium section
    And  "exported" is showing under status column row 2 in Export to eConsilium section
    And  "test1" is showing under title column row 3 in Export to eConsilium section
    And  today's date is showing under date column row 3 in Export to eConsilium section
    And  "exported" is showing under status column row 3 in Export to eConsilium section
    When click on open button of council explanatory 2
    Then "Coreper/Council note" council explanatory page is displayed
    When click on actions hamburger icon
    When click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    And  print style option "DocuWrite" is disabled in Export to eConsilium window
    And  print style option "Internal" is disabled in Export to eConsilium window
    And  print style option "DocuWrite" is checked in Export to eConsilium window
    And  print style option "Internal" is not checked in Export to eConsilium window
    When provide title "test4" in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on actions hamburger icon
    And  click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When untick "All text" option in Export to eConsilium window
    Then "All text" option is unticked in Export to eConsilium window
    When provide title "test5" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on actions hamburger icon
    And  click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When provide title "test6" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on close button present in legal act page
    Then OverView screen is displayed
    Then "test6" is showing under title column row 1 in Export to eConsilium section
    And  today's date is showing under date column row 1 in Export to eConsilium section
    And  "exported" is showing under status column row 1 in Export to eConsilium section
    And  "test5" is showing under title column row 2 in Export to eConsilium section
    And  today's date is showing under date column row 2 in Export to eConsilium section
    And  "exported" is showing under status column row 2 in Export to eConsilium section
    And  "test4" is showing under title column row 3 in Export to eConsilium section
    And  today's date is showing under date column row 3 in Export to eConsilium section
    And  "exported" is showing under status column row 3 in Export to eConsilium section
    When click on open button of council explanatory 3
    Then "Council Explanatory" council explanatory page is displayed
    When click on actions hamburger icon
    When click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    And  print style option "DocuWrite" is disabled in Export to eConsilium window
    And  print style option "Internal" is disabled in Export to eConsilium window
    And  print style option "DocuWrite" is checked in Export to eConsilium window
    And  print style option "Internal" is not checked in Export to eConsilium window
    When provide title "test7" in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on actions hamburger icon
    And  click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When untick "All text" option in Export to eConsilium window
    Then "All text" option is unticked in Export to eConsilium window
    When provide title "test8" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on actions hamburger icon
    And  click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When provide title "test9" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then "Export to eConsilium" message is displayed
    And  sleep for 1000 milliseconds
    When click on close button present in legal act page
    Then OverView screen is displayed
    Then "test9" is showing under title column row 1 in Export to eConsilium section
    And  today's date is showing under date column row 1 in Export to eConsilium section
    And  "exported" is showing under status column row 1 in Export to eConsilium section
    And  "test8" is showing under title column row 2 in Export to eConsilium section
    And  today's date is showing under date column row 2 in Export to eConsilium section
    And  "exported" is showing under status column row 2 in Export to eConsilium section
    And  "test7" is showing under title column row 3 in Export to eConsilium section
    And  today's date is showing under date column row 3 in Export to eConsilium section
    And  "exported" is showing under status column row 3 in Export to eConsilium section
    Then "test6" is showing under title column row 4 in Export to eConsilium section
    And  today's date is showing under date column row 4 in Export to eConsilium section
    And  "exported" is showing under status column row 4 in Export to eConsilium section
    And  "test5" is showing under title column row 5 in Export to eConsilium section
    And  today's date is showing under date column row 5 in Export to eConsilium section
    And  "exported" is showing under status column row 5 in Export to eConsilium section
    And  close the browser

  @updatingContentInLevelElement
  Scenario: LEOS-5669 [CN] error while updating content in level(Point 1.) in council explanatory
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    ######################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    When append " updating Content In Level Element In Council Explanatory" keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "updating Content In Level Element In Council Explanatory" keyword
    ######################################End Renaming Proposal Part####################################
    And  "Council Explanatory" section is displayed
    When click on add new explanatory button
    When click on "Explanatory" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 1
    And  title of council explanatory 1 is "Council Explanatory"
    When click on add new explanatory button
    When click on "Coreper/Council note" template under council explanatories section in council explanatory template selection window
    And  click on create button in council explanatory template selection window
    Then number of council explanatory is 2
    And  title of council explanatory 1 is "Coreper/Council note"
    And  title of council explanatory 2 is "Council Explanatory"
    When click on open button of council explanatory 1
    Then "Coreper/Council note" council explanatory page is displayed
    When click on "1. On …, the Commission submitted to the Counci..." link in navigation pane
    Then level 1 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Point 1." for element Type is disabled in selected element section
    And  input value "1." for element Number is disabled in selected element section
    And  input value "" for element Heading is editable in selected element section
    And  append text "Heading of Level" to the heading of the element in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  num "1." is present in level 1
    And  heading "Heading of Level" is present in level 1
    And  content "On …, the Commission submitted to the Council and the European Parliament a proposal for a Regulation of the European Parliament and of the Council …  , which aims at …" is present in level 1
    When double click on level 1
    Then ck editor window is displayed
    When update the heading of level to "Level Heading" in ck editor
    And  append " updated...." to the content of level in ck editor
    And  click on save close button of ck editor
    Then heading "Level Heading" is present in level 1
    And  content "On …, the Commission submitted to the Council and the European Parliament a proposal for a Regulation of the European Parliament and of the Council … 1 , which aims at … updated...." is present in level 1
    When click on close button in Council Explanatory page
    Then OverView screen is displayed
    When click on open button of council explanatory 2
    Then "Council Explanatory" council explanatory page is displayed
    When click on "1. Text..." link in navigation pane
    Then level 1 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Point 1." for element Type is disabled in selected element section
    And  input value "1." for element Number is disabled in selected element section
    And  input value "" for element Heading is editable in selected element section
    And  append text "Heading of Level" to the heading of the element in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  num "1." is present in level 1
    And  heading "Heading of Level" is present in level 1
    And  content "Text..." is present in level 1
    When double click on level 1
    Then ck editor window is displayed
    When update the heading of level to "Level Heading" in ck editor
    And  append " updated...." to the content of level in ck editor
    And  click on save close button of ck editor
    Then heading "Level Heading" is present in level 1
    And  content "Text... updated...." is present in level 1
    When click on insert after icon present in show all actions icon of level 1
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    When double click on level 2
    Then ck editor window is displayed
    When click enter button from keyboard
    And  append "Text1" to the subparagraph p tag 2 of level while ck editor is open
    And  click on increase indent icon present in ck editor panel
    And  click enter button from keyboard
    And  append "Text2" to point li tag 2 of ol tag 1 of level while ck editor is open
    And  click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then "Text..." is present in subparagraph 1 of level 2
    And  "Text1" is present in alinea 1 of point 1 of list 1 of level 2
    And  "Text2" is present in point 1 of list 1 of point 1 of list 1 of level 2
    When click on toc edit button
    And  click on "2. Text..." link in navigation pane
    Then selected element section is displayed
    And  input value "Point 1." for element Type is disabled in selected element section
    And  input value "2." for element Number is disabled in selected element section
    And  input value "" for element Heading is editable in selected element section
    And  append text "Level Heading" to the heading of the element in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    Then heading "Level Heading" is present in level 2
    And  num "2." is present in level 2
    Then "Text..." is present in subparagraph 1 of level 2
    And  "Text1" is present in alinea 1 of point 1 of list 1 of level 2
    And  "Text2" is present in point 1 of list 1 of point 1 of list 1 of level 2
    When double click on level 2
    Then ck editor window is displayed
    And  "Level Heading" is present in heading of level while ck editor is open
    And  "Text..." is present in subparagraph p tag 1 of level while ck editor is open
    And  "Text1" is present in alinea p tag 1 of point li tag 1 of ol tag 1 of level while ck editor is open
    And  "Text2" is present in point li tag 1 of ol tag 1 of point li tag 1 of ol tag 1 of level while ck editor is open
    When click on close button of ck editor
    Then ck editor window is not displayed
    ############## START LEOS-5909 [Regression][STANDALONE] in the standalone it is not possible to indent/outdent a level in Ckeditor #######################
    When click on "3. Text..." link in navigation pane
    Then level 3 is displayed
    When click on insert after icon present in show all actions icon of level 3
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    When double click on level 4
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    Then increase indent icon is disabled in ck editor
    When click on save close button of ck editor
    Then num "3.1." is present in level 4
    When click on insert after icon present in show all actions icon of level 4
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  num "3.2." is present in level 5
    When double click on level 5
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    Then increase indent icon is disabled in ck editor
    When click on save close button of ck editor
    Then num "3.1.1." is present in level 5
    When click on insert after icon present in show all actions icon of level 5
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  num "3.1.2." is present in level 6
    When double click on level 6
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    Then increase indent icon is disabled in ck editor
    When click on save close button of ck editor
    Then num "3.1.1.1." is present in level 6
    When click on insert after icon present in show all actions icon of level 6
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  num "3.1.1.2." is present in level 7
    When double click on level 7
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    Then increase indent icon is disabled in ck editor
    When click on save close button of ck editor
    Then num "3.1.1.1.1." is present in level 7
    When click on insert after icon present in show all actions icon of level 7
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  num "3.1.1.1.2." is present in level 8
    When double click on level 8
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    Then increase indent icon is disabled in ck editor
    When click on save close button of ck editor
    Then num "3.1.1.1.1.1." is present in level 8
    When click on insert after icon present in show all actions icon of level 8
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  num "3.1.1.1.1.2." is present in level 9
    When double click on level 9
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    Then increase indent icon is disabled in ck editor
    When click on save close button of ck editor
    Then num "3.1.1.1.1.1.1." is present in level 9
    When click on insert after icon present in show all actions icon of level 9
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  num "3.1.1.1.1.1.2." is present in level 10
    When double click on level 10
    Then ck editor window is displayed
    And  increase indent icon is disabled in ck editor
    When click on close button of ck editor
    Then ck editor window is not displayed
    ############## END LEOS-5909 [Regression][STANDALONE] in the standalone it is not possible to indent/outdent a level in Ckeditor #######################
    When click on close button in Council Explanatory page
    Then OverView screen is displayed
    And  close the browser

  @createCouncilExplanatoryFromCreateDraftButton
  Scenario Outline: LEOS-5891 create council explanatory document from create draft button
    When click on "Create draft" button
    Then "Create new draft - Template selection (1/2)" window is displayed
    When select option "<explanatoryTemplateName>" from Council Explanatories section in template selection window
    And  click on next button
    Then "Create new draft - Draft metadata (2/2)" window is displayed
    When provide draft title "Automation <explanatoryTemplateName>" in draft metadata page
    And  click on create button
    Then first proposal name contains "Council/GSC draft Automation <explanatoryTemplateName>"
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    And  council explanatory document stage is "Council/GSC"
    And  council explanatory document type is "draft"
    And  title of the proposal/mandate contains "Automation <explanatoryTemplateName>" keyword
    And  number of council explanatory is 1
    And  title of council explanatory 1 is "<explanatoryName>"
    When click on download button
    And  sleep for 5000 milliseconds
    And  click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    And  close the browser
    Examples:
      | explanatoryTemplateName  | explanatoryName          |
      | Explanatory              | Council Explanatory      |
      | Coreper/Council note     | Coreper/Council note     |
      | Working Party cover page | Working Party cover page |