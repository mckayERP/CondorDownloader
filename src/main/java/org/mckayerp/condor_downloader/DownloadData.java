package org.mckayerp.condor_downloader;

import java.nio.file.Path;

public interface DownloadData
{
    int getNumberToDownload();

    DownloadData withNumberToDownload(int i);

    String getTaskCode();

    DownloadData withTaskCode(String taskCode);

    Path getFirefoxExecutablePath();

    DownloadData withFirefoxExecutablePath(String firefoxPath);

    DownloadData withStatusProvider(StatusProvider provider);

    StatusProvider getStatusProvider();

    boolean doesCondor2DirectoryExist();

    DownloadData withCondor2DirectoryExists(boolean exists);

    boolean doesCondor3DirectoryExist();

    DownloadData withCondor3DirectoryExists(boolean exists);

    CharSequence getCondorClubUserEmail();

    DownloadData withCondorClubUserEmail(String condorClubUserEmail);

    DownloadData withCondorClubUserPassword(String condorClubUserPassword);

    String getCondorClubUserPassword();

    Path getCondor2Path();

    DownloadData withCondor2Path(Path path);

    Path getCondor3Path();

    DownloadData withCondor3Path(Path path);

    boolean isDownloadFlightPlanSelected();

    DownloadData withDownloadFligthPlanSelected(boolean selected);

    boolean isCopyGhostsToFlightTrackFolderSelected();

    DownloadData withCopyGhostsToFlightTrackFolderSelected(boolean selected);

    boolean isCopyGhostsFromCompetitionSelected();

    DownloadData withCopyGhostsFromCompetitionSelected(boolean selected);
}
