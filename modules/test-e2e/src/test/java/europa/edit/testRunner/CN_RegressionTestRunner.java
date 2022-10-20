package europa.edit.testRunner;

import europa.edit.util.SuiteListener;
import europa.edit.util.TestListener;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;

@CucumberOptions(
        features = {"classpath:europa/edit/features/01_CN_RepositoryBrowserPage.feature", "classpath:europa/edit/features/02_CN_ProposalViewerPage.feature", "classpath:europa/edit/features/03_CN_ExpMemo.feature", "classpath:europa/edit/features/04_CN_CouncilExplantory.feature", "classpath:europa/edit/features/05_CN_LegalAct.feature", "classpath:europa/edit/features/06_CN_Annex.feature", "classpath:europa/edit/features/07_CN_ImportFromOj.feature", "classpath:europa/edit/features/08_CN_Annotations.feature"}
        ,plugin = {"pretty", "html:target/cucumber/report.html", "json:target/cucumber/reports.json", "junit:target/junit-reports/reports.xml", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", "rerun:target/results/failed-reports/failedTestCases.txt"}
        ,glue = {"europa/edit/stepdef","europa/edit/appHooks"}
        ,monochrome = true
)

@Listeners({SuiteListener.class, TestListener.class})
public class CN_RegressionTestRunner extends AbstractTestNGCucumberTests {
}