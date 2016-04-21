package com.iss.linechart;

import com.iss.linechart.LineChartView.IPointValue;

/**
 * @author hubing
 * @version 1.0.0 2016-2-22
 */

public class IPointValueImpl implements IPointValue {

    public float value;
    
    public IPointValueImpl(float value) {
        this.value = value;
    }
    
    @Override
    public float getValue() {
        return value;
    }

}

