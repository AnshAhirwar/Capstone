package com.practice.notes.runner;

import com.practice.notes.retry.SmartRetryAnalyzer;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@CucumberOptions(
        features = "src/test/resources/features/api",
        glue = {"com.practice.notes.stepdefinitions", "com.practice.notes.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/api-report.html",
                "json:target/cucumber-reports/api-report.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class ApiTestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios",
          dataProvider = "scenarios", retryAnalyzer = SmartRetryAnalyzer.class)
    @Override
    public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        try {
            super.runScenario(pickleWrapper, featureWrapper);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
