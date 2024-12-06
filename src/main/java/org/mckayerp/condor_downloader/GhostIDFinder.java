package org.mckayerp.condor_downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GhostIDFinder
{

    public static List<String> findGhostIDs(String bestPerformancePageSource, int numberToFind)
    {
        List<String> matches = new ArrayList<>();
        String regexPattern = "dl\\('/download2/0/\\?res=([0-9]*)&(?:amp;)?next=1";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(bestPerformancePageSource);

        int countMatched = 0;
        while (matcher.find() && countMatched < numberToFind)
        {
            matches.add(matcher.group(1)); // Add the matched group (res value)
            countMatched++;
        }
        return matches;
    }

}
