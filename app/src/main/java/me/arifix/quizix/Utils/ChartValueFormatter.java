package me.arifix.quizix.Utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by Arif Khan on 5/27/2018.
 */

public class ChartValueFormatter implements IValueFormatter {

    // Format Chart Value - Float to Integer
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return "" + ((int) value);
    }
}
