package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SettingsDefaultsTest {

    @Test
    final void whenPathDoesNotExist_getDefaultCondorThrowsException() {

        SettingsDialogController controller = mock(SettingsDialogController.class);
        doReturn(false).when(controller).pathExists(any());
        doCallRealMethod().when(controller).getDefaultCondorDirectory();
        assertThrows(RuntimeException.class, controller::getDefaultCondorDirectory);
    }

    @Test
    final void defaultCondorDirectoryCanBeFound() {

        SettingsDialogController controller = new SettingsDialogController();
        String userName = System.getProperty("user.name");
        assertEquals("C:\\Users\\" + userName + "\\Documents\\Condor3", controller.getDefaultCondorDirectory().toString());

    }

}