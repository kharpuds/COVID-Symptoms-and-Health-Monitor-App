package com.example.mc_assignment1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class SymptomLoggingPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseHelper databaseHelper;
    Map<String, Float> symptomsMap = new HashMap<String, Float>();
    Button uploadSymptomsButton;
    Spinner symptomSpinner;
    RatingBar ratingBar; // initiate a rating bar
    Button addSymptom;
    float rating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_logging_page);
        uploadSymptomsButton = (Button) findViewById(R.id.uploadSymptoms);
        symptomSpinner = (Spinner) findViewById(R.id.symptoms_spinner);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar); // initiate a rating bar

        databaseHelper = new DatabaseHelper(SymptomLoggingPage.this);

        //array adapter is the container that will hold the values and integrate it with spinner
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(SymptomLoggingPage.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.symptoms_spinner));

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomSpinner.setAdapter(arrayAdapter); //setting my spinner to this adapter

        symptomsMap.put("NAUSEA", 0.0f);
        symptomsMap.put("HEADACHE", 0.0f);
        symptomsMap.put("DIARRHEA", 0.0f);
        symptomsMap.put("SORE_THROAT", 0.0f);
        symptomsMap.put("FEVER", 0.0f);
        symptomsMap.put("MUSCLE_ACHE", 0.0f);
        symptomsMap.put("LOSS_OF_SMELL_OR_TASTE", 0.0f);
        symptomsMap.put("COUGH", 0.0f);
        symptomsMap.put("SHORTNESS_OF_BREATH", 0.0f);
        symptomsMap.put("FEELING_TIRED", 0.0f);

//        addSymptom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String item = symptomSpinner.getSelectedItem().toString();
//                String columnName = getColName(item);
//                symptomsMap.put(columnName, rating);
//            }
//        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                String item = symptomSpinner.getSelectedItem().toString();
                rating = ratingBar.getRating();
                String columnName = getColName(item);
                symptomsMap.put(columnName, rating);
//                Toast.makeText(SymptomLoggingPage.this, String.valueOf(rating), Toast.LENGTH_LONG).show();

            }
        });


        uploadSymptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.insertData(symptomsMap);
            }
        });

        symptomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = symptomSpinner.getSelectedItem().toString();
                String columnName = getColName(item);
                symptomsMap.put(columnName, rating);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(SymptomLoggingPage.this, "Please select and rate all symptoms", Toast.LENGTH_LONG).show();
            }
        });

    }

    private String getColName(String item) {
        if (item.equalsIgnoreCase("NAUSEA")) {
            return "NAUSEA";
        } else if (item.equalsIgnoreCase("HEADACHE")) {
            return "HEADACHE";
        }
        if (item.equalsIgnoreCase("DIARRHEA")) {
            return "DIARRHEA";
        }
        if (item.equalsIgnoreCase("SORE THROAT")) {
            return "SORE_THROAT";
        }
        if (item.equalsIgnoreCase("FEVER")) {
            return "FEVER";
        }
        if (item.equalsIgnoreCase("MUSCLE ACHE")) {
            return "MUSCLE_ACHE";
        }
        if (item.equalsIgnoreCase("LOSS OF SMELL OR TASTE")) {
            return "LOSS_OF_SMELL_OR_TASTE";
        }
        if (item.equalsIgnoreCase("COUGH")) return "COUGH";
        if (item.equalsIgnoreCase("SHORTNESS OF BREATH")) {
            return "SHORTNESS_OF_BREATH";
        } else {
            return "FEELING_TIRED";
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getSelectedItem().toString();
        Toast.makeText(SymptomLoggingPage.this, item, Toast.LENGTH_SHORT).show();
        rating = ratingBar.getRating();
//        Toast.makeText(SymptomLoggingPage.this, String.valueOf(rating), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Toast.makeText(this, "Please select all symptoms and rate each!", Toast.LENGTH_LONG).show();
    }

}