package br.liveo.ndrawer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.okhttp.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.gcm.QuickstartPreferences;
import br.liveo.ndrawer.ui.gcm.RegistrationIntentService;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int REQUEST_SIGNUP = 0;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SharedPreferences prefs;

    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;

    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean checkLogin = false;
        checkLogin = checkPreLogin();

        if(checkLogin == false) {
            setContentView(R.layout.activity_login);
            ButterKnife.inject(this);

            _emailText = (EditText) findViewById(R.id.input_email);
            _passwordText = (EditText) findViewById(R.id.input_password);


            _loginButton = (Button) findViewById(R.id.btn_login);
            _loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });

            _signupLink = (TextView) findViewById(R.id.link_signup);
            _signupLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(getApplicationContext(), SignupActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public boolean checkPreLogin(){
        prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
        String useremail = prefs.getString("useremail", "");
        String userpassword = prefs.getString("userpassword", "");

        if(useremail.equals("")){
            return false;
        } else {
            return true;
        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        try {
            String url = "http://125.131.73.198:3000/login";
            RequestClass rc = new RequestClass(url);
            rc.AddParam("useremail", email);
            rc.AddParam("userpassword", password);

            rc.Execute(1);
            String response = rc.getResponse();

            if (response.length() == 0) {
                onLoginFailed();
            } else {
                JSONArray arr = new JSONArray(response);
                if (arr.length() != 0) {
                    JSONObject obj = arr.getJSONObject(0);
                    prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("useremail", obj.getString("useremail"));
                    edit.putString("userpassword", obj.getString("userpassword"));
                    edit.putString("usersex", obj.getString("usersex"));
                    edit.putInt("userage", obj.getInt("userage"));
                    edit.putString("userlocaton", obj.getString("userlocation"));
                    edit.commit();

                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    onLoginFailed();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Email이나 패스워드가 잘못됬습니다", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Email 형식이 잘못됬습니다");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("비밀번호를 입력해 주세요");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }



}
