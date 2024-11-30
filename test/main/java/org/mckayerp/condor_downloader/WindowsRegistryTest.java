package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mckayerp.condor_downloader.WindowsRegistry.readRegistry;

public class WindowsRegistryTest
{

    @Test
    public final void testRegistrySearch() {

        String output = readRegistry("HKLM\\SOFTWARE\\MOZILLA\\MOZILLA FIREFOX", "PathToExe", "/s");
        assertEquals("C:\\Program Files\\Mozilla Firefox\\firefox.exe", output);

    }
}
