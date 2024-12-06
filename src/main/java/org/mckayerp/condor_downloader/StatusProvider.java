package org.mckayerp.condor_downloader;

import java.util.logging.Level;

public interface StatusProvider
{

    default void updateStatus(String update)
    {
        boolean CLEAR_STATUS = false;
        boolean SCROLL_TO_BOTTOM = true;
        updateStatus(update, CLEAR_STATUS, SCROLL_TO_BOTTOM);
    }

    default void updateStatus(Level level, String update)
    {
        boolean CLEAR_STATUS = true;
        boolean SCROLL_TO_BOTTOM = true;
        updateStatus(level, update, CLEAR_STATUS, SCROLL_TO_BOTTOM);
    }

    default void updateStatus(String update, boolean clearStatus, boolean scroll_to_bottom)
    {
        updateStatus(Level.INFO, update, clearStatus, scroll_to_bottom);
    }

    void updateStatus(Level logLevel, String update, boolean clearStatus, boolean scroll_to_bottom);

    default void clearStatus()
    {
        updateStatus("", true, false);
    }
}
