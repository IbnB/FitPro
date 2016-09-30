package lu.uni.ibrahimtahirou.fitpro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import lu.uni.ibrahimtahirou.fitpro.myfragments.ActivityProfileFragment;
import lu.uni.ibrahimtahirou.fitpro.myfragments.HelpFragment;
import lu.uni.ibrahimtahirou.fitpro.myfragments.MobilityProfileFragment;
import lu.uni.ibrahimtahirou.fitpro.myfragments.TabFragment;
import lu.uni.ibrahimtahirou.fitpro.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    private final int SHOW_PREFERENCES = 0;
    public static volatile int mScanInterval;
    Preference wifiScanIntvalPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<String> settingsValue = new ArrayList<>();




         /*Setup the DrawerLayout and NavigationView*/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shiftStuff);


          /*Lets inflate the very first fragment
          Here , we are inflating the TabFragment as the first Fragment*/
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();

          /*Setup click events on the Navigation View Items.*/
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.nav_item_home) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();

                }

                if (menuItem.getItemId() == R.id.nav_item_activity) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, new ActivityProfileFragment()).commit();

                }

                if (menuItem.getItemId() == R.id.nav_item_mobility) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, new MobilityProfileFragment()).commit();

                }


                if (menuItem.getItemId() == R.id.nav_item_settings) {

                    Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivityForResult(i, SHOW_PREFERENCES);
                    return true;

                }

                if (menuItem.getItemId() == R.id.nav_item_help) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, new HelpFragment()).commit();
                    //Intent mapActIntent = new Intent(MainActivity.this, MapFullscreenActivity.class);
                    //startActivity(mapActIntent);
                    Intent mapActIntent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(mapActIntent);


                }

                if (menuItem.getItemId() == R.id.nav_item_logout) {

                    Intent mapActIntent = new Intent(MainActivity.this, MultiMapDemoActivity.class);
                    startActivity(mapActIntent);
                    Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                }

                return false;
            }

        });


          /*Setup Drawer Toggle of the Toolbar*/
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

    }


    public ArrayList<String> readAndShowValues() {
        ArrayList<String> mSettingsData = new ArrayList<String>();
        // store settings
        String string = "";

        // load preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        // edit text for Username : index 0 in the array
        String text = sharedPref.getString("edittext_preference", "");
        string += "EditText : " + text;
        mSettingsData.add(text);

        // Wifi scan interval: index 1 in the array
        String wifiScanInval = sharedPref.getString("wifi_scan_intval_pref", "");
        string += "Wifi Scan Intval : " + wifiScanInval + "\n";
        mSettingsData.add(wifiScanInval.replaceAll("[^0-9]", "")); //regex to take only inte

        // Data recording  interval: index 2 in the array
        String dataRcrdingIntval = sharedPref.getString("data_record_pref", "");
        string += "Data Recording Intval: " + dataRcrdingIntval + "\n";
        mSettingsData.add(dataRcrdingIntval.replaceAll("[^0-9]", ""));

        // switch preference: index 3 in the array
        boolean swtichPref = sharedPref.getBoolean("startStopService", true);
        if (swtichPref) {
            string += "switch is ON\n";
            mSettingsData.add("switch is ON");
        } else {
            string += "switch is OFF\n";
            mSettingsData.add("switch is OFF");

        }

        // checkbox  preference for debug: index 4 in the array
        boolean debugPref = sharedPref.getBoolean("checkbox_preference1", false);
        if (!debugPref) {
            string += "false 0\n";
            mSettingsData.add("false");
        } else {
            string += "true 1\n";
            mSettingsData.add("true");

        }

        // Activity scan Interval: index 5 in the array
        String activityScanInterval = sharedPref.getString("activity_scan_Interval_pref", "");
        string += "List : " + activityScanInterval + "\n";
        mSettingsData.add(activityScanInterval.replaceAll("[^0-9]", ""));

        // first checkbox: index 6 in the array
        boolean checked1 = sharedPref.getBoolean("checkbox_preference1", false);
        if (checked1) {
            string += "checkbox 1 checked\n";
            mSettingsData.add("checkbox 1 checked");
        } else {
            string += "checkbox 1 not checked\n";
            mSettingsData.add("checkbox 1 not checked");
        }

        // second checkbox: index 7 in the array
        boolean checked2 = sharedPref.getBoolean("checkbox_preference2", false);
        if (checked2) {
            string += "checkbox 2 checked\n";
            mSettingsData.add("checkbox 2 checked");
        } else {
            string += "checkbox 2 not checked\n";
            mSettingsData.add("checkbox 2 checked");
        }


        // show values
        // Log.i("Main-SettingsData", string);
        // textView.setText(string);
        return mSettingsData;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //readAndShowValues();
        Intent mIntent = new Intent();
        mIntent.setPackage(getPackageName());
        mIntent.setAction(Constant.ACTION_SETTINGS_DATA);
        mIntent.putStringArrayListExtra("settings", readAndShowValues());

        sendBroadcast(mIntent);


    }


    /*Method to start WifiScan service*/
    public void startWifiSanService() {
        // Intent wifiScanIntent = new Intent(getApplicationContext(), WifiScanService.class);
        //startService(wifiScanIntent);
    }

    private void myToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

