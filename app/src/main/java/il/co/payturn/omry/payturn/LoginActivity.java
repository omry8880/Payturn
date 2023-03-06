package il.co.payturn.omry.payturn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements View.OnClickListener {

    private EditText username, password;
    private TextView tvDirectToRegister;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.etUsername);
        password = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvDirectToRegister = (TextView) findViewById(R.id.tvDirectToRegister);

        btnLogin.setOnClickListener(this);
        tvDirectToRegister.setOnClickListener(this);

    }

    /**
     * onClick - this method is called every time a click was made, and it redirects the user to another activity (the register activity or home activity).
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                login();
                break;
            case R.id.tvDirectToRegister:
                directToRegisterPage();
                break;
        }
    }

    /**
     * login - the user is redirected from LoginActivity to HomeActivity with a cool animation.
     */
    private void login() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * directToRegisterPage - the user is redirected from LoginActivity to RegisterActivity with a cool animation.
     */
    private void directToRegisterPage() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
