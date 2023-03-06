package il.co.payturn.omry.payturn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends Activity implements View.OnClickListener {

    TextInputEditText etFullName, etEmail, etPassword, etConfPassword;
    Button btnRegister;
    ImageView ivBack;

    public static final String SETTING_PREF = "SETTING_PREF";
    public static final String FULL_NAME = "fullNameKey";
    public static final String EMAIL = "emailKey";
    public static final String PASSWORD = "passwordKey";
    public static final String CONF_PASSWORD = "confirmPasswordKey";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = (TextInputEditText) findViewById(R.id.etFullName);
        etEmail = (TextInputEditText) findViewById(R.id.etEmail);
        etPassword = (TextInputEditText) findViewById(R.id.etPassword);
        etConfPassword = (TextInputEditText) findViewById(R.id.etConfirmPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        ivBack = (ImageView) findViewById(R.id.ivBack);

        ivBack.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        //Shared Preferences - Load register information if user had signed up before
        prefs = getSharedPreferences(SETTING_PREF, Context.MODE_PRIVATE);
        etFullName.setText(prefs.getString(FULL_NAME, ""));
        etEmail.setText(prefs.getString(EMAIL, ""));
        etPassword.setText(prefs.getString(PASSWORD, ""));
        etConfPassword.setText(prefs.getString(CONF_PASSWORD, ""));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                if (etFullName.getText().toString().length() == 0) { //username
                    etFullName.setError("You need to enter your full name.");
                }

                if (etEmail.getText().toString().length() == 0) { //email
                    etEmail.setError("You need to enter your e-mail.");
                }
                else if (!etEmail.getText().toString().contains("@")) {
                    etEmail.setError("E-mail is not valid.");
                }

                if (etPassword.getText().toString().length() == 0) { //password
                    etPassword.setError("You need to enter a password.");
                }
                else if (etPassword.getText().toString().length() < 6) {
                    etPassword.setError("Password must be more than 5 characters!");
                }
                if (etConfPassword.getText().toString().length() == 0) { //conf pass
                    etConfPassword.setError("You need to enter your password again.");
                }
                else if (!etPassword.getText().toString().equals(etConfPassword.getText().toString())) {
                    etPassword.setError("Password and Password Confirmation fields need to be the same.");
                }
                else {
                    successfullyRegistered();
                }
                break;
            case R.id.ivBack:
                backToLogin();
                break;
        }
    }

    /**
     * successfullyRegistered - saves the user register information inside both SharedPreferences and Firebase.
     */
    private void successfullyRegistered() {
        // Shared Preferences - Saving register information
        prefs = getSharedPreferences(SETTING_PREF, Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString(FULL_NAME, etFullName.getText().toString());
        editor.putString(EMAIL, etEmail.getText().toString());
        editor.putString(PASSWORD, etPassword.getText().toString());
        editor.putString(CONF_PASSWORD, etConfPassword.getText().toString());
        editor.apply(); //commit



        // Firebase Instance
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        // Creating the User
        User user = new User(etEmail.getText().toString(), etFullName.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString());
        myRef.child("Users").child(user.getUserID()).setValue(user);

        Intent registerToMain = new Intent(this, HomeActivity.class);
        startActivity(registerToMain);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * backToLogin - redirects the user to LoginActivity with an intent
     */
    private void backToLogin() {
        Intent back = new Intent(this, LoginActivity.class);
        startActivity(back);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}

