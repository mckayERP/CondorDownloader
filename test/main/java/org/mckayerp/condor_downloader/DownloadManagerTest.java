package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DownloadManagerTest
{

    @Test
    public final void constructorTest()
    {

        DownloadData data = mock(CondorDownloadData.class);
        DownloadManager manager = new DownloadManager(data);

    }

    @Test
    public final void whenGivenANullDriver_getCondorVersionThrowsException()
    {

        DownloadData dataMock = mock(DownloadData.class);
        DownloadManager manager = new DownloadManager(dataMock);
        assertThrows(IllegalArgumentException.class, () ->
        {
            manager.getCondorVersion(null);
        });

    }

}
