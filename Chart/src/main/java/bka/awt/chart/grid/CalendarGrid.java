/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.grid;


public class CalendarGrid extends TimestampGrid {

    
    @Override
    protected StepSize[] getStepSizes() {
        return new StepSize[] { StepSize.ONE_YEAR, StepSize.ONE_MONTH, StepSize.ONE_WEEK, StepSize.ONE_DAY };
    }
    
        
}
