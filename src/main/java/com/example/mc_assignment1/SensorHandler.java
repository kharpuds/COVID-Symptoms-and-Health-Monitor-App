package com.example.mc_assignment1;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Arrays;

public class SensorHandler extends Service implements SensorEventListener {
    float maxInt = -1000f;
    float minInt = 1000f;
//    double sum = 0.0;
    double dynamicRangeUp;
    double dynamicRangeDown;
    double thresholdUp;
    double thresholdR;
    double thresholdDown;
    double thresholdQ;
    int up = 1;
    float prev_peak;
    int k = 0;
    int possiblePeak = 0;
    int rpeak = 0;
    float[] rpeak_index =new float [240];
    int qpeak = 0;
    float[] qpeak_index = new float [240];
    int speak = 0;
    float[] speak_index = new float [240];
    int peakType = 0;

    public int computeRate(float[] z_values) {
        double sum = 0f, average = 0f;
        float[] LPF = new float[z_values.length];
        float[] HPF = new float[z_values.length];
        float[] x = new float[z_values.length];

        //LPF computation
        for(int i = 12; i <= 45; i++) {
            int index = i-12;
            if(index < 3) {
                LPF[index] = (float) 0.5 * (z_values[i] - 2 * z_values[i - 6] + z_values[i - 12]);
            } else {
                LPF[index] = (float)0.5*(2*LPF[index-1] - LPF[index-2] + z_values[i] - 2*z_values[i-6] + z_values[i-12]);
            }

        }

        //HPF computation
        for(int i = 46; i < z_values.length; i++) {
            int index = i - 12;
            int index2 = i - 45;
            LPF[index] = (float)0.5*(2*LPF[index - 1] - LPF[index - 2] + z_values[i] - 2*z_values[i - 6] + z_values[i - 12]);
            if(index2 < 2) {
                HPF[index2] = (float)(0.03125) * (32 * LPF[index - 16] + (LPF[index] - LPF[index - 32]));
            }
            else {
                HPF[index2] = (float)(0.03125) * (32 * LPF[index - 16] - (HPF[index2 - 1] + LPF[index] - LPF[index - 32]));
            }
        }

        //Getting peaks by using HPF and LPF values
        for(int i = 0; i < HPF.length; i++) {

            x[i] = -1*HPF[i];
            sum += x[i];

            if(x[i] > maxInt) maxInt = x[i];
            if(x[i] < minInt) minInt = x[i];
        }

        average = sum/x.length;
        dynamicRangeUp = maxInt - average;
        dynamicRangeDown = average - minInt;

        thresholdUp = 0.000002*dynamicRangeUp;
        thresholdR = 0.005*dynamicRangeUp;
        thresholdDown = 0.000002*dynamicRangeDown;
        thresholdQ = 0.1*dynamicRangeDown;

        up = 1;
        prev_peak = x[1];


        int Rpeak = 0;
        int PeakType = 0;
        int i = 1;
        int[] peak_index = new int[z_values.length];

        double maximum = -1000.0;
        double minimum = 1000.0;

        while(i<x.length){
            if(x[i] > maximum){
                maximum = x[i];
            }
            if(x[i] < minimum){
                minimum = x[i];
            }

            if(up == 1){
                if(x[i] < maximum){
                    if(possiblePeak == 0){
                        possiblePeak = i;
                    }
                    if(x[i] < (maximum - thresholdUp)){
                        k = k+1;
                        peak_index[k] = possiblePeak - 1;
                        minimum = x[i];
                        up = 0;
                        possiblePeak = 0;
                        if(PeakType == 0){
                            if(x[peak_index[k]] > average + thresholdR){
                                Rpeak = Rpeak + 1;
                                prev_peak = x[peak_index[k]];
                            }
                        }else{
                            if((Math.abs((x[peak_index[k]] - prev_peak) / prev_peak) > 1.5) && (x[peak_index[k]] > average+thresholdR)){
                                Rpeak = Rpeak + 1;
                                prev_peak = x[peak_index[k]];
                            }
                        }
                    }
                }
            }else{
                if(x[i] > minimum){
                    if(possiblePeak == 0){
                        possiblePeak = 1;
                    }
                    if(x[i] > (minimum + thresholdDown)){
                        k = k + 1;
                        peak_index[k] = possiblePeak - 1;
                        maximum = x[i];
                        up = 1;
                        possiblePeak = 0;
                    }
                }
            }
            i++;
        }
        return Rpeak;


    }

    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[240];
    float accelValuesY[] = new float[240];
    float accelValuesZ[] = new float[240];
    int index = 0;

    Bundle b;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        Toast.makeText(getApplicationContext(), "Inside sensor handler", Toast.LENGTH_SHORT).show();
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("Z values: ", "z: "+ sensorEvent.values[2]);
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];
            if(index >= 239) {
                index = 0;
                accelManage.unregisterListener(this);

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
