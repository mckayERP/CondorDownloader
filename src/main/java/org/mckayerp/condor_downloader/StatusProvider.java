package org.mckayerp.condor_downloader;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface StatusProvider
{
    Logger logger = Logger.getLogger(StatusProvider.class.getName());

    default void updateStatus(String update)
    {
        boolean ADD_LINEBREAK_BEFORE = true;
        boolean SCROLL_TO_BOTTOM = true;
        updateStatus(update, ADD_LINEBREAK_BEFORE, SCROLL_TO_BOTTOM);
    }

    default void updateStatus(Level level, String update)
    {
        boolean ADD_LINEBREAK_BEFORE = true;
        boolean SCROLL_TO_BOTTOM = true;
        updateStatus(level, update, ADD_LINEBREAK_BEFORE, SCROLL_TO_BOTTOM);
    }

    default void updateStatus(String update, boolean addLineBreakBefore, boolean scroll_to_bottom)
    {
        updateStatus(Level.INFO, update, addLineBreakBefore, scroll_to_bottom);
    }

    void updateStatus(Level logLevel, String update, boolean addLineBreakBefore, boolean scroll_to_bottom);

    default void clearStatus()
    {
        updateStatus("", false, false);
    }
}
