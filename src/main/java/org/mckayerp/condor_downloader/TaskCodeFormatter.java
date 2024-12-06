package org.mckayerp.condor_downloader;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.util.function.UnaryOperator;

public class TaskCodeFormatter extends TextFormatter<String>
{

    static final UnaryOperator<Change> codeFilter = change ->
    {
        String newText = change.getControlNewText();
        if (newText.trim().matches("([a-z,A-Z]*)?"))
        {
            change.setText(change.getText().trim().toUpperCase());
            return change;
        } else
        {
            change.setRange(0, 0);
        }
        return null;
    };

    static final StringConverter<String> converter = new StringConverter<>()
    {
        @Override
        public String toString(String s)
        {
            return s.toUpperCase();
        }

        @Override
        public String fromString(String s)
        {
            return s.toUpperCase();
        }
    };

    public TaskCodeFormatter()
    {
        super(converter, "", codeFilter);
    }

}
