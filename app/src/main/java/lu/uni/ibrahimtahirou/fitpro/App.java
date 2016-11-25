package lu.uni.ibrahimtahirou.fitpro;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import lu.uni.ibrahimtahirou.fitpro.data.DatabaseHandler.DBHelper;
import lu.uni.ibrahimtahirou.fitpro.data.DatabaseHandler.DatabaseManager;


/**
 * Created by ibrahimtahirou on 8/21/16.
 * ACRA
 * Key:thredntinkshemblingswith
 * Password:ccf52363ed509c2434d25d458a6d5a657be87bf0
 */


public class App extends Application {
    private static Context context;
    private static DBHelper dbHelper;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = this.getApplicationContext();
        dbHelper = new DBHelper();
        DatabaseManager.initializeInstance(dbHelper);
        Log.i("APP class", "App class executed successfully");


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //MultiDex.install(this);
    }
}

