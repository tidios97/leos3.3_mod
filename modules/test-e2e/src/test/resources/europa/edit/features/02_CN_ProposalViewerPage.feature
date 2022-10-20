#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Proposal Browser Page in CN instance of Edit Application
@ProposalBrowserPageRegressionScenariosEditCouncil
Feature: Proposal Browser Page Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application

  @closeButtonNotVisibleNonSupportUser
  Scenario: LEOS-5025 [CN] Close button is not available for non support user in proposal browser page
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then OverView screen is displayed
    And  close button is not displayed
    And  close the browser

  @deleteMandate
  Scenario: LEOS-4584,4145 [CN] Verify User is able to do create mandate, create milestone, title change of mandate and delete mandate
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
    When click on delete button
    Then mandate deletion confirmation page should be displayed
    And  "Are you sure you want to delete the mandate and contained documents?" message is displayed
    And  cancel button is displayed and enabled in proposal deletion confirmation pop up
    And  delete button is displayed and enabled in proposal deletion confirmation pop up
    When click on delete button present in confirmation pop up
    Then navigate to Repository Browser page
    And  close the browser
