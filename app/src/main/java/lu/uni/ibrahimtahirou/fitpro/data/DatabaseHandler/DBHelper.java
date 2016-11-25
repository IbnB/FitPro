package lu.uni.ibrahimtahirou.fitpro.data.DatabaseHandler;


/**
 * Created by ibrahimtahirou on 7/28/16.
 */

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import lu.uni.ibrahimtahirou.fitpro.App;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fitnessTrailAssistant.db";
    public static final String DBLOCATION = "/data/data/lu.uni.ibrahimtahirou.fitpro/databases/";
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = DBHelper.class.getSimpleName().toString();


    public DBHelper() {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {


        //create interval and activityTable

        //db.execSQL(SurveyRepository.createTable());


        Log.i(TAG, "Tables created succesfully");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, String.format("SQLiteDatabase.onUpgrade(%d -> %d)", oldVersion, newVersion));

        //db.execSQL("DROP TABLE IF EXISTS " + SurveyTable.TABLE);


        onCreate(db);
    }


}