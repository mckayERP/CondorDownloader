package org.mckayerp.condor_downloader;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ControllerTest {

    @Test
    final void givenNullFlightTrackFolderWhenDownloadFlightTrackIsCalled_ThrowsAnException() {

        WebDriver driver = mock(WebDriver.class);
        Controller controller = spy(Controller.class);
        controller.condorFolderPath = null;
        assertThrows(NullPointerException.class, () ->
                controller.downloadFlightPlan(driver),
                "Expected an exception when the condor default folder is null.");

    }

    @Test
    final void givenFlightTrackFolderIsNotFound_ThrowsAnException() {

        WebDriver driver = mock(WebDriver.class);
        Controller controller = spy(Controller.class);
        doNothing().when(controller).updateStatus(anyString());
        controller.condorFolderPath = Paths.get("C:\\Users\\Mike\\gobledygook_13245346");
        assertThrows(Exception.class, () ->
                controller.downloadFlightPlan(driver),
                "Expected an exception when the condor default folder is not found.");

    }

    @Test
    final void givenFlightTrackFolderExistsWhenDownloadFlightTrackIsCalled_doesNotThrowAnException() {

        WebDriver.Navigation navigateMock = mock(WebDriver.Navigation.class);
        WebDriver driver = mock(FirefoxDriver.class);
        doReturn("someString").when(driver).getCurrentUrl();
        doReturn("someString").when(driver).getPageSource();
        doReturn(navigateMock).when(driver).navigate();
        Controller controller = spy(Controller.class);
        doNothing().when(controller).updateStatus(anyString());
        doReturn("").when(controller).getCondorClubTaskIDFromPageSource(anyString());
        controller.condorFolderPath = Paths.get("C:\\Users\\Mike\\Documents\\Condor\\FlightTracks");
        controller.downloadFlightPlan(driver);

    }

}