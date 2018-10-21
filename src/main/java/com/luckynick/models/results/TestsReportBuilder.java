package com.luckynick.models.results;

import com.luckynick.custom.Utils;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.models.profiles.SingleTestProfile;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TestsReportBuilder {

    private TestsReport testResults;

    public TestsReportBuilder(SequentialTestProfile seqTestProfile) {
        testResults = new TestsReport(seqTestProfile);
    }

    public TestsReportBuilder addTestResult(SingleTestResult testResult) {
        testResults.resultsOfTests.add(testResult);
        return this;
    }

    public TestsReport build() {
        return testResults;
    }
}
