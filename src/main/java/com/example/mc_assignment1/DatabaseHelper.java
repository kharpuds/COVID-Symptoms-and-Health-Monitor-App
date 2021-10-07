package com.example.mc_assignment1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    SQLiteDatabase sqLiteDatabase;


    private static final String TABLE_NAME = "KHARPUDE";
    public static final String DB = "computer.db";
    public static final int VER = 1;
    public static final String col1 = "HEART_RATE";
    public static final String col2 = "RESPIRATORY_RATE";
    public static final String col3 = "NAUSEA";
    public static final String col4 = "HEADACHE";
    public static final String col5 = "DIARRHEA";
    public static final String col6 = "SORE_THROAT";
    public static final String col7 = "MUSCLE_ACHE";
    public static final String col8 = "FEVER";
    public static final String col9 = "LOSS_OF_SMELL_OR_TASTE";
    public static final String col10 = "COUGH";
    public static final String col11 = "SHORTNESS_OF_BREATH";
    public static final String col12 = "FEELING_TIRED";
    String query;

    public DatabaseHelper(@Nullable Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            query = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + col1 + " text,"
                    + col2 + " text,"
                    + col3 + " text,"
                    + col4 + " text,"
                    + col5 + " text,"
                    + col6 + " text,"
                    + col7 + " text,"
                    + col8 + " text,"
                    + col9 + " text,"
                    + col10 + " text,"
                    + col11 + " text,"
                    + col12 + " text"
                    + ")";
            sqLiteDatabase.execSQL(query);
            sqLiteDatabase.setTransactionSuccessful(); //committing changes
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        query = "DROP TABLE IF EXISTS "+TABLE_NAME;
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.setTransactionSuccessful();
        onCreate(sqLiteDatabase);

    }

    public void insertData(Map<String, Float> symptomMap) {
        ContentValues contentValues = new ContentValues();
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.beginTransaction();

        try {
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(ID) FROM " + TABLE_NAME, null);
            int id = 1;
            if (cursor.getCount() > 0) {
                int i = 0;
                while (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                    i++;
                }
            }


            Iterator<Map.Entry<String, Float>> iter = symptomMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Float> entry = iter.next();
                String column_name = entry.getKey();
                float rating = entry.getValue();
                contentValues.put(column_name, String.valueOf(rating));
            }
            int result = sqLiteDatabase.update(TABLE_NAME, contentValues, "ID=?", new String[]{String.valueOf(id)});
            sqLiteDatabase.setTransactionSuccessful();
            Log.d("database", "Upload successful!");
        } catch (Exception e) {
            Log.d("Exception", e.getMessage());

        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void uploadSigns(int heartRate, int respiRate){
        ContentValues contentValues = new ContentValues();
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.beginTransaction();

        try {
            contentValues.put("HEART_RATE", String.valueOf(heartRate));
            contentValues.put("RESPIRATORY_RATE", String.valueOf(respiRate));
            long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
            sqLiteDatabase.setTransactionSuccessful();
            if(result == -1){
                Log.d("upload signs", "Data not inserted!");
            }else{
                Log.d("upload signs", "Data inserted!");
            }
        }catch (SQLiteException e){
            Log.d("Exception", e.getMessage());
            sqLiteDatabase.endTransaction();
        }finally {
            sqLiteDatabase.endTransaction();
        }
    }
}
