package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SettingsDefaultsTest
{

    @Test
    final void whenPathDoesNotExist_getDefaultCondorReturnsNull()
    {

        SettingsDialogController controller = mock(SettingsDialogController.class);
        doReturn(false).when(controller).pathExists(any());
        doCallRealMethod().when(controller).getDefaultCondorDirectory(SettingsDialogController.CONDOR3);
        assertNull(controller.getDefaultCondorDirectory(CondorVersion.CONDOR_3));
    }

    @ParameterizedTest(name = "Default directory for Condor version {0} works")
    @ValueSource(ints = {2, 3})
    final void defaultCondorDirectoryCanBeFound(int versionNum)
    {

        CondorVersion version = versionNum == 2 ? CondorVersion.CONDOR_2 : CondorVersion.CONDOR_3;
        String userName = System.getProperty("user.name");
        String expectedPath = "C:\\Users\\" + userName + "\\Documents\\Condor";
        if (version == CondorVersion.CONDOR_3)
            expectedPath += "3";
        SettingsDialogController controller = mock(SettingsDialogController.class);
        doReturn(false).when(controller).pathExists(any());
        doCallRealMethod().when(controller).getDefaultCondorDirectory(any(CondorVersion.class));
        doReturn(true).when(controller).pathExists(any(Path.class));
        assertEquals(expectedPath, controller.getDefaultCondorDirectory(version).toString());

    }

}