package org.mckayerp.condor_downloader;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GhostFileManagerTest
{

    @Test
    final void givenAZipFileName_ReturnsGhostFileName()
    {
        String zipFileName = "TNS Ronan to Glacier National Park Montana-T3X-263690.zip";
        String taskCode = "EQUHAC";
        String ghostID = "263690";
        String newName = GhostFileManager.findNewFileName(zipFileName, taskCode, ghostID);
        assertEquals(newName, "Ghost_EQUHAC_T3X_263690.ftr");
    }
}