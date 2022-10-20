#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Annexes Page in CN instance of Edit Application
@AnnexRegressionScenariosEditCouncil
Feature: Annex Page Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @create_delete_Annex
  Scenario: LEOS-5537,5649 [CN] Create annexes in Proposal Viewer Page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_2649624126129902267.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    And  explanatory memorandum section is present
    And  legal act section is present
    And  annexes section is present
    #####################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Annex Numbering....." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Annex Numbering....." keyword
    #####################################End Renaming Proposal Part####################################
    When click on add a new annex button
    Then "Annex " is added to Annexes
    And  numbers of annex present in proposal viewer screen is 1
    When click on open button of Annex 1
    Then Annex page is displayed
    And  annex title is "ANNEX"
    When click on "Body" link in navigation pane
    Then block name of the annex container is "ANNEX"
    When click on close button present in annex page
    Then OverView screen is displayed
    When click on add a new annex button
    Then "Annex II" is added to Annexes
    Then "Annex" is changed to "Annex I"
    Then numbers of annex present in proposal viewer screen is 2
    When click on open button of Annex 1
    Then Annex page is displayed
    And  annex title is "ANNEX I"
    When click on "Body" link in navigation pane
    Then block name of the annex container is "ANNEX I"
    When click on close button present in annex page
    Then OverView screen is displayed
    When click on open button of Annex 2
    Then Annex page is displayed
    And  annex title is "ANNEX II"
    When click on "Body" link in navigation pane
    Then block name of the annex container is "ANNEX II"
    When click on close button present in annex page
    Then OverView screen is displayed
    When click on add a new annex button
    Then "Annex III" is added to Annexes
    Then numbers of annex present in proposal viewer screen is 3
    When click on title of the Annex 1
    Then title save button of Annex 1 is displayed and enabled
    And  title cancel button of Annex 1 is displayed and enabled
    When add title "Annex 1 Title" to Annex 1
    And  click on title save button of Annex 1
    Then "Annex metadata updated" message is displayed
    And  click on message "Annex metadata updated"
    And  title of Annex 1 contains "Annex 1 Title"
    When click on title of the Annex 2
    And  add title "Annex 2 Title" to Annex 2
    And  click on title save button of Annex 2
    Then "Annex metadata updated" message is displayed
    And  click on message "Annex metadata updated"
    And  title of Annex 2 contains "Annex 2 Title"
    When click on title of the Annex 3
    And  add title "Annex 3 Title" to Annex 3
    And  click on title save button of Annex 3
    Then "Annex metadata updated" message is displayed
    And  click on message "Annex metadata updated"
    And  title of Annex 3 contains "Annex 3 Title"
    When click on open button of Annex 1
    Then Annex page is displayed
    And  navigation pane is displayed
    And  toc editing button is available
    And  preface and body is present in annex navigation pane
    And  3 level is present in the body of annex page
    When click on toc edit button
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Point 1.     |
      | Paragraph    |
      | Subparagraph |
      | Point (a)    |
    When click on cancel button in navigation pane
    Then elements menu lists are not displayed
    When click on element 1 in annex
    When click on insert before icon present in show all actions icon of level 1
    Then "Point inserted" message is displayed
    And  click on message "Point inserted"
    And  total number of level is 4
    When click on element 2 in annex
    When click on insert after icon present in show all actions icon of level 2
    Then "Point inserted" message is displayed
    And  click on message "Point inserted"
    And  total number of level is 5
    When click on element 1 in annex
    When click on edit icon present in show all actions icon of level 1
    Then ck editor window is displayed
    When append " New Text" at the end of the content of level
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added to content of level 1
    When click on element 1 in annex
    When double click on level 1
    Then ck editor window is displayed
    When remove " New Text" from the content of level
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is removed from content of level 1
    When click on element 3 in annex
    When click on delete icon present in show all actions icon of level 3
    Then "Point deleted" message is displayed
    And  click on message "Point deleted"
    And  total number of level is 4
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
    When click on close button present in annex page
    Then OverView screen is displayed
    When click on delete button of annex 3
    Then "Annex deletion: confirmation" window is displayed
    When click on delete button in annex deletion confirmation page
    Then "Annex has been deleted" message is displayed
    And  click on message "Annex has been deleted"
    Then numbers of annex present in proposal viewer screen is 2
    And  close the browser

  @SingleAndDoubleDiffingAnnex
  Scenario: LEOS-XXXX [CN] different operations with annex elements plus diffing functionalities
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
    And  explanatory memorandum section is present
    And  legal act section is present
    And  annexes section is present
    #####################################Start MileStone Part############################################
    And  no milestone exists in milestones section
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Meeting of the Council" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | Meeting of the Council |
      | Other                  |
    When click on milestone option as Other
    And  type "Commission proposal" in title box
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  click on message "Commission proposal has been created for this proposal."
    And  "Commission proposal" is showing in row 1 of title column of milestones table in council instance
    And  today's date is showing in date column of milestones table
    And  "File ready" is showing in status column of milestones table
    When click on the link present in the row 1 of title column in milestones table
    Then milestone explorer page is displayed
    And  explanatory memorandum section is displayed
    And  legal act section is displayed in milestone explorer page
    And  click on close button present in milestone explorer page
    #####################################End MileStone Part##############################################
    Then OverView screen is displayed
    #####################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " annex changes new release...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "annex changes new release...." keyword
    #####################################End Renaming Proposal Part####################################
    When click on open button of Annex 1
    Then Annex page is displayed
    And  navigation pane is displayed
    And  toc editing button is available
    And  preface and body is present in annex navigation pane
    When click on "Postal items for which the public list of natio..." link in navigation pane
    Then content of subparagraph 1 of paragraph 1 is displayed in annex page
    When double click on the content of subparagraph 1 of paragraph 1 in annex page
    Then ck editor window is displayed
    When replace content "List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5." with the existing content in ck editor text box of a subparagraph in annex page
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in subparagraph 1 of paragraph 1 in annex page
      | List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5. |
    And  below words are showing as grey and strikethrough in subparagraph 1 of paragraph 1 in annex page
      | Postal items for which the public list of national and all cross-border tariffs to other Member States shall be notified to the national regulatory authorities: |
    When double click on the content of subparagraph 1 of paragraph 1 in annex page
    Then ck editor window is displayed
    When Add " " at the end of the ck editor text box of a subparagraph in annex page
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in subparagraph 1 of paragraph 1 in annex page
      | List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5. |
    And  below words are showing as grey and strikethrough in subparagraph 1 of paragraph 1 in annex page
      | Postal items for which the public list of national and all cross-border tariffs to other Member States shall be notified to the national regulatory authorities: |
    When double click on the content of subparagraph 1 of paragraph 1 in annex page
    Then ck editor window is displayed
    When remove " " at the end of the ck editor text box of a subparagraph in annex page
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in subparagraph 1 of paragraph 1 in annex page
      | List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5. |
    And  below words are showing as grey and strikethrough in subparagraph 1 of paragraph 1 in annex page
      | Postal items for which the public list of national and all cross-border tariffs to other Member States shall be notified to the national regulatory authorities: |
    When click on "(a) The size limits of the postal items a-i (le..." link in navigation pane
    Then content of alinea 1 of point 1 of paragraph 2 is displayed in annex page
    When double click on alinea 1 of point 1 of paragraph 2 in annex page
    Then ck editor window is displayed
    When Add " " at the end of the ck editor text box of an alinea in annex page
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  " " is not added at the end of alinea 1 of point 1 of paragraph 2 in annex page
    When click on "(*) The tariffs corresponding to the postal ite..." link in navigation pane
    Then content of subparagraph 1 of paragraph 3 is displayed in annex page
    When double click on the content of subparagraph 1 of paragraph 3 in annex page
    Then ck editor window is displayed
    When replace content "(*) The tariffs corresponding to the postal items shall be single piece and not contain any special discounts on the basis of volumes or on any other special treatment." with the existing content in ck editor text box of a subparagraph in annex page
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in subparagraph 1 of paragraph 3 in annex page
      | be single piece and |
    When click on "(****) The tariffs above shall correspond to it..." link in navigation pane
    Then content of subparagraph 4 of paragraph 3 is displayed in annex page
    When double click on the content of subparagraph 4 of paragraph 3 in annex page
    Then ck editor window is displayed
    When replace content "(****) The tariffs above shall correspond to items delivered at the home or premises of the addressee in the Member State of destination." with the existing content in ck editor text box of a subparagraph in annex page
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in subparagraph 4 of paragraph 3 in annex page
      | of the addressee |
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    When tick on checkbox of milestone version "Version 1.0.0"
    And  tick on checkbox of version "1.0.4 Annex block updated" in recent changes
    Then "Comparing 1.0.0 and 1.0.4" is displayed
    And  below words are showing as bold and underlined in subparagraph 1 of paragraph 1 in single diffing page of annex page
      | List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5. |
    And  below words are showing as bold, underlined and strikethrough in subparagraph 1 of paragraph 1 in single diffing page of annex page
      | Postal items for which the public list of national and all cross-border tariffs to other Member States shall be notified to the national regulatory authorities: |
    And  there is only 1 addition(s) in single diff comparision page of annex page
    And  there is only 1 deletion(s) in single diff comparision page of annex page
    When tick on checkbox of version "1.0.6 Annex block updated" in recent changes
    Then "Comparing 1.0.0, 1.0.4 and 1.0.6" is displayed
    And  below words are showing as bold in subparagraph 1 of paragraph 1 in double diffing page of annex page
      | List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5. |
    And  below words are showing as bold and strikethrough in subparagraph 1 of paragraph 1 in double diffing page of annex page
      | Postal items for which the public list of national and all cross-border tariffs to other Member States shall be notified to the national regulatory authorities: |
    And  below words are showing as bold and underlined in subparagraph 1 of paragraph 3 in double diffing page of annex page
      | be single piece and |
    And  there is only 3 addition(s) in double diff comparision page of annex page
    And  there is only 2 deletion(s) in double diff comparision page of annex page
    When uncheck version "1.0.6 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.7 Annex block updated" in recent changes
    Then "Comparing 1.0.0, 1.0.4 and 1.0.7" is displayed
    And  below words are showing as bold in subparagraph 1 of paragraph 1 in double diffing page of annex page
      | List of items for which universal service provider's tariffs shall be subject to the price transparency measures and affordability assessment provided for in Articles 4 and 5. |
    And  below words are showing as bold and strikethrough in subparagraph 1 of paragraph 1 in double diffing page of annex page
      | Postal items for which the public list of national and all cross-border tariffs to other Member States shall be notified to the national regulatory authorities: |
    And  below words are showing as bold and underlined in subparagraph 1 of paragraph 3 in double diffing page of annex page
      | be single piece and |
    And  below words are showing as bold and underlined in subparagraph 4 of paragraph 3 in double diffing page of annex page
      | of the addressee |
    And  there is only 4 addition(s) in double diff comparision page of annex page
    And  there is only 2 deletion(s) in double diff comparision page of annex page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on close button present in annex page
    Then OverView screen is displayed
    And  close the browser

  @BasicRegressionAnnex
  Scenario: LEOS-XXXX [CN] different operations with annex elements plus diffing functionalities
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\2020-0306(COD).leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    And  explanatory memorandum section is present
    And  legal act section is present
    And  annexes section is present
    #####################################Start MileStone Part############################################
    And  no milestone exists in milestones section
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Meeting of the Council" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | Meeting of the Council |
      | Other                  |
    When click on milestone option as Other
    And  type "Council proposal1" in title box
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  click on message "Council proposal1 has been created for this proposal."
    And  "Council proposal1" is showing in row 1 of title column of milestones table in council instance
    And  today's date is showing in date column of milestones table
    And  "File ready" is showing in status column of milestones table
    When click on the link present in the row 1 of title column in milestones table
    Then milestone explorer page is displayed
    And  explanatory memorandum section is displayed
    And  legal act section is displayed in milestone explorer page
    And  click on close button present in milestone explorer page
    #####################################End MileStone Part##############################################
    Then OverView screen is displayed
    #####################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Automation for annex changes...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Automation for annex changes...." keyword
    #####################################End Renaming Proposal Part####################################
    When click on open button of Annex 1
    Then Annex page is displayed
    And  navigation pane is displayed
    And  toc editing button is available
    And  preface and body is present in annex navigation pane
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 1 is displayed in annex page
    When click on insert before icon present in show all actions icon of paragraph 1 in annex page
    Then "Paragraph inserted" is displayed
    And  click on message "Paragraph inserted"
    And  paragraph 1 is displayed in annex page
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    When append content "Text text text text text." with the existing content in ck editor text box of a paragraph in annex page
    And  click on save close button of ck editor
    Then "Text text text text text." is appended to the paragraph 1 content in annex page
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 2 is displayed in annex page
    When click on insert after icon present in show all actions icon of paragraph 2 in annex page
    Then "Paragraph inserted" is displayed
    And  click on message "Paragraph inserted"
    When click on row number 5 in navigation pane
    Then paragraph 3 is displayed in annex page
    When double click on the content of paragraph 3 in annex page
    Then ck editor window is displayed
    When append content " Insert Paragraph after table with a lot of Text." with the existing content in ck editor text box of a paragraph in annex page
    And  click on save close button of ck editor
    Then " Insert Paragraph after table with a lot of Text." is appended to the paragraph 3 content in annex page
    When double click on the content of paragraph 3 in annex page
    Then ck editor window is displayed
    When click on table icon of ck editor
    Then table properties window is displayed
    When provide rows as 3 in table properties window
    And  provide columns as 5 in table properties window
    And  select option with value "both" from headers dropdown in table properties window
    And  click on ok button present in table properties window
    And  click on save close button of ck editor
    Then paragraph 4 is displayed in annex page
    And  table 1 in paragraph 4 is displayed in annex page
    And  paragraph 5 is displayed in annex page
    When double click on the content of paragraph 4 in annex page
    Then ck editor window is displayed
    When click on header cell 1 of row 1 of table header in paragraph while ck editor is open
    When add "Edit" in header cell 1 of row 1 of table header in paragraph while ck editor is open
    And  click on header cell 2 of row 1 of table header in paragraph while ck editor is open
    And  add "column1" in header cell 2 of row 1 of table header in paragraph while ck editor is open
    And  click on header cell 3 of row 1 of table header in paragraph while ck editor is open
    And  add "column2" in header cell 3 of row 1 of table header in paragraph while ck editor is open
    And  click on header cell 4 of row 1 of table header in paragraph while ck editor is open
    And  add "column3" in header cell 4 of row 1 of table header in paragraph while ck editor is open
    And  click on header cell 5 of row 1 of table header in paragraph while ck editor is open
    And  add "column4" in header cell 5 of row 1 of table header in paragraph while ck editor is open
    And  click on header cell 1 of row 1 of table body in paragraph while ck editor is open
    And  add "row 1" in header cell 1 of row 1 of table body in paragraph while ck editor is open
    And  click on paragraph of data cell 1 of row 1 of table body while ck editor is open
    And  add "1" in paragraph of data cell 1 of row 1 of table body while ck editor is open
    And  click on paragraph of data cell 2 of row 1 of table body while ck editor is open
    And  add "2" in paragraph of data cell 2 of row 1 of table body while ck editor is open
    And  click on paragraph of data cell 3 of row 1 of table body while ck editor is open
    And  add "3" in paragraph of data cell 3 of row 1 of table body while ck editor is open
    And  click on paragraph of data cell 4 of row 1 of table body while ck editor is open
    And  add "4" in paragraph of data cell 4 of row 1 of table body while ck editor is open
    And  click on header cell 1 of row 2 of table body in paragraph while ck editor is open
    And  add "row 2" in header cell 1 of row 2 of table body in paragraph while ck editor is open
    And  click on paragraph of data cell 1 of row 2 of table body while ck editor is open
    And  add "5" in paragraph of data cell 1 of row 2 of table body while ck editor is open
    And  click on paragraph of data cell 2 of row 2 of table body while ck editor is open
    And  add "6" in paragraph of data cell 2 of row 2 of table body while ck editor is open
    And  click on paragraph of data cell 3 of row 2 of table body while ck editor is open
    And  add "7" in paragraph of data cell 3 of row 2 of table body while ck editor is open
    And  click on paragraph of data cell 4 of row 2 of table body while ck editor is open
    And  add "8" in paragraph of data cell 4 of row 2 of table body while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  the data "Edit" is added in header cell 1 of row 1 of table body in paragraph 4
    And  the data "column1" is added in header cell 2 of row 1 of table body in paragraph 4
    And  the data "column2" is added in header cell 3 of row 1 of table body in paragraph 4
    And  the data "column3" is added in header cell 4 of row 1 of table body in paragraph 4
    And  the data "column4" is added in header cell 5 of row 1 of table body in paragraph 4
    And  the data "row 1" is added in header cell 1 of row 2 of table body in paragraph 4
    And  the data "1" is added in data cell 1 of row 2 of table body in paragraph 4
    And  the data "2" is added in data cell 2 of row 2 of table body in paragraph 4
    And  the data "3" is added in data cell 3 of row 2 of table body in paragraph 4
    And  the data "4" is added in data cell 4 of row 2 of table body in paragraph 4
    And  the data "row 2" is added in header cell 1 of row 3 of table body in paragraph 4
    And  the data "5" is added in data cell 1 of row 3 of table body in paragraph 4
    And  the data "6" is added in data cell 2 of row 3 of table body in paragraph 4
    And  the data "7" is added in data cell 3 of row 3 of table body in paragraph 4
    And  the data "8" is added in data cell 4 of row 3 of table body in paragraph 4
    When double click on the content of paragraph 4 in annex page
    Then ck editor window is displayed
    When click on paragraph of data cell 1 of row 1 of table body while ck editor is open
    And  click on insert footnote icon present in ck editor panel
    Then edit footnote window is displayed
    When enter "Footnote text to 1." in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 1 is showing in data cell 1 of row 1 of table body in paragraph while ck editor is open
    When click on header cell 2 of row 1 of table header in paragraph while ck editor is open
    And  click on insert footnote icon present in ck editor panel
    Then edit footnote window is displayed
    When enter "Footnote to Column1" in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 1 is showing in header cell 2 of row 1 of table header in paragraph while ck editor is open
    And  footnote with marker 2 is showing in data cell 1 of row 1 of table body in paragraph while ck editor is open
    When click on save close button of ck editor
    Then ck editor window is not displayed
    And  footnote with marker 10 is showing in header cell 2 of row 1 of table body in paragraph 4
    And  footnote with marker 11 is showing in data cell 1 of row 2 of table body in paragraph 4
    When click on "Text text text text text." link in navigation pane
    Then paragraph 1 is displayed in annex page
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    When click on the content of paragraph while ck editor is open
    And  click on insert footnote icon present in ck editor panel
    Then edit footnote window is displayed
    When enter "Footnote before table" in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 1 is showing in the content of paragraph while ck editor is open
    When click on save close button of ck editor
    Then ck editor window is not displayed
    And  footnote with marker 1 is showing in the content of paragraph 1
    And  footnote with marker 11 is showing in header cell 2 of row 1 of table body in paragraph 4
    And  footnote with marker 12 is showing in data cell 1 of row 2 of table body in paragraph 4
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    When delete the footnote present in the content of paragraph while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  footnote is not present in the content of paragraph 1
    When click on "Editcolumn1column2column3column4row 11234row 25678" link in navigation pane
    Then paragraph 4 is displayed in annex page
    When click on footnote number 10
    Then the text "Footnote to Column1" is showing in the footnote list
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    When mousehover on footnote number 1 and check footnote text is displayed
    And  double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When scroll to row 9 of table body in paragraph while ck editor is open
    And  do right click on row 9 of table body in paragraph and mousehover on option "Row" and click on "Insert Row After" option in the submenu while ck editor is open
    And  scroll to row 9 of table body in paragraph while ck editor is open
    And  click on data cell 1 of row 10 of table body in paragraph while ck editor is open
    And  add "New row" in data cell 1 of row 10 of table body in paragraph while ck editor is open
    And  click on data cell 4 of row 10 of table body in paragraph while ck editor is open
    And  add "Regulation (EU) 2013/575 on..." in data cell 4 of row 10 of table body in paragraph while ck editor is open
    When click on insert footnote icon present in ck editor panel
    Then edit footnote window is displayed
    When enter "Footnote reference in original table." in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 10 is showing in data cell 4 of row 10 of table body in paragraph while ck editor is open
    When click on save close button of ck editor
    Then ck editor window is not displayed
    And  total row number of the table is 10 in paragraph 2
    And  the data "New row" is added in data cell 1 of row 10 of table body in paragraph 2
    And  the data "Regulation (EU) 2013/575 on..." is added in data cell 4 of row 10 of table body in paragraph 2
    And  "Regulation (EU) 2013/575" is hyperlinked with title to EUR-Lex in data cell 4 of row 10 of table body in paragraph 2
    And  footnote with marker 10 is showing in data cell 4 of row 10 of table body in paragraph 2
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When scroll to row 8 of table body in paragraph while ck editor is open
    And  do right click on row 8 of table body in paragraph and mousehover on option "Row" and click on "Insert Row Before" option in the submenu while ck editor is open
    And  click on data cell 1 of row 8 of table body in paragraph while ck editor is open
    And  add "New row2" in data cell 1 of row 8 of table body in paragraph while ck editor is open
    And  click on data cell 4 of row 8 of table body in paragraph while ck editor is open
    And  add "Regulation (EU) 2019/1056 on..." in data cell 4 of row 8 of table body in paragraph while ck editor is open
    When click on insert footnote icon present in ck editor panel
    Then edit footnote window is displayed
    When enter "Reference." in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 7 is showing in data cell 4 of row 8 of table body in paragraph while ck editor is open
    When click on save close button of ck editor
    Then ck editor window is not displayed
    And  total row number of the table is 11 in paragraph 2
    And  the data "New row2" is added in data cell 1 of row 8 of table body in paragraph 2
    And  the data "Regulation (EU) 2019/1056 on..." is added in data cell 4 of row 8 of table body in paragraph 2
    And  "Regulation (EU) 2019/1056" is hyperlinked with title to EUR-Lex in data cell 4 of row 8 of table body in paragraph 2
    And  footnote with marker 7 is showing in data cell 4 of row 8 of table body in paragraph 2
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When scroll to row 8 of table body in paragraph while ck editor is open
    And  delete the footnote present in data cell 4 of row 8 of table body in paragraph while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  footnote is not present in data cell 4 of row 8 of table body in paragraph 2
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    When double click on the content of paragraph 2 in annex page
    And  click on paragraph of data cell 1 of row 1 of table body while ck editor is open
    And  append " Text changed." in data cell 1 of row 1 of table body in paragraph while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  " Text changed." is added to data cell 1 of row 1 of table body in paragraph 2
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    And  sleep for 1000 milliseconds
    When scroll to row 2 of table body in paragraph while ck editor is open
    And  sleep for 1000 milliseconds
    When click on paragraph of data cell 2 of row 2 of table body while ck editor is open
    When add open and close square bracket to the content of data cell 2 of row 2 of table body in paragraph
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  open and close square bracket is added to the content of data cell 2 of row 2 of table body in paragraph 2
    When click on "Union non-customs formality Text changed.Acrony..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    And  sleep for 1000 milliseconds
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When do right click on data cell 5 of row 1 of table body in paragraph and mousehover on option "Column" and click on "Insert Column After" option in the submenu while ck editor is open
    And  click on data cell 6 of row 1 of table body in paragraph while ck editor is open
    And  add "New Column" in data cell 6 of row 1 of table body in paragraph while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Column" is added to data cell 6 of row 1 of table body in paragraph 2
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When do right click on data cell 5 of row 1 of table body in paragraph and mousehover on option "Column" and click on "Insert Column Before" option in the submenu while ck editor is open
    And  click on data cell 5 of row 1 of table body in paragraph while ck editor is open
    And  add "New Column2" in data cell 5 of row 1 of table body in paragraph while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Column2" is added to data cell 5 of row 1 of table body in paragraph 2
    When click on "Union non-customs formality Text changed.Acrony..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    And  sleep for 1000 milliseconds
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When scroll to row 10 of table body in paragraph while ck editor is open
    And  do right click on row 10 of table body in paragraph and mousehover on option "Row" and click on "Delete Rows" option in the submenu while ck editor is open
    And  number of rows in the table in paragraph is 10 while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  total row number of the table is 10 in paragraph 2
    When click on "Union non-customs formality Text changed.Acrony..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    When double click on the content of paragraph 2 in annex page
    Then ck editor window is displayed
    When scroll to row 6 of table body in paragraph while ck editor is open
    And  do right click on row 6 of table body in paragraph and mousehover on option "Row" and click on "Delete Rows" option in the submenu while ck editor is open
    And  number of rows in the table in paragraph is 9 while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  total row number of the table is 11 in paragraph 2
    And  row 6 of table in paragraph 2 is showing grey and strikethrough
    When click on "Union non-customs formality Text changed.Acrony..." link in navigation pane
    Then paragraph 2 is displayed in annex page
    When scroll to row 2 of table body in paragraph 2
    And  select content in data cell 1 of row 3 of table body in paragraph 2
    And  click on annotation pop up button
    Then comment button is displayed
    And  suggest button is displayed
    And  highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "comment for document for products" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "comment for document for products" is showing in the comment text box
    When click on comment box 1
    And  mouse hover on selected comment text box
    And  click on reply button in selected comment box
    Then add reply button is displayed and enabled in selected comment box
    When switch to "comment" rich textarea iframe
    And  enter "reply to comment for document for products" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" publish button
    Then "reply to comment for document for products" is showing in the reply list of selected comment box
    When mouse hover on comment text box
    When click on delete icon of comment text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment text box is not present
    When switch from iframe to main window
    And  click on toggle bar move to right
    And  scroll to data cell 1 of row 3 of table body in paragraph 2
    And  select content in data cell 1 of row 3 of table body in paragraph 2
    Then suggest button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "Something" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    And  click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "Something" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    When click on suggest box 1
    And  mouse hover on selected suggest box
    And  click on reply button in selected suggest box
    Then add reply button is displayed and enabled in selected suggest box
    When switch to "suggest" rich textarea iframe
    And  enter "reply to Something" in comment box rich textarea
    And  switch to parent frame
    And  click on "suggest" publish button
    Then "reply to Something" is showing in the reply list of selected suggest box
    When click on accept button present in selected suggest box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then "Something" is showing as bold in data cell 1 of row 3 of table body in paragraph 2
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When click on show more button in recent changes section inside version pane
    And  tick on checkbox of milestone version "Version 1.0.0"
    And  tick on checkbox of version "1.0.3 Annex block updated" in recent changes
    Then "Comparing 1.0.0 and 1.0.3" is displayed
    And  "Text...Text text text text text." is showing as bold and underlined in paragraph 1 in single diffing page of annex page
    When uncheck version "1.0.3 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.5 Annex block updated" in recent changes
    Then "Comparing 1.0.0 and 1.0.5" is displayed
    And  "Text... Insert Paragraph after table with a lot of Text." is showing as bold and underlined in paragraph 3 in single diffing page of annex page
    When uncheck version "1.0.5 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.7 Annex block updated" in recent changes
    Then "Comparing 1.0.0 and 1.0.7" is displayed
    And  the data "Edit" is showing as bold and underlined in header cell 1 of row 1 of table body in paragraph 4 in single diffing page of annex page
    And  the data "column1" is showing as bold and underlined in header cell 2 of row 1 of table body in paragraph 4 in single diffing page of annex page
    And  the data "column2" is showing as bold and underlined in header cell 3 of row 1 of table body in paragraph 4 in single diffing page of annex page
    And  the data "column3" is showing as bold and underlined in header cell 4 of row 1 of table body in paragraph 4 in single diffing page of annex page
    And  the data "column4" is showing as bold and underlined in header cell 5 of row 1 of table body in paragraph 4 in single diffing page of annex page
    And  the data "row 1" is showing as bold and underlined in header cell 1 of row 2 of table body in paragraph 4 in single diffing page of annex page
    And  the data "1" is showing as bold and underlined in data cell 1 of row 2 of table body in paragraph 4 in single diffing page of annex page
    And  the data "2" is showing as bold and underlined in data cell 2 of row 2 of table body in paragraph 4 in single diffing page of annex page
    And  the data "3" is showing as bold and underlined in data cell 3 of row 2 of table body in paragraph 4 in single diffing page of annex page
    And  the data "4" is showing as bold and underlined in data cell 4 of row 2 of table body in paragraph 4 in single diffing page of annex page
    And  the data "row 2" is showing as bold and underlined in header cell 1 of row 3 of table body in paragraph 4 in single diffing page of annex page
    And  the data "5" is showing as bold and underlined in data cell 1 of row 3 of table body in paragraph 4 in single diffing page of annex page
    And  the data "6" is showing as bold and underlined in data cell 2 of row 3 of table body in paragraph 4 in single diffing page of annex page
    And  the data "7" is showing as bold and underlined in data cell 3 of row 3 of table body in paragraph 4 in single diffing page of annex page
    And  the data "8" is showing as bold and underlined in data cell 4 of row 3 of table body in paragraph 4 in single diffing page of annex page
    When uncheck version "1.0.7 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.14 Annex block updated" in recent changes
    Then "Comparing 1.0.0 and 1.0.14" is displayed
    And  the data "Union non-customs formality Text changed." is showing as bold and underlined in data cell 1 of row 1 of table body in paragraph 2 in single diffing page of annex page
    And  the data "Union non-customs formality" is showing as bold and strikethrough in data cell 1 of row 1 of table body in paragraph 2 in single diffing page of annex page
    When uncheck version "1.0.14 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.19 Annex block updated" in recent changes
    Then "Comparing 1.0.0 and 1.0.19" is displayed
    And  row 6 of table body in paragraph 2 is showing as bold and strikethrough in single diffing page of annex page
    And  row 11 of table body in paragraph 2 is showing as bold and strikethrough in single diffing page of annex page
    When uncheck version "1.0.19 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.20 Suggestion content merged" in recent changes
    Then "Comparing 1.0.0 and 1.0.20" is displayed
    And  the data "Something" is showing as bold and underlined in data cell 1 of row 3 of table body in paragraph 2 in single diffing page of annex page
    And  the data "health" is showing as bold and strikethrough in data cell 1 of row 3 of table body in paragraph 2 in single diffing page of annex page
    When uncheck version "1.0.20 Suggestion content merged" in recent changes section
    And  tick on checkbox of version "1.0.2 Annex block inserted" in recent changes
    And  tick on checkbox of version "1.0.3 Annex block updated" in recent changes
    Then "Comparing 1.0.0, 1.0.2 and 1.0.3" is displayed
    And  "Text text text text text." is showing as bold and underlined in paragraph 1 in double diffing page of annex page
    When uncheck version "1.0.3 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.14 Annex block updated" in recent changes
    Then "Comparing 1.0.0, 1.0.2 and 1.0.14" is displayed
    And  "Union non-customs formality Text changed." is showing as bold and underlined in data cell 1 of row 1 of table body in paragraph 2 in double diffing page of annex page
    And  "Union non-customs formality" is showing as strikethrough in data cell 1 of row 1 of table body in paragraph 2 in double diffing page of annex page
    When uncheck version "1.0.14 Annex block updated" in recent changes section
    And  tick on checkbox of version "1.0.15 Annex block updated" in recent changes
    Then "Comparing 1.0.0, 1.0.2 and 1.0.15" is displayed
    And  "[CHED-A]" is showing as bold and underlined in data cell 2 of row 2 of table body in paragraph 2 in double diffing page of annex page
    And  "CHED-A" is showing as bold and strikethrough in data cell 2 of row 2 of table body in paragraph 2 in double diffing page of annex page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on close button present in annex page
    Then OverView screen is displayed
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Meeting of the Council" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | Meeting of the Council |
      | Other                  |
    When click on milestone option as Other
    And  type "council proposal2" in title box
    When click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  click on message "council proposal2 has been created for this proposal."
    And  "council proposal2" is showing in row 1 of title column of milestones table in council instance
    And  today's date is showing under date column row 1 of milestones table
    And  "File ready" is showing under status column row 1 of milestones table
    When click on the link present in the row 1 of title column in milestones table
    Then milestone explorer page is displayed
    And  explanatory memorandum section is displayed
    And  legal act section is displayed in milestone explorer page
    And  annex section is displayed in milestone explorer page
    When click on annex section in milestone explorer page
    Then annex page is displayed in milestone explorer page
    And  annotations section is opened in milestone explorer page
    When switch from main window to iframe "hyp_sidebar_frame"
    Then there are 1 annotations present in annotations window in milestone explorer page
    When click on Orphans link present in annotations window in milestone explorer page
    Then there are 1 annotations present in orphans window in milestone explorer page
    And  switch from iframe to main window
    When click on close button present in milestone explorer page
    Then OverView screen is displayed
    And  close the browser

  @AnnexAdditionalTableTesting
  Scenario: [CN] additional test with table present in annex
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\2020-0306(COD).leg"
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
    When append " Automation for annex additional test...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Automation for annex additional test...." keyword
    #####################################End Renaming Proposal Part####################################
    When click on open button of Annex 1
    Then Annex page is displayed
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 1 is displayed in annex page
    And  sleep for 2000 milliseconds
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    When click on data cell 4 of row 1 of table body in paragraph while ck editor is open
    And  replace "Relevant Union non-customs legislation" in paragraph of data cell 4 of row 1 of table body while ck editor is open
    And  click on data cell 5 of row 1 of table body in paragraph while ck editor is open
    And  replace "Connection by" in paragraph of data cell 5 of row 1 of table body while ck editor is open
    And  scroll to row 2 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 2 of table body in paragraph while ck editor is open
    Then footnote with marker 2 is not displayed in data cell 4 of row 2 of table body in paragraph while ck editor is open
    When scroll to row 2 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 3 of table body in paragraph while ck editor is open
    When scroll to row 2 of table body in paragraph while ck editor is open
    And  click on data cell 4 of row 3 of table body in paragraph while ck editor is open
    And  replace "Regulation (EU) 2017/625" in paragraph of data cell 4 of row 3 of table body while ck editor is open
    And  scroll to row 3 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 4 of table body in paragraph while ck editor is open
    And  scroll to row 3 of table body in paragraph while ck editor is open
    And  click on data cell 4 of row 4 of table body in paragraph while ck editor is open
    And  replace "Regulation (EU) 2017/625" in paragraph of data cell 4 of row 4 of table body while ck editor is open
    And  scroll to row 4 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 5 of table body in paragraph while ck editor is open
    And  scroll to row 4 of table body in paragraph while ck editor is open
    And  click on data cell 4 of row 5 of table body in paragraph while ck editor is open
    And  replace "Regulation (EU) 2017/625" in paragraph of data cell 4 of row 5 of table body while ck editor is open
    And  scroll to row 6 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 6 of table body in paragraph while ck editor is open
    Then footnote with marker 3 is not displayed in data cell 4 of row 6 of table body in paragraph while ck editor is open
    And  scroll to row 7 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 7 of table body in paragraph while ck editor is open
    Then footnote with marker 4 is not displayed in data cell 4 of row 7 of table body in paragraph while ck editor is open
    And  scroll to row 8 of table body in paragraph while ck editor is open
    And  delete paragraph 2 present in data cell 4 of row 8 of table body in paragraph while ck editor is open
    Then footnote with marker 5 is not displayed in data cell 4 of row 8 of table body in paragraph while ck editor is open
    And  scroll to row 8 of table body in paragraph while ck editor is open
    And  click on data cell 2 of row 9 of table body in paragraph while ck editor is open
    And  replace "ICGS, ICGL, ICGD" in paragraph of data cell 2 of row 9 of table body while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  the data "Relevant Union legislation " is showing in grey and strikethrough in data cell 4 of row 1 of table body in paragraph 1
    And  the data "Relevant Union non-customs legislation" is showing in bold in data cell 4 of row 1 of table body in paragraph 1
    And  the data "Date of application " is showing in grey and strikethrough in data cell 5 of row 1 of table body in paragraph 1
    And  the data "Connection by" is showing in bold in data cell 5 of row 1 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 2 of table body in paragraph 1
    And  the data "Articles 56 and 57 of " is showing in grey and strikethrough in data cell 4 of row 3 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 3 of table body in paragraph 1
    And  the data "Articles 56 and 57 of " is showing in grey and strikethrough in data cell 4 of row 4 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 4 of table body in paragraph 1
    And  the data "Articles 56 and 57 of " is showing in grey and strikethrough in data cell 4 of row 5 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 5 of table body in paragraph 1
    And  authorial note with marker 3 is showing in strikethrough in aknp tag 1 in data cell 4 of row 6 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 6 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 7 of table body in paragraph 1
    And  aknp tag 2 is showing in grey and strikethrough in data cell 4 of row 8 of table body in paragraph 1
    And  the data "Cultural goods" is showing in grey and strikethrough in data cell 2 of row 9 of table body in paragraph 1
    And  the data "ICGS, ICGL, ICGD" is showing in bold in data cell 2 of row 9 of table body in paragraph 1
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version                  |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | View                                   |
      | See navigation pane                    |
      | Annex action                           |
      | Switch structure to article            |
    When click on "Save this version" option
    Then save this version window is displayed
    When provide "REV5" as title in save this version window
    And  click on save button in save this version window
    Then "The version has been saved" message is displayed
    And  click on message "The version has been saved"
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  search button is displayed in versions pane section
    When click on actions hamburger icon for a major version whose title is "REV5"
    And  click on "Change to base version" option
    Then "Base version for comparison changed to 0.2.0" message is displayed
    And  click on message "Base version for comparison changed to 0.2.0"
    When click on navigation pane toggle link
    And  click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 1 is displayed in annex page
    And  sleep for 2000 milliseconds
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    When scroll to row 4 of table body in paragraph while ck editor is open
    And  click on paragraph of data cell 4 of row 5 of table body while ck editor is open
    And  click on insert footnote icon present in ck editor panel
    Then edit footnote window is displayed
    When enter "Footnote text Row 5 column 4" in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 2 is showing in data cell 4 of row 5 of table body in paragraph while ck editor is open
    When scroll to row 1 of table body in paragraph while ck editor is open
    And  sleep for 1000 milliseconds
    And  double click on footnote with marker 1 present in data cell 4 of row 2 of table body in paragraph while ck editor is open
    Then edit footnote window is displayed
    When enter "Regulation (EU) 2017/625 of the European Parliament and of the Council of 24 March 2019 on official controls and other official activities performed to ensure the application of food and feed law, rules on animal health and welfare, plant health and plant protection products, amending Regulations (EC) No 999/2001, (EC) No 396/2005, (EC) No 1069/2009, (EC) No 1107/2009, (EU) No 1151/2012, (EU) No 652/2014, (EU) 2016/429 and (EU) 2016/2031 of the European Parliament and of the Council, CHANGED TEXT Council Regulations (EC) No 1/2005 and (EC) No 1099/2009 and Council Directives 98/58/EC, 1999/74/EC, 2007/43/EC, 2008/119/EC and 2008/120/EC, and repealing Regulations (EC) No 854/2004 and (EC) No 882/2004 of the European Parliament and of the Council, Council Directives 89/608/EEC, 89/662/EEC, 90/425/EEC, 91/496/EEC, 96/23/EC, 96/93/EC and 97/78/EC and Council Decision 92/438/EEC (Official Controls Regulation) (OJ L 95, 7.4.2017, p. 1)." in text area of edit footnote window
    And  click on ok button present in edit footnote window
    Then footnote with marker 1 is showing in data cell 4 of row 2 of table body in paragraph while ck editor is open
    When click on save close button of ck editor
    Then ck editor window is not displayed
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 1 is displayed in annex page
    When click on footnote number 1
    Then index 1 of authorial note table contains "CHANGED TEXT"
    And  index 2 of authorial note table contains "Footnote text Row 5 column 4"
    When click on toc edit button
    And  click on versions pane accordion
    And  click on actions hamburger icon of a major version "Version 0.1.0"
    And  click on "Revert to this version" option
    Then restore version window is displayed
    When click on revert button present in restore version window
    And  click on navigation pane toggle link
    Then toc edit button is displayed and enabled
    When click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 1 is displayed in annex page
    And  sleep for 2000 milliseconds
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    When scroll to row 5 of table body in paragraph while ck editor is open
    And  do right click on row 6 of table body in paragraph and mousehover on option "Row" and click on "Delete Rows" option in the submenu while ck editor is open
    And  number of rows in the table in paragraph is 8 while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  total row number of the table is 9 in paragraph 1
    When scroll to row 5 of table body in paragraph 1
    Then row 6 of table in paragraph 1 is showing grey and strikethrough
    When click on versions pane accordion
    And  click on actions hamburger icon for a major version whose title is "REV5"
    And  click on "Revert to this version" option
    Then restore version window is displayed
    When click on revert button present in restore version window
    And  click on navigation pane toggle link
    And  click on "Union non-customs formalityAcronymUnion non-cus..." link in navigation pane
    Then table 1 in paragraph 1 is displayed in annex page
    And  sleep for 2000 milliseconds
    When double click on the content of paragraph 1 in annex page
    Then ck editor window is displayed
    And  click on data cell 5 of row 2 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 2 of table body while ck editor is open
    When scroll to row 2 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 3 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 3 of table body while ck editor is open
    When scroll to row 3 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 4 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 4 of table body while ck editor is open
    When scroll to row 4 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 5 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 5 of table body while ck editor is open
    When scroll to row 5 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 6 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 6 of table body while ck editor is open
    When scroll to row 6 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 7 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 7 of table body while ck editor is open
    When scroll to row 7 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 8 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 8 of table body while ck editor is open
    When scroll to row 8 of table body in paragraph while ck editor is open
    And  click on data cell 5 of row 9 of table body in paragraph while ck editor is open
    And  replace "3 March 2025" in paragraph of data cell 5 of row 9 of table body while ck editor is open
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  the data "Relevant Union non-customs legislation" is present in data cell 4 of row 1 of table body in paragraph 1
    And  the data "Connection by" is present in data cell 5 of row 1 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 2 of table body in paragraph 1
    And  the data "2023" is showing in grey and strikethrough in data cell 5 of row 2 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 2 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 2 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 3 of table body in paragraph 1
    And  the data "2023" is showing in grey and strikethrough in data cell 5 of row 3 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 3 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 3 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 4 of table body in paragraph 1
    And  the data "2023" is showing in grey and strikethrough in data cell 5 of row 4 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 4 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 4 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 5 of table body in paragraph 1
    And  the data "2023" is showing in grey and strikethrough in data cell 5 of row 5 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 5 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 5 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 6 of table body in paragraph 1
    And  the data "2024" is showing in grey and strikethrough in data cell 5 of row 6 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 6 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 6 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 7 of table body in paragraph 1
    And  the data "2023" is showing in grey and strikethrough in data cell 5 of row 7 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 7 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 7 of table body in paragraph 1
    And  the data "1" is showing in grey and strikethrough in data cell 5 of row 8 of table body in paragraph 1
    And  the data "2023" is showing in grey and strikethrough in data cell 5 of row 8 of table body in paragraph 1
    And  the data "3" is showing in bold in data cell 5 of row 8 of table body in paragraph 1
    And  the data "2025" is showing in bold in data cell 5 of row 8 of table body in paragraph 1
    And  the data "Regulation (EU) 2017/625" is present in data cell 4 of row 3 of table body in paragraph 1
    And  the data "Regulation (EU) 2017/625" is present in data cell 4 of row 4 of table body in paragraph 1
    And  the data "Regulation (EU) 2017/625" is present in data cell 4 of row 5 of table body in paragraph 1
    And  the data "ICGS, ICGL, ICGD" is present in data cell 2 of row 9 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 2 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 3 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 4 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 5 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 6 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 7 of table body in paragraph 1
    And  aknp 2 is not present in data cell 4 of row 8 of table body in paragraph 1
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  search button is displayed in versions pane section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" message is displayed
    When tick on checkbox of major version "Version 0.1.0"
#    And  click on show modifications button present under version "Version 0.2.0" in version pane
    And  tick on checkbox of major version "Version 0.2.0"
    Then "Comparing 0.1.0 and 0.2.0" message is displayed
    And  the data "Relevant Union legislation " is showing in bold, underlined and strikethrough in data cell 4 of row 1 of table body of paragraph 1 in single diffing comparision page
    And  the data "Relevant Union non-customs legislation" is showing in bold, underlined in data cell 4 of row 1 of table body of paragraph 1 in single diffing comparision page
    And  the data "Date of application " is showing in bold, underlined and strikethrough in data cell 5 of row 1 of table body of paragraph 1 in single diffing comparision page
    And  the data "Connection by" is showing in bold, underlined in data cell 5 of row 1 of table body of paragraph 1 in single diffing comparision page
    And  the data "Commission Implementing Regulation (EU) 2019/1715)" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 2 of table body of paragraph 1 in single diffing comparision page
    And  the data "Articles 56 and 57 of" is showing in bold, underlined and strikethrough in data cell 4 of row 3 of table body of paragraph 1 in single diffing comparision page
    And  the data "Implementing Regulation (EU) 2019/1715" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 3 of table body of paragraph 1 in single diffing comparision page
    And  the data "Articles 56 and 57 of" is showing in bold, underlined and strikethrough in data cell 4 of row 4 of table body of paragraph 1 in single diffing comparision page
    And  the data "Implementing Regulation (EU) 2019/1715" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 4 of table body of paragraph 1 in single diffing comparision page
    And  the data "Articles 56 and 57 of" is showing in bold, underlined and strikethrough in data cell 4 of row 5 of table body of paragraph 1 in single diffing comparision page
    And  the data "Implementing Regulation (EU) 2019/1715" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 5 of table body of paragraph 1 in single diffing comparision page
    And  the data "Commission Regulation (EC) No 1235/2008" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 6 of table body of paragraph 1 in single diffing comparision page
    And  the data "Commission Regulation (EU) No 537/2011" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 7 of table body of paragraph 1 in single diffing comparision page
    And  the data "Commission Implementing Regulation (EU) No 1191/2014" is showing in bold, underlined and strikethrough in aknp of data cell 4 of row 8 of table body of paragraph 1 in single diffing comparision page
    And  the data "Cultural goods" is showing in bold, underlined and strikethrough in data cell 2 of row 9 of table body of paragraph 1 in single diffing comparision page
    And  the data "ICGS, ICGL, ICGD" is showing in bold, underlined in data cell 2 of row 9 of table body of paragraph 1 in single diffing comparision page
    When click on show more button in recent changes section inside version pane
    And  tick on checkbox of version "0.2.3 Annex block updated" in recent changes
    Then "Comparing 0.1.0, 0.2.0 and 0.2.3" message is displayed
    And  the content is showing in bold, underlined and strikethrough in row 6 of table body of paragraph 1 in double diffing comparision page
    And  the data "Commission Regulation (EU) No 537/2011" is showing in bold, underlined in data cell 4 of row 7 of table body of paragraph 1 in double diffing comparision page
    And  the data "Commission Implementing Regulation (EU) No 1191/2014" is showing in bold, underlined in data cell 4 of row 8 of table body of paragraph 1 in double diffing comparision page
    And  the data "ICGS, ICGL, ICGD" is showing in bold, underlined and strikethrough in data cell 2 of row 9 of table body of paragraph 1 in double diffing comparision page
    And  the data "Cultural goods" is showing in underlined in data cell 2 of row 9 of table body of paragraph 1 in double diffing comparision page
    When uncheck version "0.2.3 Annex block updated" in recent changes section
    And  tick on checkbox of version "0.2.5 Annex block updated" in recent changes
    Then "Comparing 0.1.0, 0.2.0 and 0.2.5" message is displayed
    And  the data "Relevant Union legislation " is showing in bold and strikethrough in data cell 4 of row 1 of table body of paragraph 1 in double diffing comparision page
    And  the data "Relevant Union non-customs legislation" is showing in bold in data cell 4 of row 1 of table body of paragraph 1 in double diffing comparision page
    And  the data "Date of application " is showing in bold and strikethrough in data cell 5 of row 1 of table body of paragraph 1 in double diffing comparision page
    And  the data "Connection by" is showing in bold in data cell 5 of row 1 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 2 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 2 of table body of paragraph 1 in double diffing comparision page
    And  the data "2023" is showing in bold, underlined and strikethrough in data cell 5 of row 2 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 2 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 3 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 3 of table body of paragraph 1 in double diffing comparision page
    And  the data "2023" is showing in bold, underlined and strikethrough in data cell 5 of row 3 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 3 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 4 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 4 of table body of paragraph 1 in double diffing comparision page
    And  the data "2023" is showing in bold, underlined and strikethrough in data cell 5 of row 4 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 4 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 5 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 5 of table body of paragraph 1 in double diffing comparision page
    And  the data "2023" is showing in bold, underlined and strikethrough in data cell 5 of row 5 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 5 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 6 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 6 of table body of paragraph 1 in double diffing comparision page
    And  the data "2024" is showing in bold, underlined and strikethrough in data cell 5 of row 6 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 6 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 7 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 7 of table body of paragraph 1 in double diffing comparision page
    And  the data "2023" is showing in bold, underlined and strikethrough in data cell 5 of row 7 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 7 of table body of paragraph 1 in double diffing comparision page
    And  the data "1" is showing in bold, underlined and strikethrough in data cell 5 of row 8 of table body of paragraph 1 in double diffing comparision page
    And  the data "3" is showing in bold, underlined in data cell 5 of row 8 of table body of paragraph 1 in double diffing comparision page
    And  the data "2023" is showing in bold, underlined and strikethrough in data cell 5 of row 8 of table body of paragraph 1 in double diffing comparision page
    And  the data "2025" is showing in bold, underlined in data cell 5 of row 8 of table body of paragraph 1 in double diffing comparision page
    And  the data "Articles 56 and 57 of" is showing in bold and strikethrough in data cell 4 of row 3 of table body of paragraph 1 in double diffing comparision page
    And  the data "Implementing Regulation (EU) 2019/1715" is showing in bold and strikethrough in aknp of data cell 4 of row 3 of table body of paragraph 1 in double diffing comparision page
    And  the data "Articles 56 and 57 of" is showing in bold and strikethrough in data cell 4 of row 4 of table body of paragraph 1 in double diffing comparision page
    And  the data "Implementing Regulation (EU) 2019/1715" is showing in bold and strikethrough in aknp of data cell 4 of row 4 of table body of paragraph 1 in double diffing comparision page
    And  the data "Articles 56 and 57 of" is showing in bold and strikethrough in data cell 4 of row 5 of table body of paragraph 1 in double diffing comparision page
    And  the data "Implementing Regulation (EU) 2019/1715" is showing in bold and strikethrough in aknp of data cell 4 of row 5 of table body of paragraph 1 in double diffing comparision page
    And  the data "Commission Regulation (EC) No 1235/2008" is showing in bold and strikethrough in aknp of data cell 4 of row 6 of table body of paragraph 1 in double diffing comparision page
    And  the data "Commission Regulation (EU) No 537/2011" is showing in bold and strikethrough in aknp of data cell 4 of row 7 of table body of paragraph 1 in double diffing comparision page
    And  the data "Commission Implementing Regulation (EU) No 1191/2014" is showing in bold and strikethrough in aknp of data cell 4 of row 8 of table body of paragraph 1 in double diffing comparision page
    And  the data "Cultural goods" is showing in bold and strikethrough in data cell 2 of row 9 of table body of paragraph 1 in double diffing comparision page
    And  the data "ICGS, ICGL, ICGD" is showing in bold in data cell 2 of row 9 of table body of paragraph 1 in double diffing comparision page
    When click on close button present in annex page
    Then OverView screen is displayed
    And  close the browser

  @exportEConsiliumInAnnexWithAllTextAndAnnotation
  Scenario: LEOS-XXXX export to eConsilium in annex page with all text and annotations
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\2020-0306(COD).leg"
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
    When append " Automation Testing exportEConsiliumWithAllTextAndAnnotation" keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Automation Testing exportEConsiliumWithAllTextAndAnnotation" keyword
    #####################################End Renaming Proposal Part####################################
    When click on open button of Annex 1
    Then Annex page is displayed
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version                  |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | View                                   |
      | See navigation pane                    |
      | Annex action                           |
      | Switch structure to article            |
    When click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When provide title "Export to eConsilium Testing" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then sleep for 2000 milliseconds
    When click on close button present in legal act page
    Then OverView screen is displayed
    Then "Export to eConsilium Testing" is showing under title column row 1 in Export to eConsilium section
    And  today's date is showing under date column row 1 in Export to eConsilium section
    And  "exported" is showing under status column row 1 in Export to eConsilium section
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    And  close the browser