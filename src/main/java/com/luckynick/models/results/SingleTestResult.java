package com.luckynick.models.results;

import com.luckynick.Utils;
import com.luckynick.models.IOClassHandling;

public class SingleTestResult extends TestResult {

    private SessionSummary senderSessionSummary;
    private SessionSummary receiverSessionSummary;

    public boolean isDecodingSuccessful;
    public double messageMatchPecrentage;

    public float valueForPlot() {
        return (float) messageMatchPecrentage;
    }


    public SingleTestResult() {
        super();
    }

    public void setSessionSummaries(SessionSummary senderSessionSummary, SessionSummary receiverSessionSummary) {
        this.senderSessionSummary = senderSessionSummary;
        this.receiverSessionSummary = receiverSessionSummary;
        countStatistics();
    }

    private void countStatistics() {

    }


}
