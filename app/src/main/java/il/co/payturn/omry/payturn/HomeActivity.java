package il.co.payturn.omry.payturn;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private ImageView ivMenu;
    private TextView tvHeader;
    private BottomNavigationView bottomNavigationView;
    private TextView tvHelloUser;
    private ViewPager viewPager;

    private SharedPreferences prefs;

    private final int CAMERA_PERMISSION_REQUEST = 2;
    private final int READ_EXTERNAL_STORAGE = 3;
    private final int WRITE_EXTERNAL_STORAGE = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Action bar stuff goes here
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        //

        scheduleJob(this); // calls the static scheduler

        ivMenu = (ImageView) findViewById(R.id.ivMenu);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_bar);
        tvHelloUser = (TextView) findViewById(R.id.tvHelloUser);
        viewPager = (ViewPager) findViewById(R.id.vp_home);

        // Swipe Adapter | Fragments
        viewPager.setOffscreenPageLimit(1);
        SwipeAdapter swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
        viewPager.setAdapter(swipeAdapter);
        viewPager.setCurrentItem(0);

        // Asks for app permissions to use the camera & gallery
        // Permissions
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //Checks for user permission to use gallery
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //Checks for user permission to use the camera
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //Checks for user permission to add images to user's gallery
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
        }

        // Welcome Message
        prefs = this.getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); // Importing register information (name, email, password) to profile page
        String[] parts = prefs.getString(RegisterActivity.FULL_NAME, "").split(" ");
        tvHelloUser.setText(parts[0] + ", Welcome to \nPayturn.");

        bottomNavigationView.setSelectedItemId(R.id.nav_home); // set menu item state check

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            /**
             * onNavigationItemSelected - every time the user clicks on an item from the bottom navigation menu, this method is called.
             * The user can be redirected to the following activities from this navigation listener: DashboardActivity and ProfileActivity.
             * @param item
             * @return
             */
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_dashboard:
                        directToDashboardPage();
                        break;
                    case R.id.nav_profile:
                        directToProfilePage();
                        break;
                }
                return true;
            }
        });



    }

    /**
     * onCreateOptionsMenu - called when user clicks on the ActionBar menu and inflates (opens) it.
     * @param menu - the type of the parameter menu
     * @return returns the super class which will then return true (because HomeActivity does not extend Activity, a call to the super class is required)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hamburger_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * directToProfilePage - moves from HomeActivity to ProfileActivity with intent
     */
    private void directToProfilePage() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
    directToDashboardPage - moves from HomeActivity to DashboardActivity with intent
     */
    private void directToDashboardPage() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * scheduleJob - schedule the start of the service every 10 minutes
     * @param context - the class in which the function will run in, for example HomeActivity
     */
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, DebtJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(600 * 1000); // wait at least 10 minutes 600 * 1000
        builder.setOverrideDeadline(720 * 1000); // maximum delay 12 minutes 720 * 1000
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());

        int resultcode = jobScheduler.schedule(builder.build());

    }


}
