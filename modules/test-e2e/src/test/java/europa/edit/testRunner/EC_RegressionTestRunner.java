package europa.edit.testRunner;

import europa.edit.util.SuiteListener;
import europa.edit.util.TestListener;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;

@CucumberOptions(
        features = {"classpath:europa/edit/features/01_EC_RepositoryBrowserPage.feature", "classpath:europa/edit/features/03_EC_CreateProposal.feature", "classpath:europa/edit/features/04_EC_ProposalViewer.feature", "classpath:europa/edit/features/05_EC_CloneProposal.feature", "classpath:europa/edit/features/06_EC_ExpMemo.feature", "classpath:europa/edit/features/07_EC_ImportFromOj.feature", "classpath:europa/edit/features/08_EC_LegalAct.feature", "classpath:europa/edit/features/09_EC_Annotations.feature", "classpath:europa/edit/features/10_EC_Annex.feature", "classpath:europa/edit/features/11_EC_MilestoneExplorer.feature", "classpath:europa/edit/features/12_EC_CoverPage.feature"}
        ,plugin = {"pretty", "html:target/cucumber/report.html", "json:target/cucumber/reports.json", "junit:target/junit-reports/reports.xml", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", "rerun:target/results/failed-reports/failedTestCases.txt"}
        ,glue = {"europa/edit/stepdef","europa/edit/appHooks"}
        ,monochrome = true
)

@Listeners({SuiteListener.class, TestListener.class})
public class EC_RegressionTestRunner extends AbstractTestNGCucumberTests {
}
