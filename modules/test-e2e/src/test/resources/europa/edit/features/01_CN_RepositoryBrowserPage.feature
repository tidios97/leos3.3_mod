#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Repository Browser Page in CN instance of Edit Application
@RepositoryBrowserPageRegressionScenariosEditCouncil
Feature: Repository Browser Page Regression Features in Edit Council

  @resetFilterSearchAndDoubleClickMandate
  Scenario: LEOS-3841 [CN] Verify user is able to reset filter, search mandate and open proposal by using double click
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    And  filter section is present
    And  search bar is present
    And  "Create mandate" button is present
    And  user name is present in the Top right upper corner
    And  proposal/mandate list is displayed
    When untick "Proposal for a directive" in act category under filter section
    Then "Proposal for a directive" in act category is unticked
    When click on reset button
    Then "Proposal for a directive" is ticked in act category under filter section
    When search keyword "Auto_Testing" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Auto_Testing"
    When double click on first proposal
    Then OverView screen is displayed
    When click on home button
    Then navigate to Repository Browser page
    And  close the browser

#  @test
#  Scenario: test
#    Given navigate to "Council" edit application
#    When enter username "user.support.1.name" and password "user.support.1.pwd"
#    Then navigate to Repository Browser page
#    When search keyword "Automation Annex" in the search bar of repository browser page
#    Then each proposal/mandate in the search results contain keyword "Automation Annex"
#    When click on the open button of first proposal/mandate
#    Then Proposal Viewer screen is displayed
#    And  close the browser