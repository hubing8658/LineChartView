
package com.iss.linechart;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.iss.linechart.LineChartView.IPointValue;

public class MainActivity extends Activity {

    private LineChartView lcv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lcv = (LineChartView) findViewById(R.id.lcv);
        lcv.setBaseLineCount(10);
        lcv.setNeedShowPointValue(true);
        update();
    }

    public void onClick(View v) {
        update();
    }
    
    private void update() {
        lcv.clearAllLines();

        IPointValue[] line1Points = new IPointValue[10];
        for (int i = 0; i < line1Points.length; i++) {
            float value = (float) (Math.random() * 100);
            line1Points[i] = new IPointValueImpl(value);
        }
        int line1Color = 0xFF4cc2b6;
        lcv.addLinePoints(line1Points, line1Color);

        IPointValue[] line2Points = new IPointValue[10];
        for (int i = 0; i < line2Points.length; i++) {
            float value = (float) (Math.random() * 100);
            line2Points[i] = new IPointValueImpl(value);
        }
        int line2Color = 0xFFff7e8e;
        lcv.addLinePoints(line2Points, line2Color);
        
    }
    
}
