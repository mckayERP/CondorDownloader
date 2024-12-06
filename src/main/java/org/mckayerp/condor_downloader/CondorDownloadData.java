package org.mckayerp.condor_downloader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class CondorDownloadData implements DownloadData
{
    int numberToDownload = 0;
    String taskCode = null;
    private Path firefoxPath;
    private StatusProvider statusProvider;
    private boolean condor2DirectoryExists;
    private boolean condor3DirectoryExists;
    private String condorClubUserEmail;
    private String condorClubUserPassword;
    private boolean isDownloadFlightPlanSelected;
    private boolean isCopyGhostsToFlightTrackFolderSelected;
    private boolean isCopyGhostsFromCompetitionSelected;

    @Override
    public int getNumberToDownload()
    {
        return numberToDownload;
    }

    @Override
    public DownloadData withNumberToDownload(int i)
    {
        numberToDownload = i;
        return this;
    }

    @Override
    public String getTaskCode()
    {
        return taskCode;
    }

    @Override
    public DownloadData withTaskCode(String code)
    {
        taskCode = code;
        return this;
    }

    @Override
    public Path getFirefoxExecutablePath()
    {
        return firefoxPath;
    }

    @Override
    public DownloadData withFirefoxExecutablePath(String path)
    {
        firefoxPath = Paths.get(path);
        return this;
    }

    @Override
    public DownloadData withStatusProvider(StatusProvider provider)
    {
        statusProvider = Objects.requireNonNull(provider);
        return this;
    }

    @Override
    public StatusProvider getStatusProvider()
    {
        return statusProvider;
    }

    @Override
    public boolean doesCondor2DirectoryExist()
    {
        return condor2DirectoryExists;
    }

    @Override
    public DownloadData withCondor2DirectoryExists(boolean exists)
    {
        condor2DirectoryExists = exists;
        return this;
    }

    @Override
    public boolean doesCondor3DirectoryExist()
    {
        return condor3DirectoryExists;
    }

    @Override
    public DownloadData withCondor3DirectoryExists(boolean exists)
    {
        condor3DirectoryExists = exists;
        return this;
    }

    @Override
    public CharSequence getCondorClubUserEmail()
    {
        return condorClubUserEmail;
    }

    @Override
    public DownloadData withCondorClubUserEmail(String email)
    {
        condorClubUserEmail = email;
        return this;
    }

    @Override
    public String getCondorClubUserPassword()
    {
        return condorClubUserPassword;
    }

    @Override
    public DownloadData withCondorClubUserPassword(String password)
    {
        condorClubUserPassword = password;
        return this;
    }

    @Override
    public boolean isDownloadFlightPlanSelected()
    {
        return isDownloadFlightPlanSelected;
    }

    @Override
    public DownloadData withDownloadFligthPlanSelected(boolean selected)
    {
        isDownloadFlightPlanSelected = selected;
        return this;
    }

    @Override
    public boolean isCopyGhostsToFlightTrackFolderSelected()
    {
        return isCopyGhostsToFlightTrackFolderSelected;
    }

    @Override
    public DownloadData withCopyGhostsToFlightTrackFolderSelected(boolean selected)
    {
        isCopyGhostsToFlightTrackFolderSelected = selected;
        return this;
    }

    @Override
    public boolean isCopyGhostsFromCompetitionSelected()
    {
        return isCopyGhostsFromCompetitionSelected;
    }

    @Override
    public DownloadData withCopyGhostsFromCompetitionSelected(boolean selected)
    {
        isCopyGhostsFromCompetitionSelected = selected;
        return this;
    }

}
