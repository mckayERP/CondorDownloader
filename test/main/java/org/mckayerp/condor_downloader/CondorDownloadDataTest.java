package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

public class CondorDownloadDataTest
{

    @Test
    public final void condorDownloadDataCanBeConstructed()
    {
        DownloadData data = new CondorDownloadData().withNumberToDownload(Integer.parseInt("5")).withTaskCode("ABCDE").withFirefoxExecutablePath("C:\\").withStatusProvider(new Controller());
    }
}
