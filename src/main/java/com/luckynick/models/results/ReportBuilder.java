package com.luckynick.models.results;

import com.luckynick.custom.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@Deprecated
public class ReportBuilder<T extends TestsReport> {

    private ArrayList<SingleTestResult> testResults = new ArrayList<>();
    private Class<T> reportClass;

    public ReportBuilder(Class<T> reportClass) {
        this.reportClass = reportClass;
    }

    public ReportBuilder addTestResult(SingleTestResult testResult) {
        testResults.add(testResult);
        return this;
    }

    public T build() {
        try {
            return reportClass.getConstructor(reportClass)
                    .newInstance(Utils.toArray(SingleTestResult.class, testResults));
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
