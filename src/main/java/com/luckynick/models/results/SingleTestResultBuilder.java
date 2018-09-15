package com.luckynick.models.results;

import com.luckynick.shared.SharedUtils;

@Deprecated
public class SingleTestResultBuilder extends TestResultBuilder<SingleTestResult> {

    public SingleTestResultBuilder() {
        super(SingleTestResult.class, new Object[] {
                SharedUtils.getDateStringForFileName()
        });
    }

    @Override
    public SingleTestResult build() {
        SingleTestResult result = super.build();
        //result.setSessionSummaries();
        return result;
    }
}
