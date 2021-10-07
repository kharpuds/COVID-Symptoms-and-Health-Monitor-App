package com.example.mc_assignment1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static final int VIDEO_CAPTURE = 1;
    Uri videoUri;
    private SensorManager sensorManager;
    Sensor accelerometer;
    float[] z_values = new float[240];
    int index = 0;
    int heartRate = 0;
    int respiRate = 0;
    SensorHandler sensorHandler = new SensorHandler();
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();


    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            camera.setPreviewCallback(previewCallback);
        }
    };

    //video record variables
    private MediaRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button heartRateButton = (Button) findViewById(R.id.heartRate);
        Button symptomsButton = (Button) findViewById(R.id.symptoms);
        Button respiRateButton = (Button) findViewById(R.id.respiratoryRate);
        Button uploadSigns = (Button) findViewById(R.id.uploadSigns);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        heartRateButton.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Calculating Heart Rate...", Toast.LENGTH_LONG);

                try {

                    initRecorder();
                    recorder = new MediaRecorder();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }


            }
        });


        symptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent symptomsIntent = new Intent(getApplicationContext(), SymptomLoggingPage.class);
                startActivity(symptomsIntent);
            }
        });

        respiRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Calculating Respiratory Rate...", Toast.LENGTH_SHORT).show();
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener((SensorEventListener) MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            }
        });

        uploadSigns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadRatesToDatabase(heartRate, respiRate, databaseHelper);
                Toast.makeText(getApplicationContext(), "Signs Uploaded Successfully!", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void uploadRatesToDatabase(int heartRate, int respiRate, DatabaseHelper dbHelper) {
        dbHelper.uploadSigns(heartRate, respiRate);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private void initRecorder() throws Exception {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startRecordingVideo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void startRecordingVideo() throws InterruptedException {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File mediaFile = new File(getExternalFilesDir(null).toString() + "/FingertipVideo.mp4");
            videoUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.mc_assignment1", mediaFile);
            startActivityForResult(intent, VIDEO_CAPTURE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
            retriever.setDataSource(getApplicationContext(), data.getData());

            Bitmap bitmap;

            int j = 0;
            MediaPlayer mediaPlayer = MediaPlayer.create(this, data.getData());
            int millis = mediaPlayer.getDuration();
            float[] red_avg = new float[millis];
            Toast.makeText(getApplicationContext(), "Processing Video...", Toast.LENGTH_SHORT).show();
            for (int i = 1000000; i < millis * 1000; i += 100000) {

                bitmap = retriever.getFrameAtTime((long) i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                float r = 0;

                try {

                    int pixel1 = bitmap.getPixel(540, 960);
                    int pixel2 = bitmap.getPixel(100, 100);
                    int pixel3 = bitmap.getPixel(900, 1800);
                    int pixel4 = bitmap.getPixel(1040, 200);
                    int pixel5 = bitmap.getPixel(1000, 500);
                    int pixel6 = bitmap.getPixel(700, 100);

                    int pixel = pixel1 + pixel2 + pixel3 + pixel4 + pixel5 + pixel6;

                    r += Color.red(pixel);

                    i++;
                } catch (Exception e) {
                    Log.d("bitmap err", "Bitmap error:" + e.getStackTrace());

                }
//                r /= total;
                red_avg[j] = r;
                j++;

//                Toast.makeText(getApplicationContext(), "Last red avg value=" + (r), Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "Red average values list =" + (red_avg), Toast.LENGTH_LONG).show();
                Log.d("redvalues", "Red values = " + r);
                Log.d("red average length", "Red average list length = " + red_avg.length);

            }

            //send red avg values to rate computation/peak detection method
            heartRate = (int) (1.5 * sensorHandler.computeRate(red_avg));
            Toast.makeText(getApplicationContext(), "Heart rate =" + (heartRate), Toast.LENGTH_LONG).show();
            Log.d("heartrate", "Heart rate = " + heartRate);


        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    public void showNoFlashError() {
        Toast.makeText(getApplicationContext(), "Flash not available!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("sensor values", "z-axis values: " + sensorEvent.values[2]);
        z_values[index] = sensorEvent.values[2];
        if (index >= 127) {

            index = 0;
            respiRate = sensorHandler.computeRate(z_values);
            Toast.makeText(getApplicationContext(), "Respiratory rate = " + respiRate, Toast.LENGTH_LONG).show();
            sensorManager.unregisterListener(this);
        } else {
            index++;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing

    }

}

