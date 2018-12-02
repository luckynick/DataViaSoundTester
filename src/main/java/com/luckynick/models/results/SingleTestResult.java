package com.luckynick.models.results;

import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.model.ReceiveSessionSummary;
import com.luckynick.shared.model.SendSessionSummary;

import java.util.ArrayList;
import java.util.List;


@IOClassHandling(dataStorage = SharedUtils.DataStorage.SINGULAR_RESULT)
public class SingleTestResult extends TestResult {

    //TODO: fix 46. Match: 100.0; send message: One; loudness: 60; recived message: ¡ãOne

    public SendSessionSummary senderSessionSummary;
    public ReceiveSessionSummary receiverSessionSummary;

    public boolean isDecodingSuccessful;
    public double messageMatchPecrentage;

    public float valueForPlot() {
        return (float) messageMatchPecrentage;
    }

    /**
     * Used for obtaining location of folder using reflection.
     */
    private SingleTestResult() {
        super();
    }

    public SingleTestResult(SendSessionSummary senderSessionSummary, ReceiveSessionSummary receiverSessionSummary) {
        super();

        this.senderSessionSummary = senderSessionSummary;
        this.receiverSessionSummary = receiverSessionSummary;
        countStatistics();
    }

    /*
    public void setSessionSummaries(SendSessionSummary senderSessionSummary, ReceiveSessionSummary receiverSessionSummary) {
        this.senderSessionSummary = senderSessionSummary;
        //this.receiverSessionSummary = receiverSessionSummary;
        countStatistics();
    }
    */

    private void countStatistics() {
        String sent = senderSessionSummary.sendParameters.message;
        String received = receiverSessionSummary.message;

        isDecodingSuccessful = sent.equals(received);

        double lengthMatch = countMatchPercentageByLength(sent, received);
        double charByCharMatch = countMatchPercentageCharByChar(sent, received);
        messageMatchPecrentage = lengthMatch * charByCharMatch;

        if(receiverSessionSummary.exceptionDuringDecoding != null) messageMatchPecrentage = 0;
    }


    public static void main(String args[]) {
        /*
        System.out.println("Match percentage: " + countMatchPercentageCharByChar("ala ma kota",
                "do tego ala ma kota dużego") + "%"); //100%, toCheck string contains 100% of perfect string


        System.out.println("Match percentage: " + countMatchPercentageCharByChar("ala ma kota",
                "do tego bla mfgkota dużego") + "%"); //72.72727272727273%, toCheck string contains 100% of perfect string
        */
        String sent = "ala ma kota", received = "do tego ala ma kota dużego";
        double lengthMatch = countMatchPercentageByLength(sent, received);
        double charByCharMatch = countMatchPercentageCharByChar(sent, received);
        System.out.println("Match percentage: " + (lengthMatch * charByCharMatch) + "%");
        received = "do tego bla mfgkota dużego";
        lengthMatch = countMatchPercentageByLength(sent, received);
        charByCharMatch = countMatchPercentageCharByChar(sent, received);
        System.out.println("Match percentage: " + (lengthMatch * charByCharMatch) + "%");
    }


    /**
     * find indexes from toCheck string of each character in perfect string then start finding the longest match
     * starting from those indexes in the end return the longest match
     * @param perfect
     * @param toCheck
     * @return
     */
    public static double countMatchPercentageCharByChar(String perfect, String toCheck) {
        if(!"null".equals(perfect) && "null".equals(toCheck)) return 0.0;

        int indexes[][] = new int[perfect.length()][];
        for(int i = 0; i < perfect.length(); i++) {
            char toFindIndexes = perfect.charAt(i);
            List<Integer> indexesOfChar = new ArrayList<>();
            int lastIndex = toCheck.indexOf(toFindIndexes, 0);
            while(lastIndex != -1) {
                indexesOfChar.add(lastIndex);
                lastIndex = toCheck.indexOf(toFindIndexes, lastIndex + 1);
            }
            indexes[i] = new int[indexesOfChar.size()];
            for(int j = 0; j < indexes[i].length; j++) {
                indexes[i][j] = indexesOfChar.get(j);
            }
        }

        List<Integer> distances = new ArrayList<>();
        for(int i = 0; i < indexes.length; i++) {
            for(int j = 0; j < indexes[i].length; j++) {
                int matchedChars = 0;
                for(int k = 0; k < indexes.length - i && indexes[i][j] + k < toCheck.length(); k++) {
                    char toCheckChar = toCheck.charAt(indexes[i][j] + k);
                    char perfectChar = perfect.charAt(i + k);
                    if(toCheckChar == perfectChar) matchedChars++;
                }
                distances.add(matchedChars);
            }
        }
        distances.sort((x1, x2) -> x1 - x2);
        if(distances.size() < 1) return 0;
        int longestDistance = distances.get(distances.size() - 1);

        return ((double)(longestDistance)/perfect.length())*100.0;
    }

    public static double countMatchPercentageByLength(String perfect, String toCheck) {
        if(perfect.length() <= toCheck.length()) return ((double)perfect.length())/toCheck.length();
        else return 1;
    }

}
