package europa.edit.testRunner;

import europa.edit.util.SuiteListener;
import europa.edit.util.TestListener;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;

@CucumberOptions(
        features = {"@target/results/failed-reports/failedTestCases.txt"}
        ,plugin = {"pretty", "html:target/cucumber/ReRunFailedTestCasesReport.html", "json:target/cucumber/ReRunFailedTestCasesReport.json", "junit:target/junit-reports/ReRunFailedTestCasesReport.xml", "rerun:target/results/failed-reports/failedTestCases.txt"}
        ,glue = {"europa/edit/stepdef","europa/edit/appHooks"}
        ,monochrome = true
/*        ,dryRun = false*/
/*        ,tags = "@VerifyRepositoryBrowserPage"*/
)

@Listeners({SuiteListener.class, TestListener.class})
public class ReRunFailedTestCasesTestRunner extends AbstractTestNGCucumberTests {
}