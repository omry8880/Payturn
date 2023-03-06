package il.co.payturn.omry.payturn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView tvHelloUser;
    private TextView tvEmptyPlaceholderHistory;
    private ListView lvHistory;

    private ArrayList<Debt> history;
    private HistoryAdapter adapter;

    private DatabaseReference database;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Action bar stuff goes here
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        //

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_bar);
        tvHelloUser = (TextView) findViewById(R.id.tvHelloUserDashboard);
        tvEmptyPlaceholderHistory = (TextView) findViewById(R.id.tvEmptyPlaceholderDashboardHistory);
        lvHistory = (ListView) findViewById(R.id.lv_history);

        lvHistory.setEmptyView(tvEmptyPlaceholderHistory); //If the ListView is empty, a placeholder "No past debts/debits" text will instead appear


        //Welcome Message
        prefs = this.getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); //Importing register information (name, email, password) to profile page
        String[] parts = prefs.getString(RegisterActivity.FULL_NAME, "").split(" ");
        tvHelloUser.setText(parts[0] + ", Welcome to \nPayturn.");

        String[] userID = prefs.getString(RegisterActivity.EMAIL, "").split("@"); // Gets the userID from shared preferences

        database = FirebaseDatabase.getInstance().getReference();

        database.child("Users").child(userID[0]).child("History").addValueEventListener(new ValueEventListener() {

            /**
             * onDataChange - every time there's a change in Firebase in the selected path we created, this function will be called.
             * Inside the onDataChange we go over all of the debts objects in Firebase and place them in an ArrayList which will then be displayed inside a selected ListView with the help of an adapter
             * @param dataSnapshot - Any time you read Database data, you receive the data as a DataSnapshot, in this case it will be a debt object
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                history = new ArrayList<>();
                for (DataSnapshot debtSnapshot : dataSnapshot.getChildren()) {

                    history.add(debtSnapshot.getValue(Debt.class));

                }

                adapter = new HistoryAdapter(history, getApplicationContext());
                lvHistory.setAdapter(adapter);
                Collections.sort(history);
            }

            /**
             * if the function onDataChange returns an error, this function will be called instead
             * @param databaseError
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        });

        /**
         * setSelectedItemId - set menu item state check
         */
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        /**
         * setOnNavigationItemSelectedListener - every time an item in the bottom navigation bar is clicked, this function will be called
         */
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        directToHomePage();
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
    private void directToHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
