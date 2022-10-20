#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in clone proposal in EC instance of Edit Application
@CloneProposalRegressionScenarios
Feature: Clone Proposal Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page

  @forkAndMergeProposal
  Scenario: LEOS-5043 [EC] test fork and merge of a proposal
    When click on upload button present in the Repository Browser page
    Then upload window 'Upload a leg file 1/2' is showing
    When upload a leg file for creating proposal from location "commission/PROP_ACT_1383684831844402901.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on next button
    Then "Upload a legislative document - Document metadata (2/2)" is displayed
    When provide document title "Automation.....Fork&Merge....." in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "Automation.....Fork&Merge....." keyword
    When click on add a new annex button
    Then "Annex " is added to Annexes
    And  numbers of annex present in proposal viewer screen is 1
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "For Interservice Consultation" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | For Interservice Consultation |
      | For Decision |
      | Revision after Interservice Consultation |
      | Other |
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
    When click on actions hamburger icon of first milestone
    Then below options are displayed under milestone actions hamburger icon
      | View                     |
      | Send a copy for contribution |
    When click on send a copy for contribution option
    Then "Send a copy of the milestone for contribution" window is displayed
    And  "Type user name" is mentioned in target user input field
    And  send for contribution button is displayed but disabled
    And  close button is displayed and enabled in Send a copy of the milestone for contribution window
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
    And  first proposal name contains "Automation.....Fork&Merge....."
    And  colour of first proposal is "rgba(226, 226, 226, 1)"
    And  first proposal contains keyword Contribution status: Sent for contribution
    And  first proposal contains keyword CONTRIBUTION EdiT
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  proposal title has a label CONTRIBUTION EdiT in proposal viewer page
    And  numbers of annex present in proposal viewer screen is 1
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on preamble toggle link
    And  click on citation link present in navigation pane
    Then citation 1 is displayed
    When double click on citation 1
    Then ck editor window is displayed
    When replace content "Having regard to the Treaty on the Functioning of the EU, and in particular Article [...] thereof, New Text" with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then below words are showing as green and underline in citation 1
      | EU |
      | New Text |
    And  below words are showing as red and strikethrough in citation 1
      | European Union |
    When double click on citation 2
    Then ck editor window is displayed
    When replace content "Having regard to the proposal from the European Commission,New Word" with the existing content in ck editor text box
    And  click on save close button of ck editor
    Then below words are showing as green and underline in citation 2
      | New Word |
    When click on article 1 in navigation pane
    Then article 1 is displayed
    When mouse hover and click on show all action button and click on edit button of article 1
    Then ck editor window is displayed
    And  save close button is disabled in ck editor
    And  save button is disabled in ck editor
    When append "New Text" at the end of the paragraph of article
    And  click on close button of ck editor
    Then confirm cancel editing window is displayed
    When click on ok button in confirm cancel editing window
    Then ck editor window is not displayed
    When click on article 1 in navigation pane
    And  mouse hover and click on show all action button and click on edit button of article 1
    Then ck editor window is displayed
    When append "New Text" at the end of the paragraph of article
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    Then "New Text" is added with colour "rgba(0, 128, 0, 1)" to the paragraph 1 of article 1
    When click on insert after icon present in show all actions icon of article 1
    Then "A new article has been inserted" message is displayed
    And  click on message "A new article has been inserted"
    And  num "Article 2" is shown as green and underlined in new article 2
    And  heading "Article heading..." is shown as green and bold in new article 2
    And  num "1." is shown as green in paragraph 1 of new article 2
    And  below words are shown as green in paragraph 1 of new article 2
      | Text... |
    And  num "2" is shown as red and strikethrough in article 3
    And  num "3" is shown as green and underlined in article 3
    And  num "3" is shown as red and strikethrough in article 4
    And  num "4" is shown as green and underlined in article 4
    When click on article 3 in navigation pane
    Then article 3 is displayed
    When double click on article 3
    Then ck editor window is displayed
    When append "New Text" at the end of the paragraph of article
    And  click on save close button of ck editor
    Then ck editor window is not displayed
    Then "New Text" is added with colour "rgba(0, 128, 0, 1)" to the paragraph 1 of article 3
    When click on delete icon present in show all actions icon of article 4
    Then "Article has been deleted" message is displayed
    And  click on message "Article has been deleted"
    And  article 4 is showing as red and strikethrough
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
    When click on contribution pane accordion
    Then contribution from legal service with version "Version 1.0.0" is displayed
    When click on hamburger icon of contribution from legal service with "Version 1.0.0" in contribution pane
    Then below options are displayed under contribution actions hamburger icon
      | View and merge       |
      | Decline contribution |
    When click on option "View and merge" in contribution actions hamburger icon
    Then contribution view and merge screen is displayed
    When click on close button present in contribution view and merge screen
    Then contribution view and merge screen is not displayed
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @specialCharactersTestingInArticleParagraph
  Scenario: LEOS-5663 Unescape & is throwing error in document screen
    And  create proposal button is displayed and enabled
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" window is displayed
    When provide document title "Automation Testing Special Character Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    And  click on milestone option For Interservice Consultation
    And  click on create milestone button
    Then "Milestone creation has been requested" message is displayed
    And  click on message "Milestone creation has been requested"
    And  "For Interservice Consultation" is showing in row 1 of title column of milestones table in commission instance
    And  today's date is showing under date column row 1 of milestones table
    And  "In preparation" is showing under status column row 1 of milestones table
    And  "For Interservice Consultation has been updated." message is displayed
    And  "File ready" is showing under status column row 1 of milestones table
    When click on actions hamburger icon of first milestone
    Then below options are displayed under milestone actions hamburger icon
      | View                     |
      | Send a copy for contribution |
    When click on send a copy for contribution option
    Then "Send a copy of the milestone for contribution" window is displayed
    And  "Type user name" is mentioned in target user input field
    And  send for contribution button is displayed but disabled
    And  close button is displayed and enabled in Send a copy of the milestone for contribution window
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
    And  first proposal name contains "Automation Testing Special Character Testing"
    And  colour of first proposal is "rgba(226, 226, 226, 1)"
    And  first proposal contains keyword Contribution status: Sent for contribution
    And  first proposal contains keyword CONTRIBUTION EdiT
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  proposal title has a label CONTRIBUTION EdiT in proposal viewer page
    When click on open button of legal act
    Then legal act page is displayed
    And  annotation side bar is present
    When click on "Article 1 - Scope 1. Text..." link in navigation pane
    Then article 1 is displayed
    When double click on article 1
    Then ck editor window is displayed
    When append content "0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ''-â€“â€”''!!#$%&(())*./:;?@[[][[]]\]^_``{{}|}~Â´â€˜â€˜â€™â€™â€œâ€œâ€Â¢â‚¬+<<><Â=â‰>Â±Â«Â»Ã—Ã·â‰¤â‰¥Â§Â©Â®Â°Â¶â€¦â€â„¢Ï€,~Â¡Â£Â¤Â¥Â¦Â¨ÂªÂ«Â¬Â­Â¯Â²Â³ÂµÂ·Â¸Â¹ÂºÂ»Â¼Â½Â¾Â¿Ã€ÃÃ‚ÃƒÃ„Ã…Ã†Ã‡ÃˆÃ‰ÃŠÃ‹ÃŒÃÃŽÃÃÃ‘Ã’Ã“Ã”Ã•Ã–Ã˜Ã™ÃšÃ›ÃœÃÃžÃŸÃÃ¡Ã¢Ã£Ã¤Ã¥Ã¦Ã§Ã¨Ã©ÃªÃ«Ã¬Ã­Ã®Ã¯Ã°Ã±Ã²Ã³Ã´ÃµÃ¶Ã¸Ã¹ÃºÃ»Ã¼Ã½Ã¾Ã¿" with the existing content in ck editor text box of a paragraph inside article
    And  click on save close button of ck editor
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @nonSupportUserNoAccessToCloneProposal
  Scenario: LEOS-5952 Only user with support role can access to clone proposals
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" window is displayed
    When provide document title "Automation Clone Proposal Access Testing to NonSupport User" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "Clone Proposal Access Testing to NonSupport User" keyword
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
    When search "Test1 Test" in the target user field
    Then user "Test1 Test" is showing in the list
    When click on first user showing in the list
    Then "Test1 Test" user is selected in the target user input field
    And  send for contribution button is displayed and enabled
    When click on send for contribution button
    Then "Copy sent for contribution" message is displayed
    And  click on message "Copy sent for contribution"
    Then "Sent for contribution to Test1 Test" is showing under title column row 2 of milestones table
    And  today's date is showing under date column row 2 of milestones table
    And  "Sent for contribution" is showing under status column row 2 of milestones table
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    And  first proposal name contains "Clone Proposal Access Testing to NonSupport User"
    And  colour of first proposal is "rgba(226, 226, 226, 1)"
    And  first proposal contains keyword Contribution status: Sent for contribution
    And  first proposal contains keyword CONTRIBUTION EdiT
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    When search keyword "Clone Proposal Access Testing to NonSupport User" in the search bar of repository browser page
    Then first proposal name contains "Clone Proposal Access Testing to NonSupport User"
    And  close the browser