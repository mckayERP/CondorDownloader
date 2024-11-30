package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FireFoxFinderTest
{

    @Test
    public final void canFindFireFoxExe() {

        String output = FireFoxFinder.getPathToExe();
        assertEquals("C:\\Program Files\\Mozilla Firefox\\firefox.exe", output);

    }
}
