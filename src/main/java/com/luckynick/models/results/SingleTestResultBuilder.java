package com.luckynick.models.results;

import com.luckynick.Utils;

@Deprecated
public class SingleTestResultBuilder extends TestResultBuilder<SingleTestResult> {

    public SingleTestResultBuilder() {
        super(SingleTestResult.class, new Object[] {
                Utils.getDateStringForFileName()
        });
    }

    @Override
    public SingleTestResult build() {
        SingleTestResult result = super.build();
        //result.setSessionSummaries();
        return result;
    }
}
