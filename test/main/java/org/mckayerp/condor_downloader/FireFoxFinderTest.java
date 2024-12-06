package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class FireFoxFinderTest
{

    @Test
    public final void canFindFireFoxExe()
    {

        String output = FireFoxFinder.get().getPathToExe();
        assertEquals("C:\\Program Files\\Mozilla Firefox\\firefox.exe", output);

    }

    @Test
    public final void whenNotFound_hintStringReturned()
    {
        FireFoxFinder fireFoxFinder = spy(FireFoxFinder.get());
        doReturn(null).when(fireFoxFinder).getFromRegistry(any(), any(), any());
        assertEquals("Select the path to firefox.exe", fireFoxFinder.getPathToExe());
    }

}
