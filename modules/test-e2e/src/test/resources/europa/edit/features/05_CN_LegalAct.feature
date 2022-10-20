#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Legal Act Page in CN instance of Edit Application
@LegalActRegressionScenariosEditCouncil
Feature: Legal Act Page Regression Features in Edit Council

  @LegalActScenario_Citation
  Scenario: LEOS-4146 CN Edition of elements - Browse Legal Act Citation Part
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
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    And  click on citation toggle link
    And  click on citation link present in navigation pane
    And  double click on citation 1
    Then ck editor window is displayed
    And  get text from ck editor text box
    When add "New Text" and delete "Treaty " in the ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added in the text box
    And  "Treaty" is deleted with strikeout symbol in the text box
    When mouseHover and click on show all action button and click on edit button of citation 2
    Then ck editor window is displayed
    And  get text from ck editor text box
    When click on close button of ck editor
    Then ck editor window is not displayed
    When click on preamble text present in TOC
    When select content in citation 2
    Then comment, suggest and highlight buttons are not displayed
    When click on annotation pop up button
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
    When click on "Preamble" link in navigation pane
    And  click on the first preamble formula
    Then comment, suggest and highlight buttons are not displayed
    When select content on first preamble formula
    Then comment button is displayed
    Then suggest button is disabled
    Then highlight button is displayed
    When click on the second citation
    And  sleep for 1000 milliseconds
    And  click on toggle bar move to right
    Then comment, suggest and highlight buttons are not displayed
    When click on delete icon present in show all actions icon of citation 5
    Then "Citation has been deleted" message is displayed
    And  click on message "Citation has been deleted"
    And  citation 5 is showing as grey and strikethrough
    When click on delete icon present in show all actions icon of citation 6
    Then "Citation has been deleted" message is displayed
    And  click on message "Citation has been deleted"
    When click on "Acting in accordance with the ordinary legislat..." link in navigation pane
    And  click on toc edit button
    And  click on undelete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    When click on footnote number 2
    Then footnote number with marker 2 is showing in grey and strikethrough in footnote table
    And  there is only 1 footnote present with marker 2 in footnote table
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @LegalActScenario_Recital
  Scenario: LEOS-4146 CN Edition of elements - Browse Legal Act Recital Part
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
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    And  click on recital toggle link
    And  click on recital link present in navigation pane
    And  double click on recital 1
    Then ck editor window is displayed
    And  get text from ck editor text box
    When add "New Text" and delete "cross-border" in the ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added in the text box
    And  "cross-border" is deleted with strikeout symbol in the text box
    When mouseHover and click on show all action button and click on edit button of recital 2
    Then ck editor window is displayed
    And  get text from ck editor text box
    When click on close button of ck editor
    Then ck editor window is not displayed
    When click on recital link present in navigation pane
    When select content in recital 1
    When click on annotation pop up button
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
    When switch from iframe to main window
    When click on recital "(3) The market for cross-border parcel delivery..." present in navigation pane in legal act page
    Then recital 3 is displayed
    When select content in recital 3
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
    When click on recital "(5) In most Member States there are several pro..." present in navigation pane in legal act page
    Then recital 5 is displayed
    When click on delete icon present in show all actions icon of recital 5
    Then "Recital has been deleted" message is displayed
    And  click on message "Recital has been deleted"
    When click on recital "(5) In most Member States there are several pro..." present in navigation pane in legal act page
    Then recital 5 is displayed
    When click on toc edit button
    And  click on undelete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @singleAndDoubleDiffingOfRecitals
  Scenario: single and double diffing comparision and annotation exercise in recitals
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
######################################Start CreateProposal Part#######################################
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
######################################End CreateProposal Part#########################################
    And  "DAS Satyabrata" is added as "Author" in collaborators section
######################################Start MileStone Part############################################
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
######################################End MileStone Part##############################################
    Then OverView screen is displayed
######################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " recital changes new release...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "new release...." keyword
######################################End Renaming Proposal Part####################################
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on preamble toggle link
######################################Start Recital Part############################################
    When click on recital toggle link
    When click on element 1 of recital in legal act page
    And  double click on recital 1
    Then ck editor window is displayed
    When add "New text at the beginning. " at the beginning of the ck editor text box
    When add " New text at the end." at the end of the ck editor text box
    When remove below words from the ck editor text box
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New text at the beginning. " is added to the recital 1
    And  " New text at the end." is added to the recital 1
    And  below words are showing as grey and strikethrough in recital 1
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    When click on element 3 of recital in legal act page
    And  double click on recital 3
    Then ck editor window is displayed
    When add "New text at the beginning. " at the beginning of the ck editor text box
    When add " New text at the end." at the end of the ck editor text box
    When remove below words from the ck editor text box
      | The market |
      | low volume |
      | offered    |
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New text at the beginning. " is added to the recital 3
    And  " New text at the end." is added to the recital 3
    And  below words are showing as grey and strikethrough in recital 3
      | The market |
      | low volume |
      | offered    |
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  search button is displayed in versions pane section
    And  "Recent changes" is displayed
    Then "Milestone" is displayed
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    When tick on checkbox of milestone version "1.0.0"
    And  tick on checkbox of version "1.0.1" in recent changes
    Then "Comparing 1.0.0 and 1.0.1" is displayed
    Then "recital changes new release...." is underlined in the bill title in comparision page
    And  there is only 1 addition(s) in single diff comparision page
    When uncheck version "1.0.1" in recent changes section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of version "1.0.2" in recent changes
    Then "Comparing 1.0.0 and 1.0.2" is displayed
    Then "recital changes new release...." is underlined in the bill title in comparision page
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  there is only 4 addition(s) in single diff comparision page
    And  there is only 3 deletion(s) in single diff comparision page
    When uncheck version "1.0.2" in recent changes section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of version "1.0.3" in recent changes
    Then "Comparing 1.0.0 and 1.0.3" is displayed
    Then "recital changes new release...." is underlined in the bill title in comparision page
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 7 addition(s) in single diff comparision page
    And  there is only 6 deletion(s) in single diff comparision page
    When uncheck version "1.0.3" in recent changes section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When uncheck version "1.0.0" in milestone version
    When tick on checkbox of version "1.0.1" in recent changes
    When tick on checkbox of version "1.0.2" in recent changes
    Then "Comparing 1.0.1 and 1.0.2" is displayed
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  there is only 3 addition(s) in single diff comparision page
    And  there is only 3 deletion(s) in single diff comparision page
    When uncheck version "1.0.2" in recent changes section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of version "1.0.3" in recent changes
    Then "Comparing 1.0.1 and 1.0.3" is displayed
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 6 addition(s) in single diff comparision page
    And  there is only 6 deletion(s) in single diff comparision page
    When uncheck version "1.0.1" in recent changes section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of version "1.0.2" in recent changes
    Then "Comparing 1.0.2 and 1.0.3" is displayed
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 3 addition(s) in single diff comparision page
    And  there is only 3 deletion(s) in single diff comparision page
    When tick on checkbox of version "1.0.1" in recent changes
    Then "Comparing 1.0.1, 1.0.2 and 1.0.3" is displayed
    And  "New text at the beginning." is showing as bold in recital 1 in double diff comparision page
    And  ". New text at the end" is showing as bold in recital 1 in double diff comparision page
    And  below words are showing as bold, strikethrough in recital 1 in double diff comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in double diff comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 6 addition(s) in double diff comparision page
    And  there is only 6 deletion(s) in double diff comparision page
    When uncheck version "1.0.3" in recent changes section
    When tick on checkbox of milestone version "1.0.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.2" is displayed
    Then "recital changes new release...." is added in the bill title in double diff comparision page
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in double diff comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  there is only 4 addition(s) in double diff comparision page
    And  there is only 3 deletion(s) in double diff comparision page
    When uncheck version "1.0.1" in recent changes section
    When tick on checkbox of version "1.0.3" in recent changes
    Then "Comparing 1.0.0, 1.0.2 and 1.0.3" is displayed
    Then "recital changes new release...." is added in the bill title in double diff comparision page
    And  "New text at the beginning." is showing as bold in recital 1 in double diff comparision page
    And  ". New text at the end" is showing as bold in recital 1 in double diff comparision page
    And  below words are showing as bold, strikethrough in recital 1 in double diff comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in double diff comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 7 addition(s) in double diff comparision page
    And  there is only 6 deletion(s) in double diff comparision page
    When uncheck version "1.0.2" in recent changes section
    When tick on checkbox of version "1.0.1" in recent changes
    Then "Comparing 1.0.0, 1.0.1 and 1.0.3" is displayed
    Then "recital changes new release...." is added in the bill title in double diff comparision page
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in double diff comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in double diff comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 7 addition(s) in double diff comparision page
    And  there is only 6 deletion(s) in double diff comparision page
    When click on navigation pane toggle link
    When click on element 3 of recital in legal act page
    And  double click on recital 3
    Then ck editor window is displayed
    When add " [" at the end of the ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  " [" is added to the recital 3
    And  "Comparing 1.0.0, 1.0.1 and 1.0.4" is displayed
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    And  "1.0.3" version present in recent changes section is unchecked
    And  "1.0.4" version present in recent changes section is checked
    Then "recital changes new release...." is added in the bill title in double diff comparision page
    And  "New text at the beginning." is showing as bold and underlined in recital 1 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 1 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 1 in double diff comparision page
      | medium-sized |
      | The tariffs  |
      | e-commerce   |
    And  "New text at the beginning." is showing as bold and underlined in recital 3 in double diff comparision page
    And  ". New text at the end" is showing as bold and underlined in recital 3 in double diff comparision page
    And  "[" is showing as bold and underlined in recital 3 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 3 in double diff comparision page
      | The market |
      | low volume |
      | offered    |
    And  there is only 8 addition(s) in double diff comparision page
    And  there is only 6 deletion(s) in double diff comparision page
    When click on navigation pane toggle link
    When click on element 4 of recital in legal act page
    And  double click on recital 4
    Then ck editor window is displayed
    When replace content "In order to improve the affordability of cross-border parcel delivery services, especially for users in remote or sparsely populated areas, it is necessary to improve the transparency of public lists of tariffs for a limited set of cross-border parcel delivery services. Making cross-border prices more transparent and easily comparable across the European Union should encourage the reduction of unreasonable differences between tariffs." with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  ". Making cross-border prices more transparent and easily comparable across the European Union should encourage the reduction of unreasonable" is added to the recital 4
    And  "tariffs" is added to the recital 4
    And  below words are showing as grey and strikethrough in recital 4
      | offered by universal service providers, which are mostly used by small and medium-sized enterprises and individuals. Transparency of public lists is also necessary to address the issue of high tariffs of cross-border delivery services and to reduce, where applicable, unjustified tariff |
      | national and cross-border parcel delivery services                                                                                                                                                                                                                                             |
    And  "Comparing 1.0.0, 1.0.1 and 1.0.5" is displayed
    And  ". Making cross-border prices more transparent and easily comparable across the European Union should encourage the reduction of unreasonable" is showing as bold and underlined in recital 4 in double diff comparision page
    And  "tariffs" is showing as bold and underlined in recital 4 in double diff comparision page
    And  below words are showing as bold, strikethrough and underlined in recital 4 in double diff comparision page
      | offered by universal service providers, which are mostly used by small and medium-sized enterprises and individuals. Transparency of public lists is also necessary to address the issue of high tariffs of cross-border delivery services and to reduce, where applicable, unjustified tariff |
      | national and cross-border parcel delivery services                                                                                                                                                                                                                                             |
    And  there is only 10 addition(s) in double diff comparision page
    And  there is only 8 deletion(s) in double diff comparision page
    When click on toc edit button
    Then cancel button in navigation pane is displayed and enabled
    When click on element 5 of recital in legal act page
    Then selected element section is displayed
    When click on delete button present in selected element section
    When click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  content of recital 5 is greyed and strikethrough i.e. soft deleted
    And  "Comparing 1.0.0, 1.0.1 and 1.0.6" is displayed
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  search button is displayed in versions pane section
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    And  "1.0.4" version present in recent changes section is unchecked
    And  "1.0.6" version present in recent changes section is checked
    And  content of recital 5 is bold, underlined and strikethrough in double diff comparision page
    And  there is only 10 addition(s) in double diff comparision page
    And  there is only 9 deletion(s) in double diff comparision page
    When uncheck version "1.0.0" in milestone version
    Then "Comparing 1.0.1 and 1.0.6" is displayed
    And  content of recital 5 is bold, underlined and strikethrough in single diff comparision page
    And  there is only 9 addition(s) in single diff comparision page
    And  there is only 9 deletion(s) in single diff comparision page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on navigation pane toggle link
    Then toc editing button is available
    When click on element 6 of recital in legal act page
    When click on insert before icon present in show all actions icon of recital 6
    Then recital "5a" is added to the bill in legal act page
    And  content of recital "5a" is showing bold in legal act live page
    When double click on recital 6
    Then ck editor window is displayed
    When replace content "In each Member State, there are usually more providers of domestic parcel delivery services than there are cross-border. Universal service providers in the Member State of origin frequently use other universal service providers in the Member State of destination for cross-border deliveries, especially in case of small volumes posted by small and medium sized enterprises and individuals. In that context universal service providers should enter into multilateral and/or bilateral agreements concerning remuneration for services they provide each other in case of cross-border parcel delivery services. Equal and non-discriminatory third party access to such multilateral agreements should encourage competition, benefit consumers and result in a more efficient use of existing networks, particularly in rural areas." with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    When click on element 6 of recital in legal act page
    Then content of recital "5a" is showing bold in legal act live page
    When click on element 7 of recital in legal act page
    When click on insert before icon present in show all actions icon of recital 7
    Then recital "5b" is added to the bill in legal act page
    And  content of recital "5b" is showing bold in legal act live page
    When click on recital "5b" present in navigation pane in legal act page
    And  double click on recital 7
    Then ck editor window is displayed
    When replace content "Universal service providers refers to postal operators that provide a universal postal service or parts thereof within a specific Member State. Universal service providers who operate in more than one Member State should be classified as a universal service provider only in the Member State(s) in which they provide a universal postal service." with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    When click on element 6 of recital in legal act page
    Then content of recital "5b" is showing bold in legal act live page
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    When tick on checkbox of version "1.0.8" in recent changes
    And  tick on checkbox of version "1.0.1 Metadata updated" in recent changes
    And  tick on checkbox of milestone version "1.0.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.8" is displayed
    And  content of recital "5a" is bold, underlined in double diff comparision page
    And  there is only 11 addition(s) in double diff comparision page
    And  there is only 9 deletion(s) in double diff comparision page
    When uncheck version "1.0.0" in milestone version
    Then "Comparing 1.0.1 and 1.0.8" is displayed
    And  content of recital "5a" is bold, underlined in single diff comparision page
    And  there is only 10 addition(s) in single diff comparision page
    And  there is only 9 deletion(s) in single diff comparision page
    When uncheck version "1.0.8" in recent changes section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of version "1.0.10" in recent changes
    Then "Comparing 1.0.1 and 1.0.10" is displayed
    And  content of recital "5b" is bold, underlined in single diff comparision page
    And  there is only 11 addition(s) in single diff comparision page
    And  there is only 9 deletion(s) in single diff comparision page
    When tick on checkbox of milestone version "1.0.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.10" is displayed
    And  content of recital "5b" is bold, underlined in double diff comparision page
    And  there is only 12 addition(s) in double diff comparision page
    And  there is only 9 deletion(s) in double diff comparision page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on navigation pane toggle link
    When click on toc edit button
    Then cancel button in navigation pane is displayed and enabled
    And  selected element section is displayed
    When click on cross symbol of the selected element
    Then selected element section is not displayed
    When click on recital "(7)" present in navigation pane in legal act page
    Then selected element section is displayed
    When click on delete button present in selected element section
    When click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  content of recital 9 is greyed and strikethrough i.e. soft deleted
    When click on recital "(8)" present in navigation pane in legal act page
    And  double click on recital 10
    Then ck editor window is displayed
    When replace content "Therefore, it is important to provide a clear definition of parcels and parcel delivery services and to specify which postal items are covered by these definitions. An estimated 80 % of addressed postal items generated by e-commerce today weigh less than two kilograms, and are often processed in the letter-post mail stream. It is important that those postal items are subject to this Regulation, notably to the requirements on the transparency of tariffs and the assessment of their affordability. It is assumed that postal items over 20mm thick are more likely to contain goods than correspondence and are therefore defined as parcels. In line with consistent practice, parcels weigh up to 31.5 kg, as heavier items cannot be handled by a single average individual without mechanical aids and this activity is part of the freight transport and logistic sector. Postal services consisting only of correspondence should not fall under the scope of parcel delivery services." with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are added to recital 10
      | parcels and                                                                                                                                            |
      | these definitions. An estimated 80 % of addressed                                                                                                      |
      | generated by e-commerce today weigh less than two kilograms, and are often processed in the letter-post mail stream. It is important that those postal |
      | are subject to this Regulation, notably to the requirements on the transparency of tariffs and the assessment                                          |
      | affordability. It is assumed that postal items over 20mm thick are more likely to contain                                                              |
      | than correspondence and are                                                                                                                            |
      | defined as parcels.                                                                                                                                    |
      | parcels weigh                                                                                                                                          |
      | and this activity is part of the freight transport and logistic sector. Postal services consisting only of correspondence                              |
      | not fall under                                                                                                                                         |
    And  below words are showing as grey and strikethrough in recital 10
      | that definition. This concerns in particular                                                                                                                                                                                                             |
      | , other than                                                                                                                                                                                                                                             |
      | of correspondence, which because                                                                                                                                                                                                                         |
      | weight are commonly used for sending                                                                                                                                                                                                                     |
      | and merchandise. This Regulation should                                                                                                                                                                                                                  |
      | cover,                                                                                                                                                                                                                                                   |
      | postal items weighing                                                                                                                                                                                                                                    |
      | . In line with current practice and Directive 97/67/EC, each step in the postal chain, i.e. clearance, sorting and delivery should be considered parcel delivery services. Transport alone that is not undertaken in conjunction with one of those steps |
      | fall outside                                                                                                                                                                                                                                             |
      | as it can in this case be assumed that this activity is part of the transport sector                                                                                                                                                                     |
    When click on recital "(8)" present in navigation pane in legal act page
    When click on insert after icon present in show all actions icon of recital 10
    Then "A new recital has been inserted" message is displayed
    And  click on message "A new recital has been inserted"
    When click on recital "(8a)" present in navigation pane in legal act page
    Then recital "(8a)" is added to the bill in legal act page
    When double click on recital 11
    Then ck editor window is displayed
    When replace content "In line with current practice and Directive 97/67/EC, each step in the postal delivery chain alone, i.e. clearance, sorting and delivery should be considered parcel delivery services, including when provided by express and courier service providers, as well as consolidators. Providers of parcel delivery services using alternative business models, for example those drawing on the collaborative economy and e-commerce platforms, are also subject to this Regulation, if they provide at least one of these steps in the postal delivery chain. Transport alone that is not undertaken in conjunction with one of those steps should fall outside the scope of parcel delivery services as it should in this case be assumed that this activity is part of the transport sector." with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    When click on recital "(8a)" present in navigation pane in legal act page
    Then content of recital "(8a)" is showing bold in legal act live page
    And  content of recital "(8a)" contains below hyperlink(s)
      | Directive 97/67/EC |
    When click on recital "(9)" present in navigation pane in legal act page
    And  double click on recital 12
    Then ck editor window is displayed
    When replace content "Terminal rates are based on multilateral and bilateral agreements between universal service providers and remunerate the destination universal service provider for the costs of the service provided to the originating universal service provider. Terminal rates should be defined in such a way that they include both terminal dues, as defined in point 15 of Article 2 of Directive 97/67/EC that are applied for letter mail items, inward land rates i.e. payments between universal service providers for the delivery of incoming parcels and any other rates as agreed by the parties for the remuneration of the costs of the delivery service provider for the provision of the parcel delivery service to the originating universal service provider." with the existing content in ck editor text box
    And  click on recital 12 in legal act page
    Then ck editor window is not displayed
    And  below words are added to recital 12
      | remunerate                                                                                                                                                                                                     |
      | they include                                                                                                                                                                                                   |
      | ,                                                                                                                                                                                                              |
      | i.e. payments between universal service providers for the delivery of incoming                                                                                                                                 |
      | and any other rates as agreed by the parties for the remuneration of the costs of the delivery service provider for the provision of the parcel delivery service to the originating universal service provider |
    And  below words are showing as grey and strikethrough in recital 12
      | ensure that         |
      | is remunerated      |
      | it includes         |
      | and                 |
      | that are applied to |
    And  content of recital "(9)" contains below hyperlink(s)
      | point 15 of Article 2 of Directive 97/67/EC |
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    When tick on checkbox of version "1.0.15" in recent changes
    And  tick on checkbox of version "1.0.1 Metadata" in recent changes
    And  tick on checkbox of milestone version "1.0.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.15" is displayed
    And  content of recital 9 is bold, underlined and strikethrough in double diff comparision page
    And  below words are showing as bold and underlined in recital 10 in double diff comparision page
      | parcels and                                                                                                                                            |
      | these definitions. An estimated 80 % of addressed                                                                                                      |
      | generated by e-commerce today weigh less than two kilograms, and are often processed in the letter-post mail stream. It is important that those postal |
      | are subject to this Regulation, notably to the requirements on the transparency of tariffs and the assessment                                          |
      | affordability. It is assumed that postal items over 20mm thick are more likely to contain                                                              |
      | than correspondence and are                                                                                                                            |
      | defined as parcels.                                                                                                                                    |
      | parcels weigh                                                                                                                                          |
      | and this activity is part of the freight transport and logistic sector. Postal services consisting only of correspondence                              |
      | not fall under                                                                                                                                         |
    And  below words are showing as bold, strikethrough and underlined in recital 10 in double diff comparision page
      | that definition. This concerns in particular                                                                                                                                                                                                             |
      | , other than                                                                                                                                                                                                                                             |
      | of correspondence, which because                                                                                                                                                                                                                         |
      | weight are commonly used for sending                                                                                                                                                                                                                     |
      | and merchandise. This Regulation should                                                                                                                                                                                                                  |
      | cover,                                                                                                                                                                                                                                                   |
      | postal items weighing                                                                                                                                                                                                                                    |
      | . In line with current practice and Directive 97/67/EC, each step in the postal chain, i.e. clearance, sorting and delivery should be considered parcel delivery services. Transport alone that is not undertaken in conjunction with one of those steps |
      | fall outside                                                                                                                                                                                                                                             |
      | as it can in this case be assumed that this activity is part of the transport sector                                                                                                                                                                     |
    And  content of recital "(8a)" is bold, underlined in double diff comparision page
    And  below words are showing as bold and underlined in recital 12 in double diff comparision page
      | remunerate                                                                                                                                                                                                     |
      | they include                                                                                                                                                                                                   |
      | ,                                                                                                                                                                                                              |
      | i.e. payments between universal service providers for the delivery of incoming                                                                                                                                 |
      | and any other rates as agreed by the parties for the remuneration of the costs of the delivery service provider for the provision of the parcel delivery service to the originating universal service provider |
    And  below words are showing as bold, strikethrough and underlined in recital 12 in double diff comparision page
      | ensure that         |
      | is remunerated      |
      | it includes         |
      | and                 |
      | that are applied to |
    And  there is only 30 addition(s) in double diff comparision page
    And  there is only 27 deletion(s) in double diff comparision page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version                  |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | Import                                 |
      | Import from the Official Journal       |
      | View                                   |
      | See user guidance                      |
      | See navigation pane                    |
    When click on save this version link present in Actions menu
    Then "Save this version" window is displayed
    When provide "Recitals for 13096" in the title text box of save this version window
    And  click on save button in save this version window
    Then "The version has been saved" message is displayed
    And  click on message "The version has been saved"
    And  No changes after last version is displayed under recent changes section
    And  "Version 1.1.0" is displayed under version pane
    When click on show modifications button present under version "Version 1.1.0" in version pane
    Then hide modifications button is showing for version "Version 1.1.0" in version pane
    And  below minor versions are showing under version "Version 1.1.0" in version pane
      | 1.0.1 Metadata updated            |
      | 1.0.2 Recital (1) updated         |
      | 1.0.3 Recital (3) updated         |
      | 1.0.4 Recital (3) updated         |
      | 1.0.5 Recital (4) updated         |
      | 1.0.6 Document structure updated  |
      | 1.0.7 inserted                    |
      | 1.0.8 Recital (5a) updated        |
      | 1.0.9 inserted                    |
      | 1.0.10 Recital (5b) updated       |
      | 1.0.11 Document structure updated |
      | 1.0.12 Recital (8) updated        |
      | 1.0.13 inserted                   |
      | 1.0.14 Recital (8a) updated       |
      | 1.0.15 Recital (9) updated        |
    And  close the browser

  @singleAndDoubleDiffingOfArticles
  Scenario: single and double diffing comparision and annotation exercise in articles
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
######################################Start CreateProposal Part#######################################
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
######################################End CreateProposal Part#########################################
    And  "DAS Satyabrata" is added as "Author" in collaborators section
######################################Start MileStone Part############################################
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
######################################End MileStone Part##############################################
    Then OverView screen is displayed
######################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " article changes new release...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "new release...." keyword
######################################End Renaming Proposal Part####################################
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
######################################Start Article Part############################################
    And  click on "Article 1 - Scope" link in navigation pane
    Then article 1 is displayed in legal act
    When click on edit button of article 1 heading
    Then ck editor window is displayed
    When replace "Subject matter" with the existing content in ck editor text box for akn heading element origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "Subject matter" is showing as bold in heading of article 1 in legal act page
    And  "Scope" is showing as grey and strikethrough in heading of article 1 in legal act page
    When click on "(a) the regulatory oversight related to parcel ..." link in navigation pane
    Then point 1 of paragraph 1 of article 1 is displayed
    When double click on point 1 of paragraph 1 of article 1
    Then ck editor window is displayed
    And  the content "This Regulation establishes specific rules, in addition to the rules set out in Directive 97/67/EC, concerning:" is displayed for subparagraph of paragraph 1 of article 1
    When click on close button of ck editor
    Then ck editor window is not displayed
    When click on "(b) the transparency of tariffs and terminal ra..." link in navigation pane
    Then point 2 of paragraph 1 of article 1 is displayed
    When double click on point 2 of paragraph 1 of article 1
    Then ck editor window is displayed
    When replace content "the transparency of tariffs and terminal rates for certain cross-border parcel delivery services and the assessment of the affordability of related cross-border tariffs;" with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in point 2 of paragraph 1 of article 1
      | related |
    And  below words are showing as grey and strikethrough in point 2 of paragraph 1 of article 1
      | certain |
    When click on "(c) transparent and non-discriminatory access t..." link in navigation pane
    Then article 1 is displayed in legal act
    Then point 3 of paragraph 1 of article 1 is displayed
    When double click on point 3 of paragraph 1 of article 1
    Then ck editor window is displayed
    When replace content "transparent and non-discriminatory access to multilateral cross-border parcel delivery service agreements." with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in point 3 of paragraph 1 of article 1
      | multilateral       |
      | service agreements |
    And  below words are showing as grey and strikethrough in point 3 of paragraph 1 of article 1
      | certain                        |
      | services and/or infrastructure |
    When click on "(a) \"parcel delivery services\" means services i..." link in navigation pane
    Then point 1 of paragraph 2 of article 2 is displayed
    When click on insert before icon present in show all actions icon of point 1 of paragraph 2 of article 2
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    And  a point with number "(-a)" is added in paragraph 2 of article 2
    When double click on point 1 of paragraph 2 of article 2
    Then ck editor window is displayed
    When replace content "\"parcel\" means a postal item with a thickness exceeding 20 mm and a weight not exceeding 31,5 kg;" with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  content of point "(-a)" of paragraph 2 of article 2 is showing bold in legal act live page
    When click on "(a) \"parcel delivery services\" means services i..." link in navigation pane
    Then point 2 of paragraph 2 of article 2 is displayed
    When double click on point 2 of paragraph 2 of article 2
    Then ck editor window is displayed
    When replace content "\"parcel delivery services\" means services involving the clearance, sorting, transport or distribution of parcels; transport alone shall not be considered a parcel delivery service;" with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in point 2 of paragraph 2 of article 2
      | parcels |
    And  below words are showing as grey and strikethrough in point 2 of paragraph 2 of article 2
      | postal items other than items of correspondence                                             |
      | delivery of such items exceeding 31,5 kg shall not be considered a parcel delivery service; |
    When click on "1. All parcel delivery service providers shall ..." link in navigation pane
    Then paragraph 1 of article 3 is displayed
    When double click on paragraph 1 of article 3
    Then ck editor window is displayed
    When replace content "All parcel delivery service providers shall submit the following information to the national regulatory authority of the Member State in which they are established: New Text is added at the end." with the existing content in ck editor text box of a paragraph inside article origin from ec
    And  click on close button of ck editor
    Then confirm cancel editing window is displayed
    When click on ok button in confirm cancel editing window
    Then there are no changes in paragraph 1 of article 3
    When click on "(b) the nature of the services offered by the p..." link in navigation pane
    Then point 2 of paragraph 1 of article 3 is displayed
    And  sleep for 1000 milliseconds
    When double click on point 2 of paragraph 1 of article 3
    Then ck editor window is displayed
    When replace content "the characteristics of the parcel delivery services offered by the provider;" with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in point 2 of paragraph 1 of article 3
      | characteristics |
      | parcel delivery |
    And  below words are showing as grey and strikethrough in point 2 of paragraph 1 of article 3
      | nature |
    When double click on point 3 of paragraph 1 of article 3
    Then ck editor window is displayed
    When replace content "the provider's general terms and conditions." with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in point 3 of paragraph 1 of article 3
      | terms and conditions |
    And  below words are showing as grey and strikethrough in point 3 of paragraph 1 of article 3
      | conditions of sale, including a detailed description of the complaints procedure |
    When click on "2. In case of any change concerning information..." link in navigation pane
    Then paragraph 2 of article 3 is displayed
    And  sleep for 1000 milliseconds
    When double click on paragraph 2 of article 3
    Then ck editor window is displayed
    When replace content "In case of any change concerning information referred to in paragraph 1, parcel delivery service providers shall inform the national regulatory authority of this change within 30 days." with the existing content in ck editor text box of a paragraph inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  below words are showing as bold in paragraph 2 of article 3
      | paragraph 1 |
    And  below words are showing as grey and strikethrough in paragraph 2 of article 3
      | the first subparagraph |
    When click on "(c) the number of postal items other than items..." link in navigation pane
    Then point 3 of paragraph 3 of article 3 is displayed
    And  sleep for 1000 milliseconds
    When click on insert after icon present in show all actions icon of point 3 of paragraph 3 of article 3
    Then "A new point has been inserted" message is displayed
    And  click on message "A new point has been inserted"
    When double click on point 4 of paragraph 3 of article 3
    Then ck editor window is displayed
    When replace content "any publicly available price list applicable on 1 January of each calendar year for parcel delivery services." with the existing content in ck editor text box of a point inside article origin from ec
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  sleep for 1000 milliseconds
    And  content of point "(ca)" of paragraph 3 of article 3 is showing bold in legal act live page
    When click on "5. The national regulatory authorities may impo..." link in navigation pane
    Then paragraph 5 of article 3 is displayed
    And  sleep for 1000 milliseconds
    When click on delete icon present in show all actions icon of paragraph 5 of article 3
    Then "Paragraph has been deleted" message is displayed
    And  click on message "Paragraph has been deleted"
    And  content of paragraph 5 of article 3 is showing as grey and strikethrough in legal act live page
    When click on "7. When no agreement is reached on the basis of..." link in navigation pane
    Then paragraph 7 of article 6 is displayed
    When click on toc edit button
    Then cancel button in navigation pane is displayed and enabled
    And  selected element section is displayed
    And  input value "Paragraph" for element Type is disabled in selected element section
    And  input value "7." for element Number is disabled in selected element section
    And  delete button is displayed and enabled in selected element section
    When click on delete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  content of paragraph 7 of article 6 is showing as grey and strikethrough in legal act live page
    When click on "7. When no agreement is reached on the basis of..." link in navigation pane
    Then paragraph 7 of article 6 is displayed
    And  sleep for 1000 milliseconds
    When click on toc edit button
    Then cancel button in navigation pane is displayed and enabled
    And  selected element section is displayed
    And  input value "Paragraph" for element Type is disabled in selected element section
    And  input value "7." for element Number is disabled in selected element section
    And  undelete button is displayed and enabled in selected element section
    When click on undelete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  content of paragraph 7 of article 6 is showing normal in legal act live page
    When click on "7. When no agreement is reached on the basis of..." link in navigation pane
    Then paragraph 7 of article 6 is displayed
    And  sleep for 1000 milliseconds
    When click on delete icon present in show all actions icon of paragraph 7 of article 6
    Then "Paragraph has been deleted" message is displayed
    And  click on message "Paragraph has been deleted"
    And  content of paragraph 7 of article 6 is showing as grey and strikethrough in legal act live page
    When click on actions hamburger icon
    When click on save this version link present in Actions menu
    Then "Save this version" window is displayed
    When provide "Articles for 13096" in the title text box of save this version window
    And  click on save button in save this version window
    Then "The version has been saved" message is displayed
    And  click on message "The version has been saved"
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  No changes after last version is displayed under recent changes section
    And  "Version 1.1.0" is displayed under version pane
    When click on navigation pane toggle link
    Then toc editing button is available
    When click on "Article 1 - Subject matter" link in navigation pane
    Then article 1 is displayed
    And  sleep for 1000 milliseconds
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "1" for element Number is disabled in selected element section
    And  input value "Subject matter" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is added to existing paragraph 1 of article 1
    When click on " This Regulation establishes specific rules, in..." link in navigation pane
    Then paragraph 1 of article 1 is displayed
    And  sleep for 1000 milliseconds
    When click on insert after icon present in show all actions icon of subparagraph 1 of paragraph 1 of article 1
    Then "A new subparagraph has been inserted" message is displayed
    And  click on message "A new subparagraph has been inserted"
    And  paragraph 2 is added to article 1 in legal act live page
    And  number "1a." is added to new paragraph 2 of article 1
    When double click on paragraph 2
    Then ck editor window is displayed
    When append content "test unnumbered to numbered and vice versa" with the existing content in ck editor text box of a paragraph inside article
    Then click on save close button of ck editor
    When click on "Article 1 - Subject matter" link in navigation pane
    Then article 1 is displayed
    And  sleep for 1000 milliseconds
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "1" for element Number is disabled in selected element section
    And  input value "Subject matter" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  num tag is not present in paragraph 1 of article 1
    And  num tag is not present in paragraph 2 of article 1
    When click on "Article 5 - Assessing affordability of tariffs" link in navigation pane
    Then article 5 is displayed
    And  sleep for 1000 milliseconds
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "5" for element Number is disabled in selected element section
    And  input value "Assessing affordability of tariffs" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 5
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 5
    And  number "3." is shown as grey and strikethrough in paragraph 3 of article 5
    And  number "4." is shown as grey and strikethrough in paragraph 4 of article 5
    And  number "5." is shown as grey and strikethrough in paragraph 5 of article 5
    When double click on point 2 of list 1 of paragraph 1 of article 5
    Then ck editor window is displayed
    And  decrease indent icon is displayed and enabled in ck editor panel
    And  data-akn number is showing as "(b)"
    When click on decrease indent icon present in ck editor panel
    Then data-akn number attribute is not present inside ck editor
    When click on save close button of ck editor
    Then "the terminal rates obtained in accordance with Article 4(3);" is showing in subparagraph 2 of paragraph 1 of article 5
    And  "(b)" is showing as grey and strikethrough in subparagraph 2 of paragraph 1 of article 5
    When double click on paragraph 2 of article 5
    Then ck editor window is displayed
    And  increase indent icon is displayed and enabled in ck editor panel
    And  data-akn number is showing as "2."
    When click on increase indent icon present in ck editor panel
    Then data-akn number attribute is not present inside ck editor
    When click on save close button of ck editor
    And  sleep for 2000 milliseconds
    Then "Where the national regulatory authority concludes that cross-border tariffs referred to in paragraph 1 are not affordable, it shall request further necessary information and/or justification in relation to the level of those tariffs from the universal service provider." is showing in subparagraph 3 of paragraph 1 of article 5
    And  "2." is showing as grey and strikethrough in subparagraph 3 of paragraph 1 of article 5
    When double click on subparagraph 3 of paragraph 1 of article 5
    Then ck editor window is displayed
    And  decrease indent icon is displayed and enabled in ck editor panel
    And  data-akn number attribute is not present inside ck editor
    When click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    And  sleep for 2000 milliseconds
    Then "Where the national regulatory authority concludes that cross-border tariffs referred to in paragraph 1 are not affordable, it shall request further necessary information and/or justification in relation to the level of those tariffs from the universal service provider." is showing in paragraph 2 of article 5
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 5
#######################################End Article Part############################################
    When click on actions hamburger icon
    When click on save this version link present in Actions menu
    Then "Save this version" window is displayed
    When provide "Diversion before 13096" in the title text box of save this version window
    And  click on save button in save this version window
    Then "The version has been saved" message is displayed
    And  click on message "The version has been saved"
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    And  No changes after last version is displayed under recent changes section
    And  "Version 1.2.0" is displayed under version pane
    When click on hamburger icon of version "Version 1.1.0" in versions pane
    Then below options are displayed in versions pane section
      | View this version      |
      | Revert to this version |
      | Download this version  |
      | Change to base version |
    When click on the option "Revert to this version" showing in versions pane
    Then "Restore version" window is displayed
    When click on "Revert" button
    Then "1.2.1 Version restored from 1.1.0" notification is displayed under recent changes section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of milestone version "Version 1.0.0"
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When click on show modifications button present under version "Version 1.1.0" in version pane
    Then hide modifications button is showing for version "Version 1.1.0" in version pane
    When tick on checkbox of minor version "1.0.1 Metadata updated" in major version "Version 1.1.0"
    Then "Comparing 1.0.0 and 1.0.1" is displayed
    And  "article changes new release...." is underlined in the bill title in comparision page
    And  there is only 1 addition(s) in article section in single diff comparision page
    When tick on checkbox of minor version "1.0.4 Article 1, first paragraph, point (c) updated" in major version "Version 1.1.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.4" is displayed
    And  "article changes new release...." is added in the bill title in double diff comparision page
    And  "Subject matter" is showing as bold and underlined in heading of article 1 in double diff comparision page
    And  "Scope" is showing as bold, strikethrough and underlined in heading of article 1 in double diff comparision page
    And  below words are showing as bold and underlined in point 2 of paragraph 1 of article 1 in double diff comparision page
      | related |
    And  below words are showing as bold, strikethrough and underlined in point 2 of paragraph 1 of article 1 in double diff comparision page
      | certain |
    And  below words are showing as bold and underlined in point 3 of paragraph 1 of article 1 in double diff comparision page
      | multilateral       |
      | service agreements |
    And  below words are showing as bold, strikethrough and underlined in point 3 of paragraph 1 of article 1 in double diff comparision page
      | certain                        |
      | services and/or infrastructure |
    And  there is only 5 addition(s) in article section in double diff comparision page
    And  there is only 4 deletion(s) in article section in double diff comparision page
    When untick on checkbox of minor version "1.0.4 Article 1, first paragraph, point (c) updated" in major version "Version 1.1.0"
    Then "Comparing 1.0.0 and 1.0.1" is displayed
    When tick on checkbox of minor version "1.0.7 Article 2(2), point (a) updated" in major version "Version 1.1.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.7" is displayed
    And  content of point "(-a)" of paragraph 2 of article 2 is showing bold and underlined in double diff comparision page
    And  below words are showing as bold and underlined in point 2 of paragraph 2 of article 2 in double diff comparision page
      | parcels |
    And  below words are showing as bold, strikethrough and underlined in point 2 of paragraph 2 of article 2 in double diff comparision page
      | postal items other than items of correspondence                                             |
      | delivery of such items exceeding 31,5 kg shall not be considered a parcel delivery service; |
    And  there is only 7 addition(s) in article section in double diff comparision page
    And  there is only 6 deletion(s) in article section in double diff comparision page
    When untick on checkbox of minor version "1.0.7 Article 2(2), point (a) updated" in major version "Version 1.1.0"
    Then "Comparing 1.0.0 and 1.0.1" is displayed
    When tick on checkbox of minor version "1.0.13 Article 3(5) deleted" in major version "Version 1.1.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.13" is displayed
    And  below words are showing as bold and underlined in point 2 of paragraph 1 of article 3 in double diff comparision page
      | characteristics |
      | parcel delivery |
    And  below words are showing as bold, strikethrough and underlined in point 2 of paragraph 1 of article 3 in double diff comparision page
      | nature |
    And  below words are showing as bold and underlined in point 3 of paragraph 1 of article 3 in double diff comparision page
      | terms and conditions |
    And  below words are showing as bold, strikethrough and underlined in point 3 of paragraph 1 of article 3 in double diff comparision page
      | conditions of sale, including a detailed description of the complaints procedure |
    And  below words are showing as bold and underlined in paragraph 2 of article 3 in double diff comparision page
      | paragraph 1 |
    And  below words are showing as bold, strikethrough and underlined in paragraph 2 of article 3 in double diff comparision page
      | the first subparagraph |
    And  content of point "(ca)" of paragraph 3 of article 3 is showing bold and underlined in double diff comparision page
    And  content of paragraph 5 of article 3 is showing as bold, strikethrough and underlined in double diff comparision page
    And  there is only 12 addition(s) in article section in double diff comparision page
    And  there is only 10 deletion(s) in article section in double diff comparision page
    When untick on checkbox of minor version "1.0.13 Article 3(5) deleted" in major version "Version 1.1.0"
    Then "Comparing 1.0.0 and 1.0.1" is displayed
    When tick on checkbox of minor version "1.0.16 Article 6(7) deleted" in major version "Version 1.1.0"
    Then "Comparing 1.0.0, 1.0.1 and 1.0.16" is displayed
    And  content of paragraph 7 of article 6 is showing as bold, strikethrough and underlined in double diff comparision page
    And  there is only 12 addition(s) in article section in double diff comparision page
    And  there is only 11 deletion(s) in article section in double diff comparision page
    When untick on checkbox of minor version "1.0.16 Article 6(7) deleted" in major version "Version 1.1.0"
    And  untick on checkbox of minor version "1.0.1 Metadata updated" in major version "Version 1.1.0"
    Then "Choose 2 versions to compare, or 3 to double-compare" is displayed
    When tick on checkbox of major version "Version 1.1.0"
    And  tick on checkbox of major version "Version 1.2.0"
    Then "Comparing 1.0.0, 1.1.0 and 1.2.0" is displayed
    And  "article changes new release...." is added in the bill title in double diff comparision page
    And  "Subject matter" is showing as bold in heading of article 1 in double diff comparision page
    And  "Scope" is showing as bold and strikethrough in heading of article 1 in double diff comparision page
    And  below words are showing as bold in point 2 of paragraph 1 of article 1 in double diff comparision page
      | related |
    And  below words are showing as bold and strikethrough in point 2 of paragraph 1 of article 1 in double diff comparision page
      | certain |
    And  below words are showing as bold in point 3 of paragraph 1 of article 1 in double diff comparision page
      | multilateral       |
      | service agreements |
    And  below words are showing as bold and strikethrough in point 3 of paragraph 1 of article 1 in double diff comparision page
      | certain                        |
      | services and/or infrastructure |
    And  below words are showing as bold and underlined in paragraph 2 of article 1 in double diff comparision page
      | Text...test unnumbered to numbered and vice versa |
    And  content of point "(-a)" of paragraph 2 of article 2 is showing bold in double diff comparision page
    And  below words are showing as bold in point 2 of paragraph 2 of article 2 in double diff comparision page
      | parcels |
    And  below words are showing as bold and strikethrough in point 2 of paragraph 2 of article 2 in double diff comparision page
      | postal items other than items of correspondence                                             |
      | delivery of such items exceeding 31,5 kg shall not be considered a parcel delivery service; |
    And  below words are showing as bold in point 2 of paragraph 1 of article 3 in double diff comparision page
      | characteristics |
      | parcel delivery |
    And  below words are showing as bold and strikethrough in point 2 of paragraph 1 of article 3 in double diff comparision page
      | nature |
    And  below words are showing as bold in point 3 of paragraph 1 of article 3 in double diff comparision page
      | terms and conditions |
    And  below words are showing as bold and strikethrough in point 3 of paragraph 1 of article 3 in double diff comparision page
      | conditions of sale, including a detailed description of the complaints procedure |
    And  below words are showing as bold in paragraph 2 of article 3 in double diff comparision page
      | paragraph 1 |
    And  below words are showing as bold and strikethrough in paragraph 2 of article 3 in double diff comparision page
      | the first subparagraph |
    And  content of point "(ca)" of paragraph 3 of article 3 is showing bold in double diff comparision page
    And  content of paragraph 5 of article 3 is showing as bold and strikethrough in double diff comparision page
    And  content of paragraph 7 of article 6 is showing as bold and strikethrough in double diff comparision page
    And  there is only 14 addition(s) in article section in double diff comparision page
    And  there is only 11 deletion(s) in article section in double diff comparision page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @LegalActScenario_Article
  Scenario: LEOS-4146 CN Edition of elements - Browse Legal Act Article Part
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council/PROP_ACT_667256649469696816.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
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
      | Paragraph    |
      | Subparagraph |
      | Point (a)    |
    When click on article 1 in navigation pane
    Then selected element section is displayed
    Then input value "Article" for element Type is disabled in selected element section
    Then input value "1" for element Number is disabled in selected element section
    Then input value "Scope" for element Heading is editable in selected element section
    Then Paragraph Numbering has below options
      | Numbered   |
      | Unnumbered |
    And  both the options of Paragraph Numbering are editable
    Then delete button is displayed and enabled in selected element section
    When click on cross symbol of the selected element
    Then selected element section is not displayed
    When click on "cancel" button present in navigation pane
    Then save button in navigation pane is not displayed
    Then save and close button in navigation pane is not displayed
    Then cancel button in navigation pane is not displayed
    Then elements section attached to navigation pane is not displayed
    When click on article 1 in navigation pane
    And  double click on subparagraph 1 of paragraph 1 of article 1
    Then ck editor window is displayed
    And  get text from ck editor li text box
    When add "New Text" and delete "establishes " in the ck editor li text box
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  "New Text" is added in the text box
    And  "establishes" is deleted with strikeout symbol in the text box
    When mousehover and click on show all action button and click on edit button of point 1
    Then ck editor window is displayed
    And  get text from ck editor li text box
    When click on close button of ck editor
    Then ck editor window is not displayed
    When click on article 1 in navigation pane
    Then article 1 is displayed
    When select content "parcel delivery services" from point 1 of list 1 of paragraph 1 of article 1
    Then comment, suggest and highlight buttons are not displayed
    When click on annotation pop up button
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
    When select content in point 2 of list 1 of paragraph 1 of article 1 in legal act page
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
    When select content in point 3 of list 1 of paragraph 1 of article 1 in legal act page
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
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on "Article 3 - Provision of information" link in navigation pane
    Then article 3 is displayed
    When click on delete icon present in show all actions icon of paragraph 1 of article 3
    Then "Subparagraph has been deleted" message is displayed
    And  click on message "Subparagraph has been deleted"
    When click on toc edit button
    And  click on "1. All parcel delivery service providers shall ..." link in navigation pane
    And  click on undelete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    When click on "Article 7 - Penalties" link in navigation pane
    Then article 7 is displayed
    When click on delete icon present in show all actions icon of paragraph 1 of article 7
    Then "Paragraph has been deleted" message is displayed
    And  click on message "Paragraph has been deleted"
    When click on toc edit button
    And  click on "Member States shall lay down the rules on the p..." link in navigation pane
    And  click on undelete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @exportEConsiliumWithAllTextAndAnnotation
  Scenario: LEOS-XXXX export to eConsilium in legal act page with all text and annotations
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
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    And  navigation pane is displayed
    And  legal act content is displayed
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version                  |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | Import                                 |
      | Import from the Official Journal       |
      | View                                   |
      | See user guidance                      |
      | See navigation pane                    |
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

  @spaceTestingInArticleParagraph
  Scenario: LEOS-5659 LEOS-5660 Adding space(s) in paragraph of article
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
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on title of the mandate
    And  append " space addition testing...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "space addition testing...." keyword
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "4. The Commission shall, by means of an impleme..." link in navigation pane
    Then paragraph 4 of article 3 is displayed
    When double click on paragraph 4 of article 3
    Then ck editor window is displayed
    When replace content "The Commission shall, by means    of an implementing act, establish a form for the submission of the information referred to in paragraph 1 of this Article. Those implementing acts shall be adopted in accordance with the examination procedure referred to in Article 9." with the existing content in ck editor text box of a paragraph inside article origin from ec
    And  click on save close button of ck editor
    Then no words are showing as bold in paragraph 4 of article 3
    When click on versions pane accordion
    Then compare versions button is displayed in versions pane section
    When click on compare versions button present in versions pane section
    Then "Choose 2 versions to compare, or 3 to double-compare" message is displayed
    When click on show more button in recent changes section inside version pane
    Then show less button is showing in recent changes section inside version pane
    When tick on checkbox of major version "Version 0.1.0"
    And  tick on checkbox of version "0.1.2 Article 3(4) updated" in recent changes
    Then "Comparing 0.1.0 and 0.1.2" message is displayed
    And  no word is shown as bold and underlined in paragraph 4 of article 3 in single diff comparison page
    When tick on checkbox of version "0.1.1 Metadata updated" in recent changes
    Then "Comparing 0.1.0, 0.1.1 and 0.1.2" message is displayed
    And  no word is shown as bold and underlined in paragraph 4 of article 3 in double diff comparison page
    When click on close button present in comparision page
    Then comparision page is not displayed
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @specialCharactersTestingInRecital
  Scenario: LEOS-5664 Inserting < or & character in ck editor breaks legal act
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
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on title of the mandate
    And  append " spacial character testing...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "spacial character testing...." keyword
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on preamble toggle link
    And  click on recital toggle link
    And  click on "(1) The tariffs applicable to low volume sender..." link in navigation pane
    Then recital 1 is displayed
    When double click on recital 1
    Then ck editor window is displayed
    When append content " 0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ''-â€“â€”''!!#$%&(())*./:;?@[[][[]]\]^_``{{}|}~Â´â€˜â€˜â€™â€™â€œâ€œâ€Â¢â‚¬+<<><Â=â‰>Â±Â«Â»Ã—Ã·â‰¤â‰¥Â§Â©Â®Â°Â¶â€¦â€â„¢Ï€,~Â¡Â£Â¤Â¥Â¦Â¨ÂªÂ«Â¬Â­Â¯Â²Â³ÂµÂ·Â¸Â¹ÂºÂ»Â¼Â½Â¾Â¿Ã€ÃÃ‚ÃƒÃ„Ã…Ã†Ã‡ÃˆÃ‰ÃŠÃ‹ÃŒÃÃŽÃÃÃ‘Ã’Ã“Ã”Ã•Ã–Ã˜Ã™ÃšÃ›ÃœÃÃžÃŸÃÃ¡Ã¢Ã£Ã¤Ã¥Ã¦Ã§Ã¨Ã©ÃªÃ«Ã¬Ã­Ã®Ã¯Ã°Ã±Ã²Ã³Ã´ÃµÃ¶Ã¸Ã¹ÃºÃ»Ã¼Ã½Ã¾Ã¿" with the existing content in ck editor text box of a recital
    And  click on save close button of ck editor
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @differentScenariosWhileChangingArticleType
  Scenario: LEOS-5643 [CN] test different scenarios while changing article type from unnumbered to numbered and vice versa
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_397221491908628200.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with Create new mandate - Draft metadata page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on actions hamburger icon
    When click on "See user guidance" option
    Then below sentences are present for user guidance
      | [where necessary]                                                                                                                                                                             |
      | [where necessary]                                                                                                                                                                             |
      | [Choose between the two options above. The second option must be used where the Regulation is binding and directly applicable only in certain Member States in accordance with the Treaties.] |
    When click on actions hamburger icon
    And  click on "See user guidance" option
    Then user guidance is not present in the page
    When click on "Article 2 - Subject matter and scope" link in navigation pane
    Then article 2 is displayed
    When click on insert before icon present in show all actions icon of paragraph 1 of article 2
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "-1." is added to new paragraph 1 of article 2
    And  below words are showing as bold in paragraph 1 of article 2
      | Text... |
    When click on insert after icon present in show all actions icon of paragraph 2 of article 2
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "1a." is added to new paragraph 3 of article 2
    And  below words are showing as bold in paragraph 3 of article 2
      | Text... |
    When click on "-1. Text..." link in navigation pane
    Then paragraph 1 of article 2 is displayed
    When click on toc edit button
    Then selected element section is displayed
    When click on delete button present in selected element section
    When click on "1a. Text..." link in navigation pane
    Then paragraph 3 of article 2 is displayed
    When click on delete button present in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  num tag is not showing in bold for any numebered paragraph in article 2
    And  content is not showing in bold for any numebered paragraph in article 2
    When click on "Article 1 - Entry into force" link in navigation pane
    Then article 1 is displayed
    When click on insert after icon present in show all actions icon of paragraph 1 of article 1
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  below words are showing as bold in paragraph 2 of article 1
      | Text... |
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "1" for element Number is disabled in selected element section
    And  input value "Entry into force" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is added to existing paragraph 1 of article 1
    And  number "1a." is added to new paragraph 2 of article 1
    When click on "Article 2 - Subject matter and scope" link in navigation pane
    Then article 2 is displayed
    When click on insert after icon present in show all actions icon of paragraph 1 of article 2
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "1a." is added to new paragraph 2 of article 2
    And  below words are showing as bold in paragraph 2 of article 2
      | Text... |
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "2" for element Number is disabled in selected element section
    And  input value "Subject matter and scope" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 2
    And  number "2." is shown as grey and strikethrough in paragraph 3 of article 2
    And  number "3." is shown as grey and strikethrough in paragraph 4 of article 2
    And  number "4." is shown as grey and strikethrough in paragraph 5 of article 2
    And  number "5." is shown as grey and strikethrough in paragraph 6 of article 2
    And  num tag is not present in paragraph 2 of article 2
    When click on "Article 3 - Minimum harmonisation" link in navigation pane
    Then article 3 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "3" for element Number is disabled in selected element section
    And  input value "Minimum harmonisation" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is added to existing paragraph 1 of article 3
    When click on insert after icon present in show all actions icon of paragraph 1 of article 3
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "1a." is added to new paragraph 2 of article 3
    And  below words are showing as bold in paragraph 2 of article 3
      | Text... |
    When click on insert after icon present in show all actions icon of paragraph 2 of article 3
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "1b." is added to new paragraph 3 of article 3
    And  below words are showing as bold in paragraph 3 of article 3
      | Text... |
    When double click on paragraph 3 of article 3
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then there are only 2 paragraphs are present in article 3
    And  there are only 2 subparagraphs are present in paragraph 2 of article 3
    And  below words are showing as bold in subparagraph 2 of paragraph 2 of article 3
      | Text... |
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "3" for element Number is disabled in selected element section
    And  input value "Minimum harmonisation" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  there are only 2 paragraphs are present in article 3
    And  there are only 2 subparagraphs are present in paragraph 2 of article 3
    And  number "1." is shown as grey and strikethrough in paragraph 2 of article 3
    When click on "Article 6 - Disproportionate burden" link in navigation pane
    Then article 6 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "6" for element Number is disabled in selected element section
    And  input value "Disproportionate burden" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 6
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 6
    And  number "3." is shown as grey and strikethrough in paragraph 3 of article 6
    And  number "4." is shown as grey and strikethrough in paragraph 4 of article 6
    When click on insert after icon present in show all actions icon of paragraph 1 of article 6
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  below words are showing as bold in paragraph 2 of article 6
      | Text... |
    And  num tag is not present in paragraph 2 of article 6
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "6" for element Number is disabled in selected element section
    And  input value "Disproportionate burden" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1a." is added to new paragraph 2 of article 6
    And  below words are showing as bold in paragraph 2 of article 6
      | Text... |
    ########################## START ##### LEOS-5939 Case 2 #####################################
    When click on "2. In order to assess the extent to which compl..." link in navigation pane
    Then paragraph 3 of article 6 is displayed
    When double click on point 1 of list 1 of paragraph 3 of article 6
    Then ck editor window is displayed
    When select content "public sector" from the ck editor
    And  click on soft enter icon present in ck editor panel
    Then ck editor window is displayed
    When click on close button of ck editor
    Then ck editor window is not displayed
    Then 2 alinea exists in point 1 of list 1 of paragraph 3 of article 6
    And  alinea 1 of point 1 of list 1 of paragraph 3 of article 6 is from "ec" origin
    And  alinea 2 of point 1 of list 1 of paragraph 3 of article 6 is from "cn" origin
    And  below words are showing as normal text in alinea 1 of point 1 of list 1 of paragraph 3 of article 6
      | the size, resources and nature of the |
    And  below words are showing as grey and strikethrough in alinea 1 of point 1 of list 1 of paragraph 3 of article 6
      | public sector body concerned; and |
    And  below words are showing as bold in alinea 2 of point 1 of list 1 of paragraph 3 of article 6
      | public sector body concerned; and |
    ########################## END   ##### LEOS-5939 Case 2 #######################################
    ########################## START ##### LEOS-5939 Case 1 #######################################
#    When click on "3. Without prejudice to paragraph 1 of this Art..." link in navigation pane
#    Then paragraph 4 of article 6 is displayed
    When double click on paragraph 4 of article 6
    Then ck editor window is displayed
    When select content "extent to which compliance" from the ck editor
    And  click on soft enter icon present in ck editor panel
    Then ck editor window is displayed
    When click on close button of ck editor
    Then ck editor window is not displayed
    Then 2 subparagraph exists in paragraph 4 of article 6
    And  subparagraph 1 of paragraph 4 of article 6 is from "ec" origin
    And  subparagraph 2 of paragraph 4 of article 6 is from "cn" origin
    And  below words are showing as normal text in subparagraph 1 of paragraph 4 of article 6
      | Without prejudice to paragraph 1 of this Article, the public sector body concerned shall perform the initial assessment of the |
    And  below words are showing as grey and strikethrough in subparagraph 1 of paragraph 4 of article 6
      | extent to which compliance with the accessibility requirements set out in Article 4 imposes a disproportionate burden. |
    And  below words are showing as bold in subparagraph 2 of paragraph 4 of article 6
      | extent to which compliance with the accessibility requirements set out in Article 4 imposes a disproportionate burden. |
    ########################## END ##### LEOS-5939 Case 1 ######################################################
    ########################## START ##### LEOS-6011 Case 2 ####################################################
    When click on toc edit button
    And  click on "3. Without prejudice to paragraph 1 of this Art..." link in navigation pane
    Then selected element section is displayed
    And  input value "Paragraph" for element Type is disabled in selected element section
    And  input value "3." for element Number is disabled in selected element section
    When click on delete button present in selected element section
    Then "Delete item: confirmation" window is displayed
    And  "Selected item contains sub-items and all of them will be deleted.Are you sure you want to continue?  " message is displayed in delete item confirmation pop up window
    When click on continue button present in Delete item: confirmation pop up window
    Then "3. Without prejudice to paragraph 1 of this Art..." link is showing in grey and strikethrough in TOC
    And  click on save and close button in navigation pane
    Then "3. Without prejudice to paragraph 1 of this Art..." link is showing in grey and strikethrough in TOC
    And  content of paragraph 4 of article 6 is showing as grey and strikethrough in legal act live page
    And  number "3." is shown as grey and strikethrough in paragraph 4 of article 6
    ########################## END ##### LEOS-6011 Case 2 #######################################################
    When click on "Article 7 - Presumption of conformity with the ..." link in navigation pane
    Then article 7 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "7" for element Number is disabled in selected element section
    And  input value "Presumption of conformity with the accessibility requirements" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 7
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 7
    And  number "3." is shown as grey and strikethrough in paragraph 3 of article 7
    And  number "4." is shown as grey and strikethrough in paragraph 4 of article 7
    And  there are only 3 subparagraphs are present in paragraph 2 of article 7
    When double click on subparagraph 2 of paragraph 2 of article 7
    Then ck editor window is displayed
    When click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then num tag is not present in paragraph 3 of article 7
    And  there are only 2 subparagraphs are present in paragraph 3 of article 7
    And  no subparagraph is present in paragraph 2 of article 7
    And  the content "Where no references to the harmonised standards referred to in paragraph 1 of this Article have been published, content of mobile applications that meets the technical specifications or parts thereof shall be presumed to be in conformity with the accessibility requirements set out in Article 4 that are covered by those technical specifications or by parts thereof." is displayed for paragraph 2 of article 7
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @intendOutdentScenarios1
  Scenario: LEOS-5811,5808,5688 [CN] test different scenarios w.r.t. intend/outdent
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
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    ######################################Start Renaming Proposal Part####################################
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Intend Outend Scenarios...." keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Intend Outend Scenarios...." keyword
######################################End Renaming Proposal Part####################################
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Article 2 - Definitions" link in navigation pane
    Then article 2 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "2" for element Number is disabled in selected element section
    And  input value "Definitions" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 2
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 2
    When double click on point 1 of list 1 of paragraph 2 of article 2
    Then ck editor window is displayed
    When click on decrease indent icon present in ck editor panel
    And  click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then num tag is not present in paragraph 3 of article 2
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 2
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 2
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "2" for element Number is disabled in selected element section
    And  input value "Definitions" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is present in paragraph 1 of article 2
    And  number "2." is present in paragraph 2 of article 2
    And  number "2a." is added to existing paragraph 3 of article 2
    When double click on subparagraph 1 of paragraph 3 of article 2
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then num "(a)" is present in point 1 of list 1 of paragraph 2 of article 2
    And  num "(a)" is not shown in grey and strikethrough in point 1 of list 1 of paragraph 2 of article 2
    When click on "Article 3 - Provision of information" link in navigation pane
    Then article 3 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "3" for element Number is disabled in selected element section
    And  input value "Provision of information" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 3
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 3
    And  number "3." is shown as grey and strikethrough in paragraph 3 of article 3
    And  number "4." is shown as grey and strikethrough in paragraph 4 of article 3
    And  number "5." is shown as grey and strikethrough in paragraph 5 of article 3
    And  number "6." is shown as grey and strikethrough in paragraph 6 of article 3
    When double click on paragraph 2 of article 3
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then "2." is showing as grey and strikethrough in subparagraph 2 of paragraph 1 of article 3
    When double click on subparagraph 2 of paragraph 1 of article 3
    Then ck editor window is displayed
    When click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then number "2." is shown as grey and strikethrough in paragraph 2 of article 3
    When double click on paragraph 2 of article 3
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then "2." is showing as grey and strikethrough in subparagraph 2 of paragraph 1 of article 3
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "3" for element Number is disabled in selected element section
    And  input value "Provision of information" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is not shown in grey and strikethrough in paragraph 1 of article 3
    And  number "3." is not shown in grey and strikethrough in paragraph 2 of article 3
    And  number "4." is not shown in grey and strikethrough in paragraph 3 of article 3
    And  number "5." is not shown in grey and strikethrough in paragraph 4 of article 3
    And  number "6." is not shown in grey and strikethrough in paragraph 5 of article 3
    And  "2." is showing as grey and strikethrough in subparagraph 2 of paragraph 1 of article 3
    When double click on subparagraph 2 of paragraph 1 of article 3
    Then ck editor window is displayed
    When click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then number "2." is not shown in grey and strikethrough in paragraph 2 of article 3
    And  number "3." is present in paragraph 3 of article 3
    And  number "4." is present in paragraph 4 of article 3
    And  number "5." is present in paragraph 5 of article 3
    And  number "6." is present in paragraph 6 of article 3
    When click on "Article 4 - Transparency of tariffs and termina..." link in navigation pane
    Then article 4 is displayed
    When click on insert before icon present in show all actions icon of paragraph 1 of article 4
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "-1." is added to new paragraph 1 of article 4
    And  below words are showing as bold in paragraph 1 of article 4
      | Text... |
    When click on insert after icon present in show all actions icon of paragraph 2 of article 4
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  number "1a." is added to new paragraph 3 of article 4
    And  below words are showing as bold in paragraph 3 of article 4
      | Text... |
    When double click on paragraph 3 of article 4
    And  click on increase indent icon present in ck editor panel
    And  click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then point 1 of paragraph 2 of article 4 is displayed
    And  content of point "(a)" of paragraph 2 of article 4 is showing bold in legal act live page
    When click on delete icon present in show all actions icon of point 1 of paragraph 2 of article 4
    Then "Point has been deleted" message is displayed
    And  click on message "Point has been deleted"
    And  number "-1." is added to new paragraph 1 of article 4
    And  below words are showing as bold in paragraph 1 of article 4
      | Text... |
    And  number "1." is present in paragraph 2 of article 4
    When click on "Article 5 - Assessing affordability of tariffs" link in navigation pane
    Then article 5 is displayed
    When click on insert after icon present in show all actions icon of paragraph 1 of article 5
    Then "A new subparagraph has been inserted" message is displayed
    And  click on message "A new subparagraph has been inserted"
    And  paragraph 2 is added to article 5 in legal act live page
    And  number "1a." is added to new paragraph 2 of article 5
    And  below words are showing as bold in paragraph 2 of article 5
      | Text... |
    When click on delete icon present in show all actions icon of point 3 of paragraph 1 of article 5
    Then "Point has been deleted" message is displayed
    And  click on message "Point has been deleted"
    And  point 3 of paragraph 1 of article 5 is showing as grey and strikethrough
    And  paragraph 2 is added to article 5 in legal act live page
    And  number "1a." is added to new paragraph 2 of article 5
    And  below words are showing as bold in paragraph 2 of article 5
      | Text... |
    When click on "7. When no agreement is reached on the basis of..." link in navigation pane
    Then paragraph 7 of article 6 is displayed
    When double click on paragraph 8 of article 6
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on increase indent icon present in ck editor panel
    And  click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    And  subparagraph 2 of paragraph 7 of article 6 is displayed
    And  num tag is not present in subparagraph 2 of paragraph 7 of article 6
    And  "8." is showing as grey and strikethrough in subparagraph 2 of paragraph 7 of article 6
    And  paragraph 8 of article 6 is not present
    When click on "Article 7 - Penalties" link in navigation pane
    Then article 7 is displayed
    When click on insert after icon present in show all actions icon of paragraph 1 of article 7
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  num tag is not present in paragraph 2 of article 7
    And  below words are showing as bold in paragraph 2 of article 7
      | Text... |
    When click on insert after icon present in show all actions icon of paragraph 3 of article 7
    Then "A new paragraph has been inserted" message is displayed
    And  click on message "A new paragraph has been inserted"
    And  num tag is not present in paragraph 4 of article 7
    And  below words are showing as bold in paragraph 4 of article 7
      | Text... |
    And  num tag is not present in paragraph 2 of article 7
    When click on "Article 8 - Review" link in navigation pane
    Then article 8 is displayed
    When double click on subparagraph 1 of paragraph 2 of article 8
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then subparagraph 2 is not present in paragraph 1 of article 8
    And  num "(-a)" is present in point 1 of list 1 of paragraph 1 of article 8
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "8" for element Number is disabled in selected element section
    And  input value "Review" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is added to existing paragraph 1 of article 8
    And  num "(-a)" is present in point 1 of list 1 of paragraph 1 of article 8
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser

  @intendOutdentScenarios2
  Scenario: LEOS-5728 [CN] numbering of that new converted paragraph is wrong
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "council\PROP_ACT_INDENT_OUTDENT.leg"
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
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Automation Testing" keyword in the title of the proposal/mandate
    And  click on title save button
    Then "Metadata saved" message is displayed
    And  click on message "Metadata saved"
    Then title of the proposal/mandate contains "Automation Testing" keyword
######################################End Renaming Proposal Part####################################
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Article 1 - Paragraphs-Subparagraphs" link in navigation pane
    Then article 1 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "1" for element Number is disabled in selected element section
    And  input value "Paragraphs-Subparagraphs" for element Heading is editable in selected element section
    And  "Numbered" option is selected in paragraph numbering in selected element section
    When click on option "Unnumbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is shown as grey and strikethrough in paragraph 1 of article 1
    And  number "2." is shown as grey and strikethrough in paragraph 2 of article 1
    And  number "3." is shown as grey and strikethrough in paragraph 3 of article 1
    And  number "4." is shown as grey and strikethrough in paragraph 4 of article 1
    When click on " Morbi scelerisque enim vel pulvinar lobortis. ..." link in navigation pane
    Then paragraph 3 of article 1 is displayed
    When double click on paragraph 3 of article 1
    Then ck editor window is displayed
    When click on increase indent icon present in ck editor panel
    And  click on increase indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then point 1 of list 2 of paragraph 2 of article 1 is displayed
    And  num "3." is shown in grey and strikethrough in point 1 of list 2 of paragraph 2 of article 1
    And  num "(a)" is shown in bold in point 1 of list 2 of paragraph 2 of article 1
    When double click on subparagraph 2 of paragraph 3 of article 1
    Then ck editor window is displayed
    When click on decrease indent icon present in ck editor panel
    And  click on save close button of ck editor
    Then paragraph 4 of article 1 is displayed
    When click on "Article 1 - Paragraphs-Subparagraphs" link in navigation pane
    Then article 1 is displayed
    When click on toc edit button
    Then selected element section is displayed
    And  input value "Article" for element Type is disabled in selected element section
    And  input value "1" for element Number is disabled in selected element section
    And  input value "Paragraphs-Subparagraphs" for element Heading is editable in selected element section
    And  "Unnumbered" option is selected in paragraph numbering in selected element section
    When click on option "Numbered" in paragraph numbering in selected element section
    And  click on save and close button in navigation pane
    Then "Document structure saved." message is displayed
    And  click on message "Document structure saved."
    And  number "1." is not shown in grey and strikethrough in paragraph 1 of article 1
    And  number "2." is not shown in grey and strikethrough in paragraph 2 of article 1
    And  number "4." is not shown in grey and strikethrough in paragraph 3 of article 1
    When click on "Nam fermentum ipsum in dui pellentesque sollici..." link in navigation pane
    Then point 1 of list 2 of paragraph 2 of article 1 is displayed
    And  num "3." is shown in grey and strikethrough in point 1 of list 2 of paragraph 2 of article 1
    And  num "(a)" is shown in bold in point 1 of list 2 of paragraph 2 of article 1
    And  num "4a." is shown in bold in paragraph 4 of article 1
    When click on close button present in legal act page
    Then OverView screen is displayed
    And  close the browser