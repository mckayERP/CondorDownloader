package org.mckayerp.condor_downloader;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class NumberFieldFormatter extends TextFormatter<Integer>
{

    static final UnaryOperator<TextFormatter.Change> integerFilter = change ->
    {
        String newText = change.getControlNewText();
        if (newText.matches("([0-9]+)?"))
        {
            return change;
        } else
        {
            change.setRange(0, 0);
        }
        return null;
    };

    static final StringConverter<Integer> converter = new IntegerStringConverter()
    {
        // Modified version of standard converter that evaluates an empty string
        // as zero instead of null:
        @Override
        public Integer fromString(String s)
        {
            if (s.isEmpty())
                return 0;
            return super.fromString(s);
        }
    };

    public NumberFieldFormatter()
    {
        super(converter, 0, integerFilter);
    }

}
