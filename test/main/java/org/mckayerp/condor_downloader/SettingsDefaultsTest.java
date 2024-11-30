package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


public class SettingsDefaultsTest {

    @Test
    final void whenPathDoesNotExist_getDefaultCondorThrowsException() {

        SettingsDialogController controller = spy(new SettingsDialogController());
        doReturn(false).when(controller).pathExists(any());
        assertThrows(RuntimeException.class, controller::getDefaultCondorDirectory);
    }

    @Test
    final void defaultCondorDirectoryCanBeFound() {

        SettingsDialogController controller = spy(new SettingsDialogController());
        String userName = System.getProperty("user.name");
        assertEquals("C:\\Users\\" + userName + "\\Documents\\Condor", controller.getDefaultCondorDirectory().toString());

    }

}